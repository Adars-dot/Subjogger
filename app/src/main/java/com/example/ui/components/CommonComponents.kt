package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SubscriptionCategory
import com.example.ui.theme.LocalLedgerColors

@Composable
fun SubJoggerTopBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    showSearchAction: Boolean = false,
    onSearchClick: () -> Unit = {},
    onShieldClick: () -> Unit = {}
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = ledgerColors.emeraldDeep,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = stringResource(R.string.privacy_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = ledgerColors.textSecondary
                    )
                }
            }

            if (showSearchAction) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("search_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search subscriptions",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(1.dp, ledgerColors.borderTint, CircleShape)
                    .clickable(onClick = onShieldClick)
                    .testTag("privacy_badge_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = "View zero-cloud privacy guarantee",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChipBadge(
    category: SubscriptionCategory,
    isCancelled: Boolean = false
) {
    val ledgerColors = LocalLedgerColors.current
    val (bg, fg, label) = if (isCancelled) {
        Triple(ledgerColors.surfaceContainerLow, ledgerColors.textSecondary, "Cancelled")
    } else {
        when (category) {
            SubscriptionCategory.VIDEO -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), category.label)
            SubscriptionCategory.MUSIC -> Triple(Color(0xFFDCFCE7), Color(0xFF166534), category.label)
            SubscriptionCategory.SOFTWARE -> Triple(Color(0xFFDBEAFE), Color(0xFF1E40AF), category.label)
            SubscriptionCategory.CLOUD -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), category.label)
            SubscriptionCategory.FITNESS -> Triple(Color(0xFFFFEDD5), Color(0xFF9A3412), category.label)
            SubscriptionCategory.BILLS -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), category.label)
            SubscriptionCategory.OTHER -> Triple(ledgerColors.surfaceContainerLow, ledgerColors.textSecondary, category.label)
        }
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TrialBadgePill() {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = ledgerColors.coralContainer,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "Trial",
            style = MaterialTheme.typography.labelSmall,
            color = ledgerColors.coral,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ConfidencePill(confidencePercent: Int) {
    val ledgerColors = LocalLedgerColors.current
    val isHigh = confidencePercent >= 80
    val bg = if (isHigh) ledgerColors.emeraldContainer else ledgerColors.amberContainer
    val fg = if (isHigh) ledgerColors.emeraldDeep else ledgerColors.amber
    val text = if (isHigh) "$confidencePercent% match" else "$confidencePercent% confidence"
    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ZeroCloudSecurityBanner(
    headline: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current
    Surface(
        color = ledgerColors.emeraldContainer.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, ledgerColors.emerald.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ledgerColors.emerald.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = null,
                    tint = ledgerColors.emeraldDeep,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.labelMedium,
                    color = ledgerColors.emeraldDeep,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
