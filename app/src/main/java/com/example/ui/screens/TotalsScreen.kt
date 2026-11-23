package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.formatMoney
import com.example.ui.components.SummaryHeroCard
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.RokkittFontFamily
import com.example.ui.viewmodel.SubJoggerUiState

@Composable
fun TotalsScreen(
    state: SubJoggerUiState,
    onOpenCalendarExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    val summary = state.summary
    val currency = state.preferences.currencyCode
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = ledgerColors.surfaceContainerLow

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SummaryHeroCard(summary = summary, currencyCode = currency)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        label = "ACTIVE",
                        value = "${summary.activeCount}",
                        accent = ledgerColors.emeraldDeep,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        label = "TRIALS",
                        value = "${summary.trialCount}",
                        accent = ledgerColors.coral,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        label = "CANCELLED",
                        value = "${summary.cancelledCount}",
                        accent = ledgerColors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Surface(
                    color = ledgerColors.cardBackground,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ledgerColors.borderTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MONTHLY SPEND BY CATEGORY",
                            style = MaterialTheme.typography.labelMedium,
                            color = ledgerColors.textSecondary
                        )

                        if (summary.categoryBreakdowns.isEmpty()) {
                            Text(
                                text = "Add amounts to your active subscriptions to inspect category distribution.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ledgerColors.textSecondary
                            )
                        } else {
                            summary.categoryBreakdowns.forEach { entry ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${entry.category.label} (${entry.itemCount})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${formatMoney(entry.monthlyAmount, currency)} / mo",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                    ) {
                                        drawRoundRect(
                                            color = trackColor,
                                            size = size,
                                            cornerRadius = CornerRadius(99f, 99f)
                                        )
                                        drawRoundRect(
                                            color = primaryColor,
                                            size = Size(
                                                width = (size.width * entry.sharePercent.coerceIn(0.04f, 1f)),
                                                height = size.height
                                            ),
                                            cornerRadius = CornerRadius(99f, 99f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = ledgerColors.surfaceContainerLow,
                    border = BorderStroke(1.dp, ledgerColors.borderTint),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Export Calendar Reminders (.ics)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Generate an RFC 5545 .ics calendar file for Apple, Google, or Outlook Calendar offline.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ledgerColors.textSecondary
                                )
                            }
                        }
                        Button(
                            onClick = onOpenCalendarExport,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("totals_open_ics_export_button")
                        ) {
                            Text("Configure & Download .ics")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = ledgerColors.cardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ledgerColors.borderTint),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = ledgerColors.textSecondary
            )
            Text(
                text = value,
                fontFamily = RokkittFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                color = accent
            )
        }
    }
}
