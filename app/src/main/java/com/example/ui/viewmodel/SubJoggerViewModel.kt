package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SampleDataProvider
import com.example.data.local.UserPreferences
import com.example.data.model.BillingFrequency
import com.example.data.model.JoggerTemplate
import com.example.data.model.ParsedCandidate
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem
import com.example.data.repository.SubscriptionRepository
import com.example.domain.AlertScheduler
import com.example.domain.Calculations
import com.example.domain.CropBounds
import com.example.domain.DateUtils
import com.example.domain.IcsGenerator
import com.example.domain.JsonBackupManager
import com.example.domain.LedgerSummary
import com.example.domain.MlKitOcrEngine
import com.example.domain.OnDeviceCandidateExtractor
import com.example.domain.PipelineAuditMetrics
import com.example.domain.PrivacyScrubber
import com.example.domain.ScrubResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FilterTab(val label: String) {
    ALL("All"),
    SUBSCRIPTIONS("Subscriptions"),
    BILLS("Bills"),
    TO_REVIEW("To review")
}

data class PasteScrubUiState(
    val rawText: String = "",
    val scrubResult: ScrubResult = PrivacyScrubber.scrubText("")
)

data class ScreenshotPrepUiState(
    val selectedUri: Uri? = null,
    val cropBounds: CropBounds = CropBounds(),
    val previewMetrics: PipelineAuditMetrics? = null,
    val isProcessingOcr: Boolean = false,
    val errorMessage: String? = null
)

data class ConfirmExtractionUiState(
    val candidates: List<ParsedCandidate> = emptyList(),
    val sourceLabel: String = "On-Device Scrubber"
)

data class SubJoggerUiState(
    val allItems: List<SubscriptionItem> = emptyList(),
    val filteredItems: List<SubscriptionItem> = emptyList(),
    val reviewQueueItems: List<SubscriptionItem> = emptyList(),
    val summary: LedgerSummary = Calculations.summarize(emptyList()),
    val preferences: UserPreferences = UserPreferences(),
    val selectedFilter: FilterTab = FilterTab.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val addedJoggerNames: Set<String> = emptySet()
)

class SubJoggerViewModel(
    private val repository: SubscriptionRepository,
    private val appContext: Context
) : ViewModel() {

    private val selectedFilter = MutableStateFlow(FilterTab.ALL)
    private val searchQuery = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)

    private val _pasteScrubState = MutableStateFlow(PasteScrubUiState())
    val pasteScrubState: StateFlow<PasteScrubUiState> = _pasteScrubState.asStateFlow()

    private val _screenshotState = MutableStateFlow(ScreenshotPrepUiState())
    val screenshotState: StateFlow<ScreenshotPrepUiState> = _screenshotState.asStateFlow()

    private val _confirmState = MutableStateFlow(ConfirmExtractionUiState())
    val confirmState: StateFlow<ConfirmExtractionUiState> = _confirmState.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    val uiState: StateFlow<SubJoggerUiState> = combine(
        repository.subscriptionsFlow,
        repository.preferencesFlow,
        selectedFilter,
        searchQuery,
        isSearchActive
    ) { items, prefs, filter, query, searchOpen ->
        val summary = Calculations.summarize(items)
        val normalizedQuery = query.trim().lowercase()
        val filtered = items.filter { item ->
            val matchesTab = when (filter) {
                FilterTab.ALL -> !item.needsReview
                FilterTab.SUBSCRIPTIONS -> !item.category.isBill && !item.needsReview
                FilterTab.BILLS -> item.category.isBill && !item.needsReview
                FilterTab.TO_REVIEW -> item.needsReview
            }
            val matchesQuery = normalizedQuery.isBlank() ||
                item.name.lowercase().contains(normalizedQuery) ||
                item.category.label.lowercase().contains(normalizedQuery) ||
                item.notes.lowercase().contains(normalizedQuery)
            matchesTab && matchesQuery
        }

        SubJoggerUiState(
            allItems = items,
            filteredItems = filtered,
            reviewQueueItems = items.filter { it.needsReview },
            summary = summary,
            preferences = prefs,
            selectedFilter = filter,
            searchQuery = query,
            isSearchActive = searchOpen,
            addedJoggerNames = items.map { it.name.lowercase() }.toSet()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SubJoggerUiState()
    )

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfFirstLaunch()
            val prefs = repository.preferencesFlow
            prefs.collect { userPrefs ->
                AlertScheduler.syncSchedule(appContext, userPrefs.notificationsEnabled)
            }
        }
    }

    fun setFilter(tab: FilterTab) {
        selectedFilter.value = tab
    }

    fun toggleSearch() {
        val next = !isSearchActive.value
        isSearchActive.value = next
        if (!next) searchQuery.value = ""
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun dismissBanner() {
        viewModelScope.launch { repository.setBannerDismissed(true) }
    }

    fun saveSubscription(item: SubscriptionItem) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.addSubscription(item)
                _toastMessages.tryEmit("Saved ${item.name} locally")
            } else {
                repository.updateSubscription(item)
                _toastMessages.tryEmit("Updated ${item.name}")
            }
        }
    }

    fun confirmReviewItem(item: SubscriptionItem) {
        viewModelScope.launch {
            repository.updateSubscription(item.copy(needsReview = false, confidencePercent = 100))
            _toastMessages.tryEmit("Confirmed ${item.name} to My List")
        }
    }

    fun deleteSubscription(id: Long, name: String) {
        viewModelScope.launch {
            repository.deleteSubscription(id)
            _toastMessages.tryEmit("Removed $name")
        }
    }

    fun addJoggerTemplate(template: JoggerTemplate) {
        viewModelScope.launch {
            val today = DateUtils.todayEpochDay()
            val offsetDays = when (template.frequency) {
                BillingFrequency.WEEKLY -> 7L
                BillingFrequency.MONTHLY -> 14L
                BillingFrequency.YEARLY -> 45L
            }
            val item = SubscriptionItem(
                name = template.name,
                amount = template.suggestedAmount,
                currencyCode = uiState.value.preferences.currencyCode,
                frequency = template.frequency,
                category = template.category,
                nextBillingEpochDay = today + offsetDays,
                sourceTag = SourceTag.JOGGER,
                notes = template.note
            )
            repository.addSubscription(item)
            _toastMessages.tryEmit("Added ${template.name} from Memory Jogger")
        }
    }

    // Paste & Scrub Pipeline
    fun updatePasteText(text: String) {
        val capped = text.take(3000)
        val scrubResult = PrivacyScrubber.scrubText(capped)
        _pasteScrubState.value = PasteScrubUiState(
            rawText = capped,
            scrubResult = scrubResult
        )
    }

    fun fillSampleReceipt() {
        updatePasteText(SampleDataProvider.sampleReceiptText)
    }

    fun extractCandidatesFromScrubbedText(): Boolean {
        val current = _pasteScrubState.value
        if (current.scrubResult.scrubbedText.isBlank()) {
            _toastMessages.tryEmit("Paste a receipt or SMS first")
            return false
        }
        val candidates = OnDeviceCandidateExtractor.extractCandidates(
            scrubbedText = current.scrubResult.scrubbedText,
            paymentHint = current.scrubResult.detectedLast4Hint,
            defaultCurrency = uiState.value.preferences.currencyCode,
            sourceTag = SourceTag.TEXT_SCRUB
        )
        _confirmState.value = ConfirmExtractionUiState(
            candidates = candidates,
            sourceLabel = "On-Device Regex Scrubber"
        )
        return true
    }

    // Screenshot OCR Pipeline
    fun onScreenshotSelected(uri: Uri) {
        _screenshotState.value = ScreenshotPrepUiState(
            selectedUri = uri,
            cropBounds = CropBounds(),
            isProcessingOcr = false,
            errorMessage = null
        )
        viewModelScope.launch {
            val metrics = MlKitOcrEngine.inspectOriginalBounds(appContext, uri)
            _screenshotState.update { it.copy(previewMetrics = metrics) }
        }
    }

    fun updateCropBounds(bounds: CropBounds) {
        _screenshotState.update { it.copy(cropBounds = bounds) }
    }

    fun runOnDeviceOcrExtraction(onReadyToConfirm: () -> Unit) {
        val current = _screenshotState.value
        val uri = current.selectedUri ?: return
        viewModelScope.launch {
            _screenshotState.update { it.copy(isProcessingOcr = true, errorMessage = null) }
            runCatching {
                MlKitOcrEngine.sanitizeCropAndRecognize(appContext, uri, current.cropBounds)
            }.onSuccess { result ->
                val scrubResult = PrivacyScrubber.scrubText(result.recognizedText)
                val candidates = OnDeviceCandidateExtractor.extractCandidates(
                    scrubbedText = scrubResult.scrubbedText,
                    paymentHint = scrubResult.detectedLast4Hint,
                    defaultCurrency = uiState.value.preferences.currencyCode,
                    sourceTag = SourceTag.SCREENSHOT_OCR
                )
                _screenshotState.update {
                    it.copy(
                        isProcessingOcr = false,
                        previewMetrics = result.audit
                    )
                }
                _confirmState.value = ConfirmExtractionUiState(
                    candidates = candidates,
                    sourceLabel = "On-Device ML Kit OCR (${result.audit.sanitizedKb} KB PNG)"
                )
                onReadyToConfirm()
            }.onFailure { err ->
                _screenshotState.update {
                    it.copy(
                        isProcessingOcr = false,
                        errorMessage = err.message ?: "Failed to read screenshot offline"
                    )
                }
            }
        }
    }

    // Confirm Extraction Checklist actions
    fun toggleCandidateSelection(tempId: String) {
        _confirmState.update { state ->
            state.copy(
                candidates = state.candidates.map {
                    if (it.tempId == tempId) it.copy(selected = !it.selected) else it
                }
            )
        }
    }

    fun updateCandidate(updated: ParsedCandidate) {
        _confirmState.update { state ->
            state.copy(
                candidates = state.candidates.map {
                    if (it.tempId == updated.tempId) updated else it
                }
            )
        }
    }

    fun saveSelectedCandidates(stageLowConfidenceToReview: Boolean = false) {
        val selected = _confirmState.value.candidates.filter { it.selected }
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val items = selected.map { cand ->
                SubscriptionItem(
                    name = cand.name,
                    amount = cand.amount,
                    currencyCode = cand.currencyCode,
                    frequency = cand.frequency,
                    category = cand.category,
                    nextBillingEpochDay = cand.nextBillingEpochDay,
                    isTrial = cand.isTrial,
                    needsReview = stageLowConfidenceToReview && cand.confidencePercent < 75,
                    confidencePercent = cand.confidencePercent,
                    sourceTag = cand.sourceTag,
                    paymentHint = cand.paymentHint,
                    notes = if (cand.confidencePercent < 75) "Low-confidence estimate — verify rate" else ""
                )
            }
            repository.addAll(items)
            _confirmState.value = ConfirmExtractionUiState()
            _pasteScrubState.value = PasteScrubUiState()
            _screenshotState.value = ScreenshotPrepUiState()
            _toastMessages.tryEmit("Saved ${items.size} item(s) to local ledger")
        }
    }

    fun discardCandidates() {
        _confirmState.value = ConfirmExtractionUiState()
    }

    // Settings & Exports
    fun setCurrencyCode(code: String) {
        viewModelScope.launch { repository.setCurrencyCode(code) }
    }

    fun setAlertLeadDays(days: Int) {
        viewModelScope.launch { repository.setAlertLeadDays(days) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setNotificationsEnabled(enabled)
            AlertScheduler.syncSchedule(appContext, enabled)
        }
    }

    fun setOnDeviceParserEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setOnDeviceParserEnabled(enabled) }
    }

    fun triggerTestNotification() {
        val state = uiState.value
        val sent = AlertScheduler.sendTestNotification(
            context = appContext,
            items = state.allItems,
            leadDays = state.preferences.alertLeadDays
        )
        _toastMessages.tryEmit(
            if (sent) "Local renewal notification sent"
            else "Enable notification permission to preview local alerts"
        )
    }

    fun buildIcsContent(leadDays: Int): String {
        return IcsGenerator.generateCalendarIcs(uiState.value.allItems, leadDays)
    }

    fun exportJsonBackupString(): String {
        val state = uiState.value
        viewModelScope.launch { repository.updateLastBackupTimestamp() }
        return JsonBackupManager.exportToJson(
            items = state.allItems,
            currencyCode = state.preferences.currencyCode,
            alertLeadDays = state.preferences.alertLeadDays
        )
    }

    fun importJsonBackup(rawJson: String) {
        viewModelScope.launch {
            JsonBackupManager.parseBackupJson(rawJson)
                .onSuccess { (payload, items) ->
                    repository.replaceAllFromBackup(items)
                    repository.setCurrencyCode(payload.currencyCode)
                    repository.setAlertLeadDays(payload.alertLeadDays)
                    repository.updateLastBackupTimestamp()
                    _toastMessages.tryEmit("Restored ${items.size} items from local backup")
                }
                .onFailure { err ->
                    _toastMessages.tryEmit("Backup import failed: ${err.message ?: "Invalid JSON"}")
                }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllSubscriptions()
            _toastMessages.tryEmit("Local ledger cleared")
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetSampleData()
            _toastMessages.tryEmit("Sample ledger restored")
        }
    }

    class Factory(
        private val repository: SubscriptionRepository,
        private val appContext: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubJoggerViewModel(repository, appContext) as T
        }
    }
}
