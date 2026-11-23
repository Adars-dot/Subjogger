package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SubscriptionItem
import com.example.ui.components.JogMemoryPromptCard
import com.example.ui.components.OfflineRemindersBanner
import com.example.ui.components.ReviewQueueBanner
import com.example.ui.components.SubscriptionRowCard
import com.example.ui.components.SummaryHeroCard
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.FilterTab
import com.example.ui.viewmodel.SubJoggerUiState

@Composable
fun HomeListScreen(
    state: SubJoggerUiState,
    onFilterSelected: (FilterTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onDismissBanner: () -> Unit,
    onOpenCalendarExport: () -> Unit,
    onOpenReviewQueue: () -> Unit,
    onOpenJogger: () -> Unit,
    onItemClick: (SubscriptionItem) -> Unit,
    onQuickAddAmount: (SubscriptionItem) -> Unit,
    onOpenAddSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    val summary = state.summary
    val totalConfirmedCount = summary.subscriptionsOnlyCount + summary.billsOnlyCount

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                AnimatedVisibility(visible = state.isSearchActive) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search Netflix, bills, software...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = onToggleSearch) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Close search")
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .testTag("home_search_input")
                    )
                }
            }

            if (!state.preferences.bannerDismissed) {
                item {
                    OfflineRemindersBanner(
                        onOpenRemindersClick = onOpenCalendarExport,
                        onDismiss = onDismissBanner
                    )
                }
            }

            item {
                SummaryHeroCard(
                    summary = summary,
                    currencyCode = state.preferences.currencyCode
                )
            }

            if (summary.reviewQueueCount > 0) {
                item {
                    ReviewQueueBanner(
                        count = summary.reviewQueueCount,
                        onReviewClick = onOpenReviewQueue
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FilterTab.entries) { tab ->
                        val count = when (tab) {
                            FilterTab.ALL -> totalConfirmedCount
                            FilterTab.SUBSCRIPTIONS -> summary.subscriptionsOnlyCount
                            FilterTab.BILLS -> summary.billsOnlyCount
                            FilterTab.TO_REVIEW -> summary.reviewQueueCount
                        }
                        val label = if (tab == FilterTab.TO_REVIEW && count > 0) {
                            "● ${tab.label} ($count)"
                        } else {
                            "${tab.label} ($count)"
                        }
                        val selected = state.selectedFilter == tab
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterSelected(tab) },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.upcoming_schedule_header),
                        style = MaterialTheme.typography.labelMedium,
                        color = ledgerColors.textSecondary
                    )
                    Text(
                        text = stringResource(R.string.upcoming_chronological),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (state.filteredItems.isEmpty()) {
                item {
                    Surface(
                        color = ledgerColors.cardBackground,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = ledgerColors.textSecondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = if (state.searchQuery.isNotBlank()) "No matching entries" else "No subscriptions in this view",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Add a recurring charge manually, paste a receipt, or scan a screenshot offline.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ledgerColors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = onOpenAddSheet) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("  Add subscription")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = state.filteredItems,
                    key = { it.id }
                ) { item ->
                    SubscriptionRowCard(
                        item = item,
                        onClick = { onItemClick(item) },
                        onQuickAddAmount = { onQuickAddAmount(item) }
                    )
                }
            }

            item {
                JogMemoryPromptCard(onBrowseClick = onOpenJogger)
            }
        }
    }
}
