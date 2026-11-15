package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sd
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ImouCameraEntity
import com.example.data.local.PtzPresetEntity
import com.example.data.local.VaultEventEntity
import com.example.network.PtzDirection
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonContainer
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.ElevatedCarbon
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImouOrange
import com.example.ui.theme.ImouOrangeContainer
import com.example.ui.theme.InfraredViolet
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.LanEmeraldContainer
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private enum class ImouDeckSubTab(val labelBn: String, val icon: ImageVector) {
    PTZ_JOYSTICK("PTZ জয়স্টিক", Icons.Default.ControlCamera),
    FAVORITE_POINTS("ফেভারিট পয়েন্ট", Icons.Default.StarBorder),
    MORE_FEATURES("সব ফিচার", Icons.Default.GridView),
    LOCAL_PLAYBACK("প্লেব্যাক", Icons.Default.History)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImouLiveControlConsole(
    camera: ImouCameraEntity,
    presets: List<PtzPresetEntity>,
    cameraEvents: List<VaultEventEntity>,
    ptzSpeed: Int,
    isRecording: Boolean,
    isAudioMuted: Boolean,
    isTalkActive: Boolean,
    lastCgiStatus: String,
    onCaptureSnapshot: () -> Unit,
    onToggleRecording: () -> Unit,
    onToggleAudio: () -> Unit,
    onToggleTalk: () -> Unit,
    onToggleStreamQuality: () -> Unit,
    onToggleSpotlight: () -> Unit,
    onToggleSiren: () -> Unit,
    onPtzDirection: (PtzDirection) -> Unit,
    onCenterPtz: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSelectPreset: (PtzPresetEntity) -> Unit,
    onAddPresetRequest: () -> Unit,
    onDeletePreset: (Long) -> Unit,
    onNightVisionChange: (String) -> Unit,
    onToggleHumanDetect: () -> Unit,
    onToggleSmartTracking: () -> Unit,
    onToggleClick2Track: () -> Unit,
    onToggleAutoCruise: () -> Unit,
    onTogglePrivacyMask: () -> Unit,
    onToggleFlip180: () -> Unit,
    onRebootCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(ImouDeckSubTab.PTZ_JOYSTICK) }
    val haptics = LocalHapticFeedback.current
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlack),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Official Imou Life Quick Action Ribbon (Snapshot, Record, Audio, Talk, HD/SD, Spotlight, Siren)
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImouRibbonActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "স্ন্যাপশট",
                    active = false,
                    activeColor = ImouOrange,
                    onClick = onCaptureSnapshot,
                    testTag = "ribbon_snapshot"
                )
                ImouRibbonActionButton(
                    icon = Icons.Default.FiberManualRecord,
                    label = if (isRecording) "REC চালু" else "রেকর্ড",
                    active = isRecording,
                    activeColor = AlertCrimson,
                    onClick = onToggleRecording,
                    testTag = "ribbon_record"
                )
                ImouRibbonActionButton(
                    icon = if (isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    label = if (isAudioMuted) "মিউট" else "সাউন্ড",
                    active = !isAudioMuted,
                    activeColor = LanEmerald,
                    onClick = onToggleAudio,
                    testTag = "ribbon_audio"
                )
                ImouRibbonActionButton(
                    icon = if (isTalkActive) Icons.Default.Mic else Icons.Default.MicOff,
                    label = if (isTalkActive) "টক অন" else "মাইক টক",
                    active = isTalkActive,
                    activeColor = ImouOrange,
                    onClick = onToggleTalk,
                    testTag = "ribbon_talk"
                )
                ImouRibbonActionButton(
                    icon = if (camera.preferredSubtype == 0) Icons.Default.Hd else Icons.Default.Sd,
                    label = if (camera.preferredSubtype == 0) "2K মেইন" else "SD সাব",
                    active = camera.preferredSubtype == 0,
                    activeColor = CyberCyan,
                    onClick = onToggleStreamQuality,
                    testTag = "ribbon_quality"
                )
                ImouRibbonActionButton(
                    icon = Icons.Default.FlashlightOn,
                    label = "স্পটলাইট",
                    active = camera.spotlightActive,
                    activeColor = WarningAmber,
                    onClick = onToggleSpotlight,
                    testTag = "ribbon_spotlight"
                )
                ImouRibbonActionButton(
                    icon = Icons.Default.NotificationsActive,
                    label = "সাইরেন",
                    active = camera.sirenActive,
                    activeColor = AlertCrimson,
                    onClick = onToggleSiren,
                    testTag = "ribbon_siren"
                )
            }
        }

        // 2. Official Imou Life 4-Tab Selector Strip (PTZ জয়স্টিক | ফেভারিট পয়েন্ট | সব ফিচার | প্লেব্যাক)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ImouDeckSubTab.entries.forEach { tab ->
                val isSelected = activeSubTab == tab
                Surface(
                    color = if (isSelected) ImouOrangeContainer else ObsidianSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) ImouOrange else GlassBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable { activeSubTab = tab }
                        .testTag("imou_subtab_${tab.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.labelBn,
                            tint = if (isSelected) ImouOrange else TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = tab.labelBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) ImouOrange else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 3. Sub-Tab Content Area in Pitch Black Card
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            when (activeSubTab) {
                ImouDeckSubTab.PTZ_JOYSTICK -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PTZ মোটর কন্ট্রোল (চ্যানেল ${camera.ptzControlChannel})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPureWhite
                                )
                                Text(
                                    text = lastCgiStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = ImouOrange,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Zoom Quick Multiplier Pills (1x, 2x, 4x, 8x) like Imou Life
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(1.0f, 2.0f, 4.0f, 8.0f).forEach { zVal ->
                                    val zActive = kotlin.math.abs(camera.zoomLevel - zVal) < 0.35f
                                    Surface(
                                        color = if (zActive) ImouOrange else PitchBlack,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (zActive) ImouOrange else GlassBorder),
                                        modifier = Modifier.clickable { onZoomChange(zVal) }
                                    ) {
                                        Text(
                                            text = "${zVal.toInt()}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (zActive) PitchBlack else TextPureWhite,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Imou Life Style Large Circular PTZ Joystick + Diagonal & Speed Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(188.dp)
                                    .clip(CircleShape)
                                    .background(PitchBlack)
                                    .border(2.dp, ImouOrange.copy(alpha = 0.65f), CircleShape)
                                    .pointerInput(Unit) {
                                        val maxR = 60f
                                        detectDragGestures(
                                            onDragEnd = {
                                                val dist = hypot(knobOffsetX, knobOffsetY)
                                                if (dist > 16f) {
                                                    val deg = Math.toDegrees(atan2(knobOffsetY.toDouble(), knobOffsetX.toDouble())).toFloat()
                                                    val dir = angleToDirection(deg)
                                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    onPtzDirection(dir)
                                                }
                                                knobOffsetX = 0f
                                                knobOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                knobOffsetX = 0f
                                                knobOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                val nx = knobOffsetX + dragAmount.x
                                                val ny = knobOffsetY + dragAmount.y
                                                val d = hypot(nx, ny)
                                                if (d <= maxR) {
                                                    knobOffsetX = nx
                                                    knobOffsetY = ny
                                                } else {
                                                    val a = atan2(ny, nx)
                                                    knobOffsetX = cos(a) * maxR
                                                    knobOffsetY = sin(a) * maxR
                                                }
                                            }
                                        )
                                    }
                                    .testTag("imou_ptz_wheel"),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val c = Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(
                                        color = ImouOrange.copy(alpha = 0.16f),
                                        radius = size.minDimension * 0.42f,
                                        center = c,
                                        style = Stroke(width = 1.5f)
                                    )
                                    val knobPos = Offset(c.x + knobOffsetX, c.y + knobOffsetY)
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(ImouOrange.copy(alpha = 0.45f), Color.Transparent),
                                            center = knobPos,
                                            radius = 42f
                                        ),
                                        radius = 42f,
                                        center = knobPos
                                    )
                                    drawCircle(
                                        color = CarbonCard,
                                        radius = 25f,
                                        center = knobPos
                                    )
                                    drawCircle(
                                        color = ImouOrange,
                                        radius = 25f,
                                        center = knobPos,
                                        style = Stroke(width = 2f)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPtzDirection(PtzDirection.UP)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .size(48.dp)
                                        .testTag("ptz_up")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = ImouOrange, modifier = Modifier.size(30.dp))
                                }

                                IconButton(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPtzDirection(PtzDirection.DOWN)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .size(48.dp)
                                        .testTag("ptz_down")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = ImouOrange, modifier = Modifier.size(30.dp))
                                }

                                IconButton(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPtzDirection(PtzDirection.LEFT)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(48.dp)
                                        .testTag("ptz_left")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = ImouOrange, modifier = Modifier.size(30.dp))
                                }

                                IconButton(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPtzDirection(PtzDirection.RIGHT)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(48.dp)
                                        .testTag("ptz_right")
                                ) {
                                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = ImouOrange, modifier = Modifier.size(30.dp))
                                }

                                IconButton(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onCenterPtz()
                                    },
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(48.dp)
                                        .testTag("ptz_center")
                                ) {
                                    Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center", tint = TextPureWhite, modifier = Modifier.size(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Right Column: Diagonal Pads + Speed + Click2Track Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SmallDirButton("↖ বাম-উপর", Modifier.weight(1f)) { onPtzDirection(PtzDirection.LEFT_UP) }
                                    SmallDirButton("↗ ডান-উপর", Modifier.weight(1f)) { onPtzDirection(PtzDirection.RIGHT_UP) }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SmallDirButton("↙ বাম-নিচ", Modifier.weight(1f)) { onPtzDirection(PtzDirection.LEFT_DOWN) }
                                    SmallDirButton("↘ ডান-নিচ", Modifier.weight(1f)) { onPtzDirection(PtzDirection.RIGHT_DOWN) }
                                }

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("PTZ রোটেশন স্পিড", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text("${ptzSpeed}/8", style = MaterialTheme.typography.labelSmall, color = ImouOrange)
                                    }
                                    Slider(
                                        value = ptzSpeed.toFloat(),
                                        onValueChange = { onSpeedChange(it.toInt().coerceIn(1, 8)) },
                                        valueRange = 1f..8f,
                                        steps = 6,
                                        colors = SliderDefaults.colors(
                                            thumbColor = ImouOrange,
                                            activeTrackColor = ImouOrange,
                                            inactiveTrackColor = ElevatedCarbon
                                        )
                                    )
                                }

                                if (camera.lensCount >= 2) {
                                    Surface(
                                        color = if (camera.click2TrackEnabled) LanEmeraldContainer else PitchBlack,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (camera.click2TrackEnabled) LanEmerald else GlassBorder
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleClick2Track() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AdsClick,
                                                contentDescription = null,
                                                tint = if (camera.click2TrackEnabled) LanEmerald else TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (camera.click2TrackEnabled) "Click2Track লিংকেজ: চালু" else "Click2Track লিংকেজ: বন্ধ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (camera.click2TrackEnabled) LanEmerald else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ImouDeckSubTab.FAVORITE_POINTS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "কালেকশন পয়েন্ট (Favorite Angles)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPureWhite
                                )
                                Text(
                                    text = "এক ট্যাপে নির্ধারিত পয়েন্টে PTZ লেন্স ঘোরান",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = onAddPresetRequest,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImouOrange,
                                    contentColor = PitchBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পয়েন্ট যোগ", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (presets.isEmpty()) {
                            Text(
                                text = "কোনো ফেভারিট পয়েন্ট নেই। ক্যামেরা ঘুরিয়ে 'পয়েন্ট যোগ' বাটনে ট্যাপ করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                presets.forEach { preset ->
                                    Surface(
                                        color = PitchBlack,
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ImouOrange.copy(alpha = 0.55f)),
                                        modifier = Modifier.clickable { onSelectPreset(preset) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MyLocation,
                                                contentDescription = null,
                                                tint = ImouOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = preset.title,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = TextPureWhite
                                                )
                                                Text(
                                                    text = "PAN ${preset.panAngle.toInt()}° • TILT ${preset.tiltAngle.toInt()}° • ${preset.zoomLevel}x",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeletePreset(preset.id) },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ImouDeckSubTab.MORE_FEATURES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "নাইট ভিশন মোড (Imou Night Vision)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPureWhite
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "SMART" to "স্মার্ট NV",
                                "FULL_COLOR" to "ফুল কালার",
                                "IR_BW" to "ইনফ্রারেড IR",
                                "OFF" to "অফ"
                            ).forEach { (modeCode, label) ->
                                val selected = camera.nightVisionMode == modeCode
                                FilterChip(
                                    selected = selected,
                                    onClick = { onNightVisionChange(modeCode) },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ImouOrangeContainer,
                                        selectedLabelColor = ImouOrange,
                                        containerColor = PitchBlack,
                                        labelColor = TextSecondary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text(
                            text = "অফিসিয়াল Imou ডিভাইস কন্ট্রোল (LAN CGI / ONVIF)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPureWhite
                        )

                        // 2x4 Grid of Imou Feature Tiles
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ImouFeatureTile(
                                title = "হিউম্যান ডিটেকশন",
                                status = if (camera.humanDetectEnabled) "চালু (ON)" else "বন্ধ",
                                icon = Icons.Default.PersonSearch,
                                active = camera.humanDetectEnabled,
                                activeColor = LanEmerald,
                                onClick = onToggleHumanDetect,
                                modifier = Modifier.weight(1f)
                            )
                            ImouFeatureTile(
                                title = "স্মার্ট ট্র্যাকিং",
                                status = if (camera.smartTrackingEnabled) "অটো ট্র্যাক" else "বন্ধ",
                                icon = Icons.Default.DirectionsRun,
                                active = camera.smartTrackingEnabled,
                                activeColor = ImouOrange,
                                onClick = onToggleSmartTracking,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ImouFeatureTile(
                                title = "Click2Track লিংক",
                                status = if (camera.click2TrackEnabled) "মাল্টি-লেন্স সিঙ্ক" else "বন্ধ",
                                icon = Icons.Default.AdsClick,
                                active = camera.click2TrackEnabled,
                                activeColor = CyberCyan,
                                onClick = onToggleClick2Track,
                                modifier = Modifier.weight(1f)
                            )
                            ImouFeatureTile(
                                title = "৩৬০° অটো ক্রুজ",
                                status = if (camera.autoCruiseEnabled) "প্যাট্রোল চলছে" else "বন্ধ",
                                icon = Icons.Default.Explore,
                                active = camera.autoCruiseEnabled,
                                activeColor = LanEmerald,
                                onClick = onToggleAutoCruise,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ImouFeatureTile(
                                title = "প্রাইভেসি মোড",
                                status = if (camera.privacyMaskEnabled) "লেন্স হাইড" else "লেন্স খোলা",
                                icon = Icons.Default.VisibilityOff,
                                active = camera.privacyMaskEnabled,
                                activeColor = WarningAmber,
                                onClick = onTogglePrivacyMask,
                                modifier = Modifier.weight(1f)
                            )
                            ImouFeatureTile(
                                title = "১৮০° ফ্লিপ",
                                status = if (camera.imageFlip180) "উল্টানো (180°)" else "স্বাভাবিক (0°)",
                                icon = Icons.Default.FlipCameraAndroid,
                                active = camera.imageFlip180,
                                activeColor = InfraredViolet,
                                onClick = onToggleFlip180,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedButton(
                            onClick = onRebootCamera,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ক্যামেরা রিস্টার্ট করুন (LAN Reboot CGI)")
                        }
                    }
                }

                ImouDeckSubTab.LOCAL_PLAYBACK -> {
                    var timelineHour by remember { mutableFloatStateOf(14.5f) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "লোকাল SD / LAN টাইমলাইন প্লেব্যাক",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPureWhite
                            )
                            Text(
                                text = "%02d:%02d:00".format(timelineHour.toInt(), ((timelineHour % 1f) * 60).toInt()),
                                style = MaterialTheme.typography.labelMedium,
                                fontFamily = JetBrainsMonoFamily,
                                color = ImouOrange
                            )
                        }

                        // Imou Life Orange Timeline Scrubber Bar
                        Slider(
                            value = timelineHour,
                            onValueChange = { timelineHour = it },
                            valueRange = 0f..23.9f,
                            colors = SliderDefaults.colors(
                                thumbColor = ImouOrange,
                                activeTrackColor = ImouOrange,
                                inactiveTrackColor = ElevatedCarbon
                            )
                        )

                        Text(
                            text = "সাম্প্রতিক লোকাল ইভেন্ট ও স্ন্যাপশট (${cameraEvents.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )

                        if (cameraEvents.isEmpty()) {
                            Text(
                                text = "এই ক্যামেরার জন্য এখনো কোনো স্ন্যাপশট বা ইভেন্ট নেই।",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        } else {
                            cameraEvents.take(4).forEach { ev ->
                                val timeStr = remember(ev.timestamp) {
                                    SimpleDateFormat("HH:mm:ss • dd MMM", Locale.US).format(Date(ev.timestamp))
                                }
                                Surface(
                                    color = PitchBlack,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(ev.title, style = MaterialTheme.typography.labelLarge, color = TextPureWhite)
                                            Text(ev.details, style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = JetBrainsMonoFamily,
                                            color = ImouOrange
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImouRibbonActionButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (active) activeColor.copy(alpha = 0.22f) else PitchBlack)
                .border(
                    width = if (active) 1.5.dp else 1.dp,
                    color = if (active) activeColor else GlassBorder,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else TextPureWhite,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) activeColor else TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SmallDirButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = PitchBlack,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ImouFeatureTile(
    title: String,
    status: String,
    icon: ImageVector,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (active) activeColor.copy(alpha = 0.14f) else PitchBlack,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (active) 1.5.dp else 1.dp,
            color = if (active) activeColor else GlassBorder
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (active) activeColor else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPureWhite
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) activeColor else TextMuted
                )
            }
        }
    }
}

private fun angleToDirection(angleDeg: Float): PtzDirection {
    return when {
        angleDeg >= -22.5f && angleDeg < 22.5f -> PtzDirection.RIGHT
        angleDeg >= 22.5f && angleDeg < 67.5f -> PtzDirection.RIGHT_DOWN
        angleDeg >= 67.5f && angleDeg < 112.5f -> PtzDirection.DOWN
        angleDeg >= 112.5f && angleDeg < 157.5f -> PtzDirection.LEFT_DOWN
        angleDeg >= -67.5f && angleDeg < -22.5f -> PtzDirection.RIGHT_UP
        angleDeg >= -112.5f && angleDeg < -67.5f -> PtzDirection.UP
        angleDeg >= -157.5f && angleDeg < -112.5f -> PtzDirection.LEFT_UP
        else -> PtzDirection.LEFT
    }
}
