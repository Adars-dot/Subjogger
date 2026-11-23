package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.CropBounds
import com.example.ui.components.ZeroCloudSecurityBanner
import com.example.ui.theme.LocalLedgerColors
import com.example.ui.viewmodel.ScreenshotPrepUiState

@Composable
fun ScreenshotPrepScreen(
    state: ScreenshotPrepUiState,
    onScreenshotPicked: (Uri) -> Unit,
    onCropChange: (CropBounds) -> Unit,
    onRunOcrClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ledgerColors = LocalLedgerColors.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onScreenshotPicked(uri)
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
                    headline = "ZERO CLOUD EXPOSURE PRE-CROP",
                    detail = "EXIF, GPS & device metadata are stripped via clean PNG-24 canvas re-encoding before offline ML Kit text recognition."
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pick_screenshot_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (state.selectedUri == null) "Select Screenshot (Zero-Permission Photo Picker)"
                        else "Choose Different Screenshot"
                    )
                }
            }

            if (state.selectedUri != null) {
                item {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Crop,
                                    contentDescription = null,
                                    tint = Color(0xFFA7F3D0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Drag inside frame to adjust crop area",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                            ) {
                                AsyncImage(
                                    model = state.selectedUri,
                                    contentDescription = "Selected subscription screenshot",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(state.cropBounds) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                val dx = dragAmount.x / size.width
                                                val dy = dragAmount.y / size.height
                                                val widthFrac = state.cropBounds.rightFraction - state.cropBounds.leftFraction
                                                val heightFrac = state.cropBounds.bottomFraction - state.cropBounds.topFraction
                                                val newLeft = (state.cropBounds.leftFraction + dx).coerceIn(0f, 1f - widthFrac)
                                                val newTop = (state.cropBounds.topFraction + dy).coerceIn(0f, 1f - heightFrac)
                                                onCropChange(
                                                    CropBounds(
                                                        leftFraction = newLeft,
                                                        topFraction = newTop,
                                                        rightFraction = newLeft + widthFrac,
                                                        bottomFraction = newTop + heightFrac
                                                    )
                                                )
                                            }
                                        }
                                ) {
                                    val left = state.cropBounds.leftFraction * size.width
                                    val top = state.cropBounds.topFraction * size.height
                                    val right = state.cropBounds.rightFraction * size.width
                                    val bottom = state.cropBounds.bottomFraction * size.height
                                    val rectW = right - left
                                    val rectH = bottom - top

                                    // Dim outside crop bounds
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        topLeft = Offset.Zero,
                                        size = Size(size.width, top)
                                    )
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        topLeft = Offset(0f, bottom),
                                        size = Size(size.width, size.height - bottom)
                                    )
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        topLeft = Offset(0f, top),
                                        size = Size(left, rectH)
                                    )
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        topLeft = Offset(right, top),
                                        size = Size(size.width - right, rectH)
                                    )

                                    // Crop frame border
                                    drawRect(
                                        color = Color(0xFF10B981),
                                        topLeft = Offset(left, top),
                                        size = Size(rectW, rectH),
                                        style = Stroke(width = 4f)
                                    )

                                    // Corner handles
                                    val handleRadius = 11f
                                    listOf(
                                        Offset(left, top),
                                        Offset(right, top),
                                        Offset(left, bottom),
                                        Offset(right, bottom)
                                    ).forEach { corner ->
                                        drawCircle(
                                            color = Color.White,
                                            radius = handleRadius,
                                            center = corner
                                        )
                                        drawCircle(
                                            color = Color(0xFF10B981),
                                            radius = handleRadius - 3f,
                                            center = corner
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onCropChange(CropBounds(0.04f, 0.05f, 0.96f, 0.95f)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Full Frame", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { onCropChange(CropBounds(0.06f, 0.16f, 0.94f, 0.78f)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Center Focus", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            item {
                val metrics = state.previewMetrics
                Surface(
                    color = ledgerColors.surfaceContainerLow,
                    border = BorderStroke(1.dp, ledgerColors.borderTint),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = ledgerColors.emeraldDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "ON-DEVICE PIPELINE AUDIT",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AuditMetricRow("EXIF, Geotags & Device Serial", "Stripped (0 bytes)")
                        AuditMetricRow(
                            "Downscaled Bounds",
                            if (metrics != null) "${metrics.originalWidth}×${metrics.originalHeight} → ${metrics.sanitizedWidth}×${metrics.sanitizedHeight} px"
                            else "Max 1080×1600 px"
                        )
                        AuditMetricRow(
                            "Payload Compression",
                            if (metrics != null) "${metrics.originalKb} KB → ${metrics.sanitizedKb} KB"
                            else "In-memory PNG buffer"
                        )
                        AuditMetricRow("Local Canvas Sanitization", "Clean PNG-24 Buffer • Offline ML Kit")
                    }
                }
            }

            if (state.errorMessage != null) {
                item {
                    Surface(
                        color = ledgerColors.coralContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ledgerColors.coral,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onRunOcrClick,
                    enabled = state.selectedUri != null && !state.isProcessingOcr,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("run_offline_ocr_button")
                ) {
                    if (state.isProcessingOcr) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Running On-Device ML Kit OCR...")
                    } else {
                        Text("Read Screenshot with Offline OCR")
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
}

@Composable
private fun AuditMetricRow(label: String, value: String) {
    val ledgerColors = LocalLedgerColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ledgerColors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = ledgerColors.emeraldDeep,
            fontWeight = FontWeight.Bold
        )
    }
}
