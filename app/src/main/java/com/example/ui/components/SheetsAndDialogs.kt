package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BillingFrequency
import com.example.data.model.CurrencyOption
import com.example.data.model.SourceTag
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem
import com.example.domain.DateUtils
import com.example.ui.theme.LocalLedgerColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionSheet(
    onDismiss: () -> Unit,
    onPasteTextClick: () -> Unit,
    onUploadScreenshotClick: () -> Unit,
    onManualEntryClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ledgerColors = LocalLedgerColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Subscription or Bill",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose how to capture recurring charges privately on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = ledgerColors.textSecondary
            )

            CaptureOptionCard(
                icon = Icons.Filled.ContentPaste,
                title = "Paste text",
                badge = "Fastest",
                description = "Copy an email receipt, renewal alert, or SMS. Sensitive PANs, emails & IDs are scrubbed on-device.",
                testTag = "capture_option_paste",
                onClick = {
                    onDismiss()
                    onPasteTextClick()
                }
            )

            CaptureOptionCard(
                icon = Icons.Filled.Image,
                title = "Upload screenshot",
                badge = "Offline OCR",
                description = "Pick a screenshot of your store subscriptions. EXIF stripped, cropped & read locally with ML Kit.",
                testTag = "capture_option_screenshot",
                onClick = {
                    onDismiss()
                    onUploadScreenshotClick()
                }
            )

            CaptureOptionCard(
                icon = Icons.Filled.Edit,
                title = "Type it in manually",
                badge = null,
                description = "Enter service name, amount, and renewal frequency step-by-step yourself.",
                testTag = "capture_option_manual",
                onClick = {
                    onDismiss()
                    onManualEntryClick()
                }
            )

            Surface(
                color = ledgerColors.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = ledgerColors.emeraldDeep,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Zero bank credentials needed. Texts and images are processed locally and never stored on a server.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ledgerColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptureOptionCard(
    icon: ImageVector,
    title: String,
    badge: String?,
    description: String,
    testTag: String,
    onClick: () -> Unit
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = ledgerColors.cardBackground,
        border = BorderStroke(1.dp, ledgerColors.borderTint),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Surface(
                            color = ledgerColors.emeraldContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = ledgerColors.emeraldDeep,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ledgerColors.textSecondary
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = ledgerColors.textSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditSubscriptionSheet(
    initialItem: SubscriptionItem?,
    defaultCurrencyCode: String,
    onDismiss: () -> Unit,
    onSave: (SubscriptionItem) -> Unit,
    onDelete: ((Long, String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ledgerColors = LocalLedgerColors.current
    val isNew = initialItem == null || initialItem.id == 0L

    var name by remember(initialItem) { mutableStateOf(initialItem?.name ?: "") }
    var amountText by remember(initialItem) {
        mutableStateOf(initialItem?.amount?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "")
    }
    var frequency by remember(initialItem) { mutableStateOf(initialItem?.frequency ?: BillingFrequency.MONTHLY) }
    var category by remember(initialItem) { mutableStateOf(initialItem?.category ?: SubscriptionCategory.SOFTWARE) }
    var epochDay by remember(initialItem) {
        mutableStateOf(initialItem?.nextBillingEpochDay ?: (DateUtils.todayEpochDay() + 14L))
    }
    var isTrial by remember(initialItem) { mutableStateOf(initialItem?.isTrial ?: false) }
    var isCancelled by remember(initialItem) { mutableStateOf(initialItem?.isCancelled ?: false) }
    var needsReview by remember(initialItem) { mutableStateOf(initialItem?.needsReview ?: false) }
    var notes by remember(initialItem) { mutableStateOf(initialItem?.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val currency = CurrencyOption.fromCode(initialItem?.currencyCode ?: defaultCurrencyCode)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isNew) "New Subscription" else "Edit ${initialItem?.name ?: "Item"}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Service or bill name") },
                placeholder = { Text("e.g., Netflix, iCloud+, Rent") },
                isError = nameError,
                supportingText = if (nameError) { { Text("Enter a name") } } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_name_input")
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("""^\d{0,6}(\.\d{0,2})?$"""))) {
                        amountText = input
                    }
                },
                label = { Text("Amount (${currency.code}) — optional") },
                prefix = { Text(currency.symbol) },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_amount_input")
            )

            Text(
                text = "Billing Cycle",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BillingFrequency.entries.forEach { freq ->
                    FilterChip(
                        selected = frequency == freq,
                        onClick = { frequency = freq },
                        label = { Text(freq.label) },
                        modifier = Modifier.testTag("freq_chip_${freq.name.lowercase()}")
                    )
                }
            }

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SubscriptionCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.label) }
                    )
                }
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_date_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Next Billing Date: ${DateUtils.formatMediumDate(epochDay)}")
            }

            Surface(
                color = ledgerColors.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Free trial / intro window", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = isTrial, onCheckedChange = { isTrial = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Stage in Review Queue", style = MaterialTheme.typography.bodyLarge)
                        Switch(checked = needsReview, onCheckedChange = { needsReview = it })
                    }
                    if (!isNew) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mark as Cancelled", style = MaterialTheme.typography.bodyLarge)
                            Switch(checked = isCancelled, onCheckedChange = { isCancelled = it })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isNew && onDelete != null && initialItem != null) {
                    OutlinedButton(
                        onClick = {
                            onDelete(initialItem.id, initialItem.name)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ledgerColors.coral),
                        border = BorderStroke(1.dp, ledgerColors.coral.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("delete_subscription_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "Delete subscription",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                            return@Button
                        }
                        val parsedAmount = amountText.trim().toDoubleOrNull()
                        val result = SubscriptionItem(
                            id = initialItem?.id ?: 0L,
                            name = name.trim(),
                            amount = parsedAmount,
                            currencyCode = currency.code,
                            frequency = frequency,
                            category = category,
                            nextBillingEpochDay = epochDay,
                            isTrial = isTrial,
                            isCancelled = isCancelled,
                            needsReview = needsReview,
                            confidencePercent = 100,
                            sourceTag = initialItem?.sourceTag ?: SourceTag.MANUAL,
                            paymentHint = initialItem?.paymentHint ?: "",
                            notes = notes.trim()
                        )
                        onSave(result)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("save_subscription_button")
                ) {
                    Text(if (isNew) "Save to My List" else "Update Item")
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateUtils.epochDayToUtcPickerMillis(epochDay)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        epochDay = DateUtils.millisToEpochDay(millis)
                    }
                    showDatePicker = false
                }) {
                    Text("Set Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
fun PrivacyPromiseDialog(onDismiss: () -> Unit) {
    val ledgerColors = LocalLedgerColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ledgerColors.emeraldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = ledgerColors.emeraldDeep,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Zero-Cloud Privacy Guarantee",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SubJogger has zero internet permissions, no remote database, and no user tracking.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Ledger stored in device-local Room SQLite\n• Regex PII scrubbing masks PANs, emails & IDs locally\n• Screenshot OCR runs offline via bundled ML Kit\n• Reminders sync via standard .ics files & WorkManager",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ledgerColors.textSecondary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}
