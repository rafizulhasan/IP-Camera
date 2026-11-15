package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ImouCameraEntity
import com.example.data.local.PtzPresetEntity
import com.example.network.PtzDirection
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonContainer
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.ElevatedCarbon
import com.example.ui.theme.GlassBorder
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PtzJoystickConsole(
    camera: ImouCameraEntity,
    presets: List<PtzPresetEntity>,
    ptzSpeed: Int,
    lastCgiStatus: String,
    onPtzDirection: (PtzDirection) -> Unit,
    onCenterPtz: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onZoomStep: (Boolean) -> Unit,
    onSelectPreset: (PtzPresetEntity) -> Unit,
    onAddPresetRequest: () -> Unit,
    onDeletePreset: (Long) -> Unit,
    onNightVisionChange: (String) -> Unit,
    onToggleSpotlight: () -> Unit,
    onToggleSiren: () -> Unit,
    onTogglePrivacyMask: () -> Unit,
    onToggleFlip180: () -> Unit,
    onToggleSmartTracking: () -> Unit,
    onRebootCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    var knobOffsetX by remember { mutableFloatStateOf(0f) }
    var knobOffsetY by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. PTZ Joystick & Optical Zoom Card
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "8-Way PTZ ও অপটিক্যাল কন্ট্রোল",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPureWhite
                        )
                        Text(
                            text = lastCgiStatus,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFamily,
                            color = CyberCyan
                        )
                    }
                    Surface(
                        color = CyberCyanContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "PAN ${camera.panAngle.toInt()}° • TILT ${camera.tiltAngle.toInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Circular 8-Way Joystick + D-Pad Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Tactile 8-Way Wheel with Drag + Tap sectors
                    Box(
                        modifier = Modifier
                            .size(196.dp)
                            .clip(CircleShape)
                            .background(PitchBlack)
                            .border(1.5.dp, GlassBorder, CircleShape)
                            .pointerInput(Unit) {
                                val maxRadius = 64f
                                detectDragGestures(
                                    onDragEnd = {
                                        val distance = hypot(knobOffsetX, knobOffsetY)
                                        if (distance > 18f) {
                                            val angleDeg = Math.toDegrees(atan2(knobOffsetY.toDouble(), knobOffsetX.toDouble())).toFloat()
                                            val dir = angleToPtzDirection(angleDeg)
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
                                        val nextX = knobOffsetX + dragAmount.x
                                        val nextY = knobOffsetY + dragAmount.y
                                        val dist = hypot(nextX, nextY)
                                        if (dist <= maxRadius) {
                                            knobOffsetX = nextX
                                            knobOffsetY = nextY
                                        } else {
                                            val angle = atan2(nextY, nextX)
                                            knobOffsetX = cos(angle) * maxRadius
                                            knobOffsetY = sin(angle) * maxRadius
                                        }
                                    }
                                )
                            }
                            .testTag("ptz_joystick_wheel"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(
                                color = CyberCyan.copy(alpha = 0.14f),
                                radius = size.minDimension * 0.42f,
                                center = center,
                                style = Stroke(width = 1.5f)
                            )
                            drawCircle(
                                color = GlassBorder,
                                radius = size.minDimension * 0.24f,
                                center = center,
                                style = Stroke(width = 1f)
                            )
                            // Joystick Knob position
                            val knobCenter = Offset(center.x + knobOffsetX, center.y + knobOffsetY)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CyberCyan.copy(alpha = 0.40f), Color.Transparent),
                                    center = knobCenter,
                                    radius = 44f
                                ),
                                radius = 44f,
                                center = knobCenter
                            )
                            drawCircle(
                                color = CarbonCard,
                                radius = 26f,
                                center = knobCenter
                            )
                            drawCircle(
                                color = CyberCyan,
                                radius = 26f,
                                center = knobCenter,
                                style = Stroke(width = 2f)
                            )
                        }

                        // Up Button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPtzDirection(PtzDirection.UP)
                            },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .size(48.dp)
                                .testTag("ptz_btn_up")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Tilt Up",
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Down Button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPtzDirection(PtzDirection.DOWN)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(48.dp)
                                .testTag("ptz_btn_down")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Tilt Down",
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Left Button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPtzDirection(PtzDirection.LEFT)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .size(48.dp)
                                .testTag("ptz_btn_left")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Pan Left",
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Right Button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPtzDirection(PtzDirection.RIGHT)
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(48.dp)
                                .testTag("ptz_btn_right")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Pan Right",
                                tint = CyberCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Center Reset / Home Button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCenterPtz()
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .testTag("ptz_btn_center")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = "Center PTZ",
                                tint = TextPureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Diagonal Quick Pads + Optical Zoom + Speed
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ডায়াগোনাল ও জুম (Zoom ${"%.1f".format(camera.zoomLevel)}x)",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )

                        // Diagonal Step Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DiagonalPillButton("↖ NW", Modifier.weight(1f)) { onPtzDirection(PtzDirection.LEFT_UP) }
                            DiagonalPillButton("↗ NE", Modifier.weight(1f)) { onPtzDirection(PtzDirection.RIGHT_UP) }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DiagonalPillButton("↙ SW", Modifier.weight(1f)) { onPtzDirection(PtzDirection.LEFT_DOWN) }
                            DiagonalPillButton("↘ SE", Modifier.weight(1f)) { onPtzDirection(PtzDirection.RIGHT_DOWN) }
                        }

                        // Zoom Wide / Tele Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onZoomStep(false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("ptz_zoom_out"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPureWhite)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wide", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { onZoomStep(true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("ptz_zoom_in"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tele", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // PTZ Speed Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "মোটর স্পিড (CGI Speed)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${ptzSpeed}/8",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = LanEmerald
                                )
                            }
                            Slider(
                                value = ptzSpeed.toFloat(),
                                onValueChange = { onSpeedChange(it.toInt().coerceIn(1, 8)) },
                                valueRange = 1f..8f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = ElevatedCarbon
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. PTZ Presets Memory Bank Card
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PTZ প্রিসেট মেমোরি (Quick Angles)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPureWhite
                        )
                        Text(
                            text = "এক ট্যাপে নির্ধারিত অ্যাঙ্গেলে ক্যামেরা ঘোরান",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = onAddPresetRequest,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LanEmerald),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("btn_save_preset")
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Preset", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("পজিশন সেভ", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (presets.isEmpty()) {
                    Text(
                        text = "এই ক্যামেরার জন্য কোনো প্রিসেট সেভ করা নেই। উপরের 'পজিশন সেভ' বাটনে ট্যাপ করে বর্তমান অ্যাঙ্গেলটি সংরক্ষণ করুন।",
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
                                color = CarbonCard,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier
                                    .clickable { onSelectPreset(preset) }
                                    .testTag("preset_chip_${preset.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "#${preset.presetNumber} ${preset.title}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPureWhite
                                        )
                                        Text(
                                            text = "P:${preset.panAngle.toInt()}° T:${preset.tiltAngle.toInt()}° • ${preset.zoomLevel}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeletePreset(preset.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Preset",
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

        // 3. Imou Active Deterrence, Night Vision & Hardware CGI Controls Card
        Surface(
            color = ObsidianSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "নাইট ভিশন ও অ্যাক্টিভ ডিটারেন্স (CGI / ONVIF)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPureWhite
                )

                // Night Vision 4-Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        "AUTO" to "অটো NV",
                        "FULL_COLOR" to "ফুল কালার",
                        "IR_BW" to "ইনফ্রারেড IR",
                        "SMART" to "স্মার্ট AI"
                    )
                    modes.forEach { (code, label) ->
                        val selected = camera.nightVisionMode == code
                        FilterChip(
                            selected = selected,
                            onClick = { onNightVisionChange(code) },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyanContainer,
                                selectedLabelColor = CyberCyan,
                                containerColor = PitchBlack,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Hardware Action Toggles Grid (Spotlight, Siren, Smart Track, Privacy Mask, 180 Flip, Reboot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HardwareActionTile(
                        title = "স্পটলাইট",
                        subtitle = if (camera.spotlightActive) "চালু (ON)" else "বন্ধ (OFF)",
                        icon = Icons.Default.FlashlightOn,
                        active = camera.spotlightActive,
                        activeColor = WarningAmber,
                        activeBg = WarningAmberContainer,
                        onClick = onToggleSpotlight,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_spotlight")
                    )

                    HardwareActionTile(
                        title = "110dB সাইরেন",
                        subtitle = if (camera.sirenActive) "বাজছে (ALARM)" else "প্রস্তুত (READY)",
                        icon = Icons.Default.NotificationsActive,
                        active = camera.sirenActive,
                        activeColor = AlertCrimson,
                        activeBg = AlertCrimsonContainer,
                        onClick = onToggleSiren,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_siren")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HardwareActionTile(
                        title = "স্মার্ট ট্র্যাকিং",
                        subtitle = if (camera.smartTrackingEnabled) "হিউম্যান ট্র্যাক ON" else "স্থির (Static)",
                        icon = Icons.Default.DirectionsRun,
                        active = camera.smartTrackingEnabled,
                        activeColor = LanEmerald,
                        activeBg = LanEmeraldContainer,
                        onClick = onToggleSmartTracking,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_smart_tracking")
                    )

                    HardwareActionTile(
                        title = "প্রাইভেসি শিল্ড",
                        subtitle = if (camera.privacyMaskEnabled) "লেন্স হাইড করা" else "লেন্স খোলা",
                        icon = Icons.Default.VisibilityOff,
                        active = camera.privacyMaskEnabled,
                        activeColor = CyberCyan,
                        activeBg = CyberCyanContainer,
                        onClick = onTogglePrivacyMask,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_privacy_mask")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HardwareActionTile(
                        title = "১৮০° ফ্লিপ (ছাদ)",
                        subtitle = if (camera.imageFlip180) "উল্টানো (180°)" else "স্বাভাবিক (0°)",
                        icon = Icons.Default.FlipCameraAndroid,
                        active = camera.imageFlip180,
                        activeColor = InfraredViolet,
                        activeBg = CarbonCard,
                        onClick = onToggleFlip180,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_flip_180")
                    )

                    HardwareActionTile(
                        title = "LAN রিবুট",
                        subtitle = "magicBox.cgi",
                        icon = Icons.Default.RestartAlt,
                        active = false,
                        activeColor = CyberCyan,
                        activeBg = CarbonCard,
                        onClick = onRebootCamera,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_reboot_camera")
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagonalPillButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = PitchBlack,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = modifier
            .height(42.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = JetBrainsMonoFamily,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun HardwareActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (active) activeBg else PitchBlack,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (active) 1.5.dp else 1.dp,
            color = if (active) activeColor else GlassBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
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
                    color = if (active) TextPureWhite else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) activeColor else TextMuted
                )
            }
        }
    }
}

private fun angleToPtzDirection(angleDeg: Float): PtzDirection {
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
