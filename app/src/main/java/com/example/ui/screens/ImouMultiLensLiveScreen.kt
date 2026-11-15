package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ImouCameraEntity
import com.example.data.local.PtzPresetEntity
import com.example.data.local.VaultEventEntity
import com.example.network.PtzDirection
import com.example.ui.ImouHudUiState
import com.example.ui.components.ImouLiveControlConsole
import com.example.ui.components.MultiLensCameraPlayerSection
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImouOrange
import com.example.ui.theme.ImouOrangeContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary

@Composable
fun ImouMultiLensLiveScreen(
    cameras: List<ImouCameraEntity>,
    selectedCamera: ImouCameraEntity?,
    presetsForSelected: List<PtzPresetEntity>,
    eventsForSelected: List<VaultEventEntity>,
    hudState: ImouHudUiState,
    onBackToHome: () -> Unit,
    onSelectCamera: (Long) -> Unit,
    onEditCameraSettings: (ImouCameraEntity) -> Unit,
    onProbeCamera: (ImouCameraEntity) -> Unit,
    onClick2TrackTap: (ImouCameraEntity, Int, Float, Float) -> Unit,
    onCaptureSnapshot: (ImouCameraEntity) -> Unit,
    onToggleRecording: (ImouCameraEntity) -> Unit,
    onToggleAudio: (Long) -> Unit,
    onToggleTalk: (Long) -> Unit,
    onToggleStreamQuality: (ImouCameraEntity) -> Unit,
    onToggleSpotlight: (ImouCameraEntity) -> Unit,
    onToggleSiren: (ImouCameraEntity) -> Unit,
    onPtzDirection: (ImouCameraEntity, PtzDirection) -> Unit,
    onCenterPtz: (ImouCameraEntity) -> Unit,
    onSpeedChange: (Int) -> Unit,
    onZoomChange: (ImouCameraEntity, Float) -> Unit,
    onSelectPreset: (ImouCameraEntity, PtzPresetEntity) -> Unit,
    onAddPresetRequest: (ImouCameraEntity) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onNightVisionChange: (ImouCameraEntity, String) -> Unit,
    onToggleHumanDetect: (ImouCameraEntity) -> Unit,
    onToggleSmartTracking: (ImouCameraEntity) -> Unit,
    onToggleClick2Track: (ImouCameraEntity) -> Unit,
    onToggleAutoCruise: (ImouCameraEntity) -> Unit,
    onTogglePrivacyMask: (ImouCameraEntity) -> Unit,
    onToggleFlip180: (ImouCameraEntity) -> Unit,
    onRebootCamera: (ImouCameraEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Official Imou Life Live View Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onBackToHome,
                    modifier = Modifier.testTag("btn_live_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPureWhite
                    )
                }
                if (selectedCamera != null) {
                    Column {
                        Text(
                            text = selectedCamera.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPureWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedCamera.ipAddress}:${selectedCamera.rtspPort} • ${selectedCamera.lensCount}-Lens Simultaneous",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFamily,
                            color = ImouOrange
                        )
                    }
                }
            }

            if (selectedCamera != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(selectedCamera.buildRtspUrl()))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy RTSP URL",
                            tint = LanEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onProbeCamera(selectedCamera) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = "Test LAN Ping",
                            tint = ImouOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onEditCameraSettings(selectedCamera) },
                        modifier = Modifier.testTag("btn_live_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Device Settings",
                            tint = TextPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. Quick Camera Switcher Strip (Switch between 3-Lens, 2-Lens, 1-Lens cameras)
        if (cameras.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cameras.forEach { cam ->
                    val isSelected = cam.id == selectedCamera?.id
                    Surface(
                        color = if (isSelected) ImouOrangeContainer else ObsidianSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) ImouOrange else GlassBorder
                        ),
                        modifier = Modifier.clickable { onSelectCamera(cam.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cam.name} (${cam.lensCount}L)",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) ImouOrange else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        if (selectedCamera != null) {
            // 3. Simultaneous 3-Lens / 2-Lens / 1-Lens Live Video Player with Click2Track
            MultiLensCameraPlayerSection(
                camera = selectedCamera,
                probeResult = hudState.probeResults[selectedCamera.id],
                isRecording = hudState.recordingCameraIds.contains(selectedCamera.id),
                isAudioMuted = hudState.mutedCameraIds.contains(selectedCamera.id),
                isTalkActive = hudState.activeTalkCameraIds.contains(selectedCamera.id),
                onClick2TrackTap = { lensIdx, nx, ny ->
                    onClick2TrackTap(selectedCamera, lensIdx, nx, ny)
                },
                onPinchZoomChange = { z -> onZoomChange(selectedCamera, z) }
            )

            // 4. Official Imou Life Action Ribbon & 4-Tab Control Deck
            ImouLiveControlConsole(
                camera = selectedCamera,
                presets = presetsForSelected,
                cameraEvents = eventsForSelected,
                ptzSpeed = hudState.ptzSpeed,
                isRecording = hudState.recordingCameraIds.contains(selectedCamera.id),
                isAudioMuted = hudState.mutedCameraIds.contains(selectedCamera.id),
                isTalkActive = hudState.activeTalkCameraIds.contains(selectedCamera.id),
                lastCgiStatus = hudState.lastCommandFeedback,
                onCaptureSnapshot = { onCaptureSnapshot(selectedCamera) },
                onToggleRecording = { onToggleRecording(selectedCamera) },
                onToggleAudio = { onToggleAudio(selectedCamera.id) },
                onToggleTalk = { onToggleTalk(selectedCamera.id) },
                onToggleStreamQuality = { onToggleStreamQuality(selectedCamera) },
                onToggleSpotlight = { onToggleSpotlight(selectedCamera) },
                onToggleSiren = { onToggleSiren(selectedCamera) },
                onPtzDirection = { dir -> onPtzDirection(selectedCamera, dir) },
                onCenterPtz = { onCenterPtz(selectedCamera) },
                onSpeedChange = onSpeedChange,
                onZoomChange = { z -> onZoomChange(selectedCamera, z) },
                onSelectPreset = { preset -> onSelectPreset(selectedCamera, preset) },
                onAddPresetRequest = { onAddPresetRequest(selectedCamera) },
                onDeletePreset = onDeletePreset,
                onNightVisionChange = { mode -> onNightVisionChange(selectedCamera, mode) },
                onToggleHumanDetect = { onToggleHumanDetect(selectedCamera) },
                onToggleSmartTracking = { onToggleSmartTracking(selectedCamera) },
                onToggleClick2Track = { onToggleClick2Track(selectedCamera) },
                onToggleAutoCruise = { onToggleAutoCruise(selectedCamera) },
                onTogglePrivacyMask = { onTogglePrivacyMask(selectedCamera) },
                onToggleFlip180 = { onToggleFlip180(selectedCamera) },
                onRebootCamera = { onRebootCamera(selectedCamera) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
