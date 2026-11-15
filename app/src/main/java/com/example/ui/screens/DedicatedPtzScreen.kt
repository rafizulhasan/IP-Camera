package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.local.ImouCameraEntity
import com.example.data.local.PtzPresetEntity
import com.example.network.PtzDirection
import com.example.ui.ImouHudUiState
import com.example.ui.components.PtzJoystickConsole
import com.example.ui.components.RtspCameraViewport
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary

@Composable
fun DedicatedPtzScreen(
    cameras: List<ImouCameraEntity>,
    selectedCamera: ImouCameraEntity?,
    presetsForSelected: List<PtzPresetEntity>,
    hudState: ImouHudUiState,
    onSelectCamera: (Long) -> Unit,
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
    onTogglePrivacyMask: (ImouCameraEntity) -> Unit,
    onToggleFlip180: (ImouCameraEntity) -> Unit,
    onToggleSmartTracking: (ImouCameraEntity) -> Unit,
    onRebootCamera: (ImouCameraEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "প্রিসিশন PTZ ও CGI কমান্ড ডেক",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPureWhite
        )

        // Camera Selector Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cameras.forEach { cam ->
                val isSelected = cam.id == selectedCamera?.id
                Surface(
                    color = if (isSelected) CyberCyanContainer else ObsidianSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CyberCyan else GlassBorder
                    ),
                    modifier = Modifier.clickable { onSelectCamera(cam.id) }
                ) {
                    Text(
                        text = "${cam.name} (${cam.ipAddress})",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) CyberCyan else TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (selectedCamera != null) {
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
                    .aspectRatio(16f / 9f)
            )

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
