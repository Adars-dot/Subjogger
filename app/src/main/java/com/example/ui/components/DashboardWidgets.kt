package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SubscriptionCategory
import com.example.data.model.SubscriptionItem
import com.example.data.model.formatMoney
import com.example.domain.DateUtils
import com.example.domain.LedgerSummary
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.theme.RokkittFontFamily

@Composable
fun OfflineRemindersBanner(
    onOpenRemindersClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(ledgerColors.heroGradient)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFFA7F3D0),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.banner_offline_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            color = Color(0xFF065F46),
                            shape = RoundedCornerShape(99.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.banner_offline_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA7F3D0),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.banner_offline_subline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dismiss_offline_banner_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss offline reminder banner",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onOpenRemindersClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E3A8A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("banner_reminders_button")
                ) {
                    Text(
                        text = "Sync .ics & Alerts",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryHeroCard(
    summary: LedgerSummary,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = ledgerColors.cardBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ledgerColors.borderTint),
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.hero_label_run_rate),
                    style = MaterialTheme.typography.labelMedium,
                    color = ledgerColors.textSecondary
                )
                Surface(
                    color = ledgerColors.emeraldContainer,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "● ${stringResource(R.string.hero_on_budget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ledgerColors.emeraldDeep,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatMoney(summary.monthlyRunRate, currencyCode),
                    fontFamily = RokkittFontFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 38.sp,
                    lineHeight = 42.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ month",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ledgerColors.textSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = ledgerColors.surfaceContainerLow,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.hero_yearly_equivalent),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ledgerColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatMoney(summary.yearlyEquivalent, currencyCode),
                            fontFamily = RokkittFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(R.string.hero_next_30_days),
                            style = MaterialTheme.typography.bodyMedium,
                            color = ledgerColors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${summary.next30DaysCount} charges (${formatMoney(summary.next30DaysTotal, currencyCode)})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewQueueBanner(
    count: Int,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = Color(0xFFE0F2FE),
        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onReviewClick)
            .testTag("review_queue_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.HelpOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (count == 1) "1 item to confirm" else "$count items to confirm",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Recent entries need review",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155)
                )
            }
            TextButton(
                onClick = onReviewClick,
                modifier = Modifier.testTag("review_queue_action_button")
            ) {
                Text(
                    text = "${stringResource(R.string.review_banner_cta)} →",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF1E3A8A),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SubscriptionRowCard(
    item: SubscriptionItem,
    onClick: () -> Unit,
    onQuickAddAmount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    val daysLeft = DateUtils.daysUntil(item.nextBillingEpochDay)
    val isUrgent = !item.isCancelled && daysLeft in 0L..7L

    Surface(
        color = ledgerColors.cardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isUrgent) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else ledgerColors.borderTint
        ),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("subscription_card_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (item.isCancelled) ledgerColors.surfaceContainerLow
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon(item.category),
                    contentDescription = null,
                    tint = if (item.isCancelled) ledgerColors.textSecondary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (item.isCancelled) ledgerColors.textSecondary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isTrial && !item.isCancelled) {
                        TrialBadgePill()
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryChipBadge(category = item.category, isCancelled = item.isCancelled)
                    val countdownText = DateUtils.formatCountdown(item.nextBillingEpochDay, item.isCancelled)
                    val displayCountdown = if (isUrgent) "● $countdownText" else countdownText
                    Text(
                        text = displayCountdown,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isUrgent) ledgerColors.emeraldDeep else ledgerColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (item.amount == null) {
                OutlinedButton(
                    onClick = onQuickAddAmount,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_amount_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Add amount",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMoney(item.amount, item.currencyCode),
                        fontFamily = RokkittFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = if (item.isCancelled) ledgerColors.textSecondary else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (item.isCancelled) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "/ ${item.frequency.shortLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ledgerColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun JogMemoryPromptCard(
    onBrowseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = ledgerColors.surfaceContainerLow,
        border = BorderStroke(1.dp, ledgerColors.borderTint),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.jog_memory_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.jog_memory_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ledgerColors.textSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBrowseClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("browse_common_picks_button")
                ) {
                    Text(
                        text = stringResource(R.string.jog_memory_cta),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

fun categoryIcon(category: SubscriptionCategory): ImageVector = when (category) {
    SubscriptionCategory.VIDEO -> Icons.Filled.Movie
    SubscriptionCategory.MUSIC -> Icons.Filled.MusicNote
    SubscriptionCategory.SOFTWARE -> Icons.Filled.Code
    SubscriptionCategory.CLOUD -> Icons.Filled.CloudQueue
    SubscriptionCategory.FITNESS -> Icons.Filled.FitnessCenter
    SubscriptionCategory.BILLS -> Icons.Filled.ReceiptLong
    SubscriptionCategory.OTHER -> Icons.Filled.Repeat
}
