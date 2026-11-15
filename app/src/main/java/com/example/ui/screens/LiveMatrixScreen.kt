package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled. CropSquare
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ImouCameraEntity
import com.example.data.local.PtzPresetEntity
import com.example.network.PtzDirection
import com.example.network.RtspProbeResult
import com.example.ui.ImouHudUiState
import com.example.ui.MatrixLayoutMode
import com.example.ui.components.PtzJoystickConsole
import com.example.ui.components.RtspCameraViewport
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonContainer
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.LanEmeraldContainer
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary

@Composable
fun LiveMatrixScreen(
    cameras: List<ImouCameraEntity>,
    selectedCamera: ImouCameraEntity?,
    presetsForSelected: List<PtzPresetEntity>,
    hudState: ImouHudUiState,
    onSelectCamera: (Long) -> Unit,
    onToggleLayoutMode: () -> Unit,
    onAddCameraClick: () -> Unit,
    onEditCameraClick: (ImouCameraEntity) -> Unit,
    onToggleStreamQuality: (ImouCameraEntity) -> Unit,
    onToggleAudio: (Long) -> Unit,
    onCaptureSnapshot: (ImouCameraEntity) -> Unit,
    onToggleRecording: (ImouCameraEntity) -> Unit,
    onProbeCamera: (ImouCameraEntity) -> Unit,
    onPinchZoom: (ImouCameraEntity, Float) -> Unit,
    onPtzDirection: (ImouCameraEntity, PtzDirection) -> Unit,
    onCenterPtz: (ImouCameraEntity) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onSelectPreset: (ImouCameraEntity, PtzPresetEntity) -> Unit,
    onAddPresetRequest: (ImouCameraEntity) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onNightVisionChange: (ImouCameraEntity, String) -> Unit,
    onToggleSpotlight: (ImouCameraEntity) -> Unit,
    onToggleSiren: (ImouCameraEntity) -> Unit,
    onToggleAllSirens: () -> Unit,
    onTogglePrivacyMask: (ImouCameraEntity) -> Unit,
    onToggleFlip180: (ImouCameraEntity) -> Unit,
    onToggleSmartTracking: (ImouCameraEntity) -> Unit,
    onRebootCamera: (ImouCameraEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val anySirenActive = cameras.any { it.sirenActive }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tactical Header & Layout Switcher Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(LanEmerald)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OFF-GRID LAN • ${hudState.networkInfo.subnetPrefix}.0/24",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFamily,
                        color = LanEmerald
                    )
                }
                Text(
                    text = "Imou LAN Matrix",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPureWhite
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Emergency All-Camera Siren Button
                Surface(
                    color = if (anySirenActive) AlertCrimson else AlertCrimsonContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertCrimson),
                    modifier = Modifier
                        .height(48.dp)
                        .clickable { onToggleAllSirens() }
                        .testTag("btn_all_siren")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Emergency Siren",
                            tint = if (anySirenActive) PitchBlack else AlertCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (anySirenActive) "STOP" else "SOS",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (anySirenActive) PitchBlack else AlertCrimson
                        )
                    }
                }

                // Quad 2x2 vs Single 1x1 Switcher
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .height(48.dp)
                        .clickable { onToggleLayoutMode() }
                        .testTag("btn_toggle_matrix_layout")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hudState.layoutMode == MatrixLayoutMode.QUAD_2X2) {
                                Icons.Default.CropSquare
                            } else {
                                Icons.Default.GridView
                            },
                            contentDescription = "Toggle Matrix View",
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hudState.layoutMode == MatrixLayoutMode.QUAD_2X2) "1x1 ভিউ" else "2x2 গ্রিড",
                            style = MaterialTheme.typography.labelLarge,
                            color = CyberCyan
                        )
                    }
                }

                // Add Camera Button
                IconButton(
                    onClick = onAddCameraClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan)
                        .testTag("btn_add_camera_top")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Camera",
                        tint = PitchBlack
                    )
                }
            }
        }

        // 2. Camera Selector Pills Row
        if (cameras.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cameras.forEach { cam ->
                    val isSelected = cam.id == selectedCamera?.id
                    val probe = hudState.probeResults[cam.id]
                    Surface(
                        color = if (isSelected) CyberCyanContainer else ObsidianSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) CyberCyan else GlassBorder
                        ),
                        modifier = Modifier
                            .clickable { onSelectCamera(cam.id) }
                            .testTag("camera_chip_${cam.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (probe?.rtspPortOpen == true) LanEmerald else CyberCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cam.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) CyberCyan else TextPureWhite,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Multi-Camera Quad Matrix (2x2) or Single 1x1 Viewport
        if (cameras.isEmpty()) {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "কোনো Imou LAN ক্যামেরা যুক্ত করা নেই",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPureWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onAddCameraClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = PitchBlack)
                    ) {
                        Text("প্রথম ক্যামেরা যুক্ত করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (hudState.layoutMode == MatrixLayoutMode.QUAD_2X2 && cameras.size >= 2) {
            val quadCameras = cameras.take(4)
            val rows = quadCameras.chunked(2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rows.forEach { rowCameras ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCameras.forEach { cam ->
                            RtspCameraViewport(
                                camera = cam,
                                probeResult = hudState.probeResults[cam.id],
                                isSelected = cam.id == selectedCamera?.id,
                                isRecording = hudState.recordingCameraIds.contains(cam.id),
                                isAudioMuted = hudState.mutedCameraIds.contains(cam.id),
                                compactMode = true,
                                onSelectCamera = { onSelectCamera(cam.id) },
                                onToggleStreamQuality = { onToggleStreamQuality(cam) },
                                onToggleAudio = { onToggleAudio(cam.id) },
                                onCaptureSnapshot = { onCaptureSnapshot(cam) },
                                onToggleRecording = { onToggleRecording(cam) },
                                onReconnectRtsp = { onProbeCamera(cam) },
                                onPinchZoomChange = { z -> onPinchZoom(cam, z) },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(16f / 10f)
                            )
                        }
                        if (rowCameras.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else if (selectedCamera != null) {
            RtspCameraViewport(
                camera = selectedCamera,
                probeResult = hudState.probeResults[selectedCamera.id],
                isSelected = true,
                isRecording = hudState.recordingCameraIds.contains(selectedCamera.id),
                isAudioMuted = hudState.mutedCameraIds.contains(selectedCamera.id),
                compactMode = false,
                onSelectCamera = {},
                onToggleStreamQuality = { onToggleStreamQuality(selectedCamera) },
                onToggleAudio = { onToggleAudio(selectedCamera.id) },
                onCaptureSnapshot = { onCaptureSnapshot(selectedCamera) },
                onToggleRecording = { onToggleRecording(selectedCamera) },
                onReconnectRtsp = { onProbeCamera(selectedCamera) },
                onPinchZoomChange = { z -> onPinchZoom(selectedCamera, z) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9.5f)
            )
        }

        // 4. Active Camera Quick Command & RTSP Deck
        if (selectedCamera != null) {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${selectedCamera.name} • ${selectedCamera.modelName}",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedCamera.buildMaskedRtspUrl(),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFamily,
                                color = CyberCyan,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row {
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(selectedCamera.buildRtspUrl()))
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy RTSP URL",
                                    tint = CyberCyan
                                )
                            }
                            IconButton(
                                onClick = { onEditCameraClick(selectedCamera) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_edit_selected_camera")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Camera",
                                    tint = TextPureWhite
                                )
                            }
                        }
                    }

                    // Action Strip Buttons (Snapshot, Record, Stream Quality, Ping Check)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isRec = hudState.recordingCameraIds.contains(selectedCamera.id)
                        OutlinedButton(
                            onClick = { onCaptureSnapshot(selectedCamera) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPureWhite),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_btn_snapshot")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("স্ন্যাপশট", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { onToggleRecording(selectedCamera) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isRec) AlertCrimson else TextPureWhite
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_btn_record")
                        ) {
                            Icon(
                                Icons.Default.FiberManualRecord,
                                contentDescription = null,
                                tint = if (isRec) AlertCrimson else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isRec) "REC স্টপ" else "রেকর্ড", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { onToggleStreamQuality(selectedCamera) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_btn_quality")
                        ) {
                            Text(
                                text = if (selectedCamera.preferredSubtype == 0) "2K মেইন" else "SD সাব",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onProbeCamera(selectedCamera) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LanEmerald),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("quick_btn_ping")
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("LAN পিং", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // 5. Integrated 8-Way PTZ, Presets & Deterrence Deck right under the live view
            PtzJoystickConsole(
                camera = selectedCamera,
                presets = presetsForSelected,
                ptzSpeed = hudState.ptzSpeed,
                lastCgiStatus = hudState.lastCommandFeedback,
                onPtzDirection = { dir -> onPtzDirection(selectedCamera, dir) },
                onCenterPtz = { onCenterPtz(selectedCamera) },
                onSpeedChange = onSpeedChange,
                onZoomStep = { zoomIn ->
                    val next = if (zoomIn) selectedCamera.zoomLevel + 0.4f else selectedCamera.zoomLevel - 0.4f
                    onPinchZoom(selectedCamera, next)
                },
                onSelectPreset = { preset -> onSelectPreset(selectedCamera, preset) },
                onAddPresetRequest = { onAddPresetRequest(selectedCamera) },
                onDeletePreset = onDeletePreset,
                onNightVisionChange = { mode -> onNightVisionChange(selectedCamera, mode) },
                onToggleSpotlight = { onToggleSpotlight(selectedCamera) },
                onToggleSiren = { onToggleSiren(selectedCamera) },
                onTogglePrivacyMask = { onTogglePrivacyMask(selectedCamera) },
                onToggleFlip180 = { onToggleFlip180(selectedCamera) },
                onToggleSmartTracking = { onToggleSmartTracking(selectedCamera) },
                onRebootCamera = { onRebootCamera(selectedCamera) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
