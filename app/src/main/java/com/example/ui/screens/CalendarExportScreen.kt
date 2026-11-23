package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.model.SubscriptionItem
import com.example.ui.components.ZeroCloudSecurityBanner
import com.example.ui.theme.LocalLedgerColors
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarExportScreen(
    items: List<SubscriptionItem>,
    defaultLeadDays: Int,
    onGenerateIcs: (Int) -> String,
    onSavedFeedback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ledgerColors = LocalLedgerColors.current
    var leadDays by remember(defaultLeadDays) { mutableIntStateOf(defaultLeadDays) }
    val activeItems = items.filter { !it.isCancelled }

    val saveIcsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/calendar")
    ) { uri ->
        if (uri != null) {
            val ics = onGenerateIcs(leadDays)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(ics.toByteArray(Charsets.UTF_8))
            }
            onSavedFeedback("Saved subjogger-reminders.ics (${activeItems.size} events)")
        }
    }

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
                    headline = "OFFLINE RFC 5545 CALENDAR EXPORT",
                    detail = "Never miss a cancellation window or surprise renewal charge. Imports into Google Calendar, Apple Calendar, or Outlook with zero backend sync."
                )
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Included Active Renewals (${activeItems.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (activeItems.isEmpty()) {
                            Text(
                                text = "No active subscriptions to export yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ledgerColors.textSecondary
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeItems.forEach { sub ->
                                    Surface(
                                        color = ledgerColors.surfaceContainerLow,
                                        border = BorderStroke(1.dp, ledgerColors.borderTint),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = sub.name,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
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
                    color = ledgerColors.cardBackground,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, ledgerColors.borderTint),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Lead Alert Notice (VALARM Trigger)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        listOf(
                            1 to "1 day before",
                            3 to "3 days before (Recommended for trial cancellations)",
                            7 to "7 days before"
                        ).forEach { (days, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { leadDays = days }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = leadDays == days,
                                    onClick = { leadDays = days }
                                )
                                Text(text = label, style = MaterialTheme.typography.bodyLarge)
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
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "EXPORT SPECIFICATIONS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Standard: iCalendar v2.0 (RFC 5545) • .ics\n• Recurrence: RRULE MONTHLY / YEARLY / WEEKLY\n• Alarm: VALARM TRIGGER:-P${leadDays}D\n• Transmission: 100% local file generation",
                            style = MaterialTheme.typography.labelMedium,
                            color = ledgerColors.textSecondary
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { saveIcsLauncher.launch("subjogger-reminders.ics") },
                        enabled = activeItems.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("download_ics_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download .ics Calendar File")
                    }

                    OutlinedButton(
                        onClick = {
                            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                            val icsFile = File(exportDir, "subjogger-reminders.ics")
                            icsFile.writeText(onGenerateIcs(leadDays), Charsets.UTF_8)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                icsFile
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/calendar"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "SubJogger Renewal Reminders")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Open or Share .ics Calendar"))
                        },
                        enabled = activeItems.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("share_ics_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open / Share to Calendar App")
                    }
                }
            }
        }
    }
}
