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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ParsedCandidate
import com.example.data.model.formatMoney
import com.example.domain.DateUtils
import com.example.ui.components.CategoryChipBadge
import com.example.ui.components.ConfidencePill
import com.example.ui.components.TrialBadgePill
import com.example.ui.components.ZeroCloudSecurityBanner
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.RokkittFontFamily
import com.example.ui.viewmodel.ConfirmExtractionUiState

@Composable
fun ConfirmExtractionScreen(
    state: ConfirmExtractionUiState,
    onToggleSelect: (String) -> Unit,
    onUpdateCandidate: (ParsedCandidate) -> Unit,
    onSaveSelected: (stageLowConfidence: Boolean) -> Unit,
    onDiscardAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    val selectedCount = state.candidates.count { it.selected }
    val hasLowConfidence = state.candidates.any { it.selected && it.confidencePercent < 75 }
    var editingCandidate by remember { mutableStateOf<ParsedCandidate?>(null) }

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
                    headline = "ON-DEVICE READOUT COMPLETE • ZERO DATA STORED ON SERVER",
                    detail = "Source: ${state.sourceLabel}. Untick any entry that looks incorrect before saving."
                )
            }

            item {
                Text(
                    text = "Found ${state.candidates.size} subscription candidate(s)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(state.candidates, key = { it.tempId }) { candidate ->
                val isLowConf = candidate.confidencePercent < 75
                Surface(
                    color = ledgerColors.cardBackground,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isLowConf) ledgerColors.amber.copy(alpha = 0.6f) else ledgerColors.borderTint
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = candidate.selected,
                            onCheckedChange = { onToggleSelect(candidate.tempId) },
                            modifier = Modifier.testTag("candidate_checkbox_${candidate.tempId}")
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = candidate.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                ConfidencePill(candidate.confidencePercent)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CategoryChipBadge(candidate.category)
                                if (candidate.isTrial) TrialBadgePill()
                                if (candidate.paymentHint.isNotBlank()) {
                                    Text(
                                        text = candidate.paymentHint,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ledgerColors.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = "Next Charge: ${DateUtils.formatMediumDate(candidate.nextBillingEpochDay)}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ledgerColors.textSecondary
                            )
                            if (isLowConf) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WarningAmber,
                                        contentDescription = null,
                                        tint = ledgerColors.amber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Estimated rate — verify before confirming",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ledgerColors.amber
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatMoney(candidate.amount, candidate.currencyCode),
                                fontFamily = RokkittFontFamily,
                                fontSize = 20.sp
                            )
                            Text(
                                text = "/ ${candidate.frequency.shortLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ledgerColors.textSecondary
                            )
                            IconButton(
                                onClick = { editingCandidate = candidate },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "Edit ${candidate.name}",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onSaveSelected(false) },
                        enabled = selectedCount > 0,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_save_candidates_button")
                    ) {
                        Text("Save $selectedCount Subscription(s) to My List")
                    }

                    if (hasLowConfidence) {
                        OutlinedButton(
                            onClick = { onSaveSelected(true) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Save & Stage Low-Confidence Items to Review")
                        }
                    }

                    TextButton(
                        onClick = onDiscardAll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("discard_candidates_button")
                    ) {
                        Text("Discard All", color = ledgerColors.coral)
                    }
                }
            }
        }
    }

    editingCandidate?.let { cand ->
        var editName by remember(cand) { mutableStateOf(cand.name) }
        var editAmt by remember(cand) {
            mutableStateOf(cand.amount?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "")
        }
        AlertDialog(
            onDismissRequest = { editingCandidate = null },
            title = { Text("Adjust Detected Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editAmt,
                        onValueChange = { editAmt = it },
                        label = { Text("Amount (${cand.currencyCode})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateCandidate(
                        cand.copy(
                            name = editName.trim().ifBlank { cand.name },
                            amount = editAmt.toDoubleOrNull() ?: cand.amount,
                            confidencePercent = 100
                        )
                    )
                    editingCandidate = null
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
