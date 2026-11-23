package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionItem
import com.example.data.model.formatMoney
import com.example.domain.DateUtils
import com.example.ui.components.CategoryChipBadge
import com.example.ui.components.ConfidencePill
import com.example.ui.components.TrialBadgePill
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.RokkittFontFamily

@Composable
fun ReviewQueueScreen(
    items: List<SubscriptionItem>,
    onConfirmItem: (SubscriptionItem) -> Unit,
    onEditItem: (SubscriptionItem) -> Unit,
    onDeleteItem: (SubscriptionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (items.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircleOutline,
                    contentDescription = null,
                    tint = ledgerColors.emeraldDeep,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Review Queue is Clear",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All staged items have been confirmed or removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ledgerColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Confirm estimated amounts and renewal dates before promoting these items into your active monthly run-rate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ledgerColors.textSecondary
                    )
                }

                items(items, key = { it.id }) { item ->
                    Surface(
                        color = ledgerColors.cardBackground,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, ledgerColors.borderTint),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        CategoryChipBadge(item.category)
                                        if (item.isTrial) TrialBadgePill()
                                        ConfidencePill(item.confidencePercent)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatMoney(item.amount, item.currencyCode),
                                        fontFamily = RokkittFontFamily,
                                        fontSize = 22.sp
                                    )
                                    Text(
                                        text = "/ ${item.frequency.shortLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ledgerColors.textSecondary
                                    )
                                }
                            }

                            Text(
                                text = DateUtils.formatCountdown(item.nextBillingEpochDay, item.isCancelled),
                                style = MaterialTheme.typography.labelMedium,
                                color = ledgerColors.textSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onConfirmItem(item) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_review_item_${item.id}")
                                ) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Confirm")
                                }
                                OutlinedButton(
                                    onClick = { onEditItem(item) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("edit_review_item_${item.id}")
                                ) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                }
                                OutlinedButton(
                                    onClick = { onDeleteItem(item) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("delete_review_item_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = "Discard",
                                        tint = ledgerColors.coral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
