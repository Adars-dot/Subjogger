package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.ZeroCloudSecurityBanner
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.SpaceMonoFontFamily
import com.example.ui.viewmodel.PasteScrubUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasteScrubScreen(
    state: PasteScrubUiState,
    onTextChange: (String) -> Unit,
    onFillSample: () -> Unit,
    onExtractClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    val scrub = state.scrubResult

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ZeroCloudSecurityBanner(
                    headline = "CLIENT-SIDE SHIELD ACTIVE • ZERO SERVER EXPOSURE",
                    detail = "Sensitive tokens like card numbers, emails, phone numbers, and order IDs are stripped on-device before parsing."
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paste Receipt, Email, or SMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        onClick = onFillSample,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("fill_sample_receipt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sample Receipt")
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = state.rawText,
                        onValueChange = onTextChange,
                        placeholder = {
                            Text("Paste renewal confirmation, app store receipt, or bank SMS here...")
                        },
                        minLines = 5,
                        maxLines = 9,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paste_receipt_input")
                    )
                    Text(
                        text = "${state.rawText.length} / 3,000 characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = ledgerColors.textSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            item {
                Surface(
                    color = ledgerColors.surfaceContainerLow,
                    border = BorderStroke(1.dp, ledgerColors.borderTint),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REAL-TIME SCRUBBED PREVIEW",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${scrub.redactedCount} token(s) masked",
                                style = MaterialTheme.typography.labelSmall,
                                color = ledgerColors.emeraldDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = ledgerColors.cardBackground,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ledgerColors.borderTint),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = scrub.scrubbedText.ifBlank {
                                    "Redacted text will appear here instantaneously as you type or paste..."
                                },
                                fontFamily = SpaceMonoFontFamily,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (scrub.scrubbedText.isBlank()) ledgerColors.textSecondary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .testTag("scrubbed_preview_text")
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AuditStatusChip("Card sequence", if (scrub.cardMasked) "Masked" else "None detected", scrub.cardMasked)
                            AuditStatusChip("Email", if (scrub.emailObfuscated) "Obfuscated" else "None detected", scrub.emailObfuscated)
                            AuditStatusChip("Phone", if (scrub.phoneRedacted) "Redacted" else "None detected", scrub.phoneRedacted)
                            AuditStatusChip("Order ID", if (scrub.idMasked) "Masked" else "None detected", scrub.idMasked)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = onExtractClick,
                    enabled = state.rawText.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("extract_from_text_button")
                ) {
                    Text("Extract Subscriptions Offline")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditStatusChip(label: String, status: String, active: Boolean) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = if (active) ledgerColors.emeraldContainer else ledgerColors.cardBackground,
        border = BorderStroke(1.dp, if (active) ledgerColors.emerald else ledgerColors.borderTint),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (active) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = ledgerColors.emeraldDeep,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = "$label: $status",
                style = MaterialTheme.typography.labelSmall,
                color = if (active) ledgerColors.emeraldDeep else ledgerColors.textSecondary,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
