package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CurrencyOption
import com.example.domain.DateUtils
import com.example.domain.RenewalNotificationWorker
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.SubJoggerUiState
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    state: SubJoggerUiState,
    onCurrencyChange: (String) -> Unit,
    onAlertLeadDaysChange: (Int) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onOnDeviceParserToggle: (Boolean) -> Unit,
    onTestNotification: () -> Unit,
    onOpenCalendarExport: () -> Unit,
    onExportJsonBackup: () -> String,
    onImportJsonBackup: (String) -> Unit,
    onClearAllData: () -> Unit,
    onResetSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ledgerColors = LocalLedgerColors.current
    val prefs = state.preferences
    var showClearConfirm by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onNotificationsToggle(true)
            onTestNotification()
        } else {
            onNotificationsToggle(false)
        }
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val json = onExportJsonBackup()
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader(Charsets.UTF_8).readText()
            }
            if (!json.isNullOrBlank()) {
                onImportJsonBackup(json)
            }
        }
    }

    val backupStale = prefs.lastBackupEpochMs == 0L ||
        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - prefs.lastBackupEpochMs) >= 14L

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
                            text = "GENERAL PREFERENCES",
                            style = MaterialTheme.typography.labelMedium,
                            color = ledgerColors.textSecondary
                        )

                        Text(
                            text = "Base Currency",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CurrencyOption.entries.forEach { option ->
                                FilterChip(
                                    selected = prefs.currencyCode == option.code,
                                    onClick = { onCurrencyChange(option.code) },
                                    label = { Text(option.displayName) },
                                    modifier = Modifier.testTag("currency_chip_${option.code.lowercase()}")
                                )
                            }
                        }

                        Text(
                            text = "Renewal Alert Window",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column {
                            listOf(1 to "1 day before", 3 to "3 days before (Default)", 7 to "7 days before").forEach { (days, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAlertLeadDaysChange(days) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = prefs.alertLeadDays == days,
                                        onClick = { onAlertLeadDaysChange(days) }
                                    )
                                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Local Renewal Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Daily offline WorkManager check — zero server push.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ledgerColors.textSecondary
                                )
                            }
                            Switch(
                                checked = prefs.notificationsEnabled,
                                onCheckedChange = { wantEnabled ->
                                    if (wantEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !RenewalNotificationWorker.hasNotificationPermission(context)
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onNotificationsToggle(wantEnabled)
                                    }
                                },
                                modifier = Modifier.testTag("notifications_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        !RenewalNotificationWorker.hasNotificationPermission(context)
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onTestNotification()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_notification_button")
                            ) {
                                Icon(imageVector = Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Preview Alert")
                            }
                            OutlinedButton(
                                onClick = onOpenCalendarExport,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_ics_export_button")
                            ) {
                                Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(".ics Export")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "On-Device Text & OCR Reader",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Surface(
                                        color = ledgerColors.emeraldContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Offline",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ledgerColors.emeraldDeep,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Regex scrubbing + bundled ML Kit OCR stay strictly in memory.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ledgerColors.textSecondary
                                )
                            }
                            Switch(
                                checked = prefs.onDeviceParserEnabled,
                                onCheckedChange = onOnDeviceParserToggle
                            )
                        }
                    }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BACKUP & DATA PORTABILITY",
                                style = MaterialTheme.typography.labelMedium,
                                color = ledgerColors.textSecondary
                            )
                            Text(
                                text = DateUtils.formatRelativeBackupAge(prefs.lastBackupEpochMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = ledgerColors.textSecondary
                            )
                        }

                        if (backupStale && state.allItems.isNotEmpty()) {
                            Surface(
                                color = ledgerColors.amberContainer,
                                border = BorderStroke(1.dp, ledgerColors.amber.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WarningAmber,
                                        contentDescription = null,
                                        tint = ledgerColors.amber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "No backup in the last 14 days — You have ${state.allItems.size} items tracked locally. Since no data touches any server, exporting a JSON backup protects your ledger.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    createBackupLauncher.launch("subjogger-backup.json")
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("export_json_button")
                            ) {
                                Icon(imageVector = Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export JSON")
                            }
                            OutlinedButton(
                                onClick = {
                                    openBackupLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("import_json_button")
                            ) {
                                Icon(imageVector = Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onResetSampleData,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_sample_button")
                            ) {
                                Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Sample")
                            }
                            OutlinedButton(
                                onClick = { showClearConfirm = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ledgerColors.coral),
                                border = BorderStroke(1.dp, ledgerColors.coral.copy(alpha = 0.45f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clear_ledger_button")
                            ) {
                                Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Ledger")
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    color = ledgerColors.surfaceContainerLow,
                    border = BorderStroke(1.dp, ledgerColors.borderTint),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = ledgerColors.emeraldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "All data is stored exclusively in your device storage (Room SQLite & DataStore). Zero cloud servers or analytics.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ledgerColors.textSecondary
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = {
                Icon(imageVector = Icons.Filled.Backup, contentDescription = null, tint = ledgerColors.coral)
            },
            title = { Text("Clear all local subscriptions?") },
            text = {
                Text("This permanently deletes all ${state.allItems.size} items from your local Room database. You can re-seed sample items anytime.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ledgerColors.coral)
                ) {
                    Text("Clear Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
