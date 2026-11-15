package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ImouCameraEntity
import com.example.network.LocalNetworkInfo
import com.example.network.RtspProbeResult
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.AlertCrimsonContainer
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImouOrange
import com.example.ui.theme.ImouOrangeContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.LanEmeraldContainer
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ImouHomeScreen(
    cameras: List<ImouCameraEntity>,
    networkInfo: LocalNetworkInfo,
    probeResults: Map<Long, RtspProbeResult>,
    onOpenCameraLiveView: (Long) -> Unit,
    onAddCameraClick: () -> Unit,
    onEditCameraClick: (ImouCameraEntity) -> Unit,
    onOpenLanScannerClick: () -> Unit,
    onToggleCameraSiren: (ImouCameraEntity) -> Unit,
    onToggleCameraSpotlight: (ImouCameraEntity) -> Unit,
    onToggleCameraPrivacy: (ImouCameraEntity) -> Unit,
    onToggleAllSirens: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRoom by remember { mutableStateOf("সব ডিভাইস") }
    val rooms = remember(cameras) {
        listOf("সব ডিভাইস") + cameras.map { it.roomName }.distinct()
    }
    val filteredCameras = remember(cameras, selectedRoom) {
        if (selectedRoom == "সব ডিভাইস") cameras
        else cameras.filter { it.roomName == selectedRoom }
    }
    val anySirenActive = cameras.any { it.sirenActive }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // 1. Official Imou Life Style Home Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = ImouOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আমার বাসা (Imou LAN)",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPureWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(LanEmerald)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "রাউটার লোকাল নেটওয়ার্ক (${networkInfo.subnetPrefix}.0/24) • ইন্টারনেট ছাড়া সচল",
                            style = MaterialTheme.typography.labelSmall,
                            color = LanEmerald
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // SOS Siren Button
                    Surface(
                        color = if (anySirenActive) AlertCrimson else AlertCrimsonContainer,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AlertCrimson),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onToggleAllSirens() }
                            .testTag("home_btn_all_siren")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "SOS Siren",
                                tint = if (anySirenActive) PitchBlack else AlertCrimson,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Quick LAN Auto-Scan Button
                    Surface(
                        color = ObsidianSurface,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onOpenLanScannerClick() }
                            .testTag("home_btn_lan_scan")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = "LAN Scan",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Imou Signature Orange '+' Add Camera Button
                    Surface(
                        color = ImouOrange,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(44.dp)
                            .clickable { onAddCameraClick() }
                            .testTag("home_btn_add_camera")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Camera",
                                tint = PitchBlack,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Room / Category Filter Pills (Just like Imou Life Home Tabs)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rooms.forEach { room ->
                    val selected = selectedRoom == room
                    FilterChip(
                        selected = selected,
                        onClick = { selectedRoom = room },
                        label = {
                            Text(
                                text = room,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ImouOrange,
                            selectedLabelColor = PitchBlack,
                            containerColor = ObsidianSurface,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // 3. Multi-Lens Device Cards List (Matching Imou Life Card Layout)
        items(filteredCameras, key = { it.id }) { camera ->
            ImouDeviceHomeCard(
                camera = camera,
                probeResult = probeResults[camera.id],
                onOpenLiveView = { onOpenCameraLiveView(camera.id) },
                onEditSettings = { onEditCameraClick(camera) },
                onToggleSiren = { onToggleCameraSiren(camera) },
                onToggleSpotlight = { onToggleCameraSpotlight(camera) },
                onTogglePrivacy = { onToggleCameraPrivacy(camera) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImouDeviceHomeCard(
    camera: ImouCameraEntity,
    probeResult: RtspProbeResult?,
    onOpenLiveView: () -> Unit,
    onEditSettings: () -> Unit,
    onToggleSiren: () -> Unit,
    onToggleSpotlight: () -> Unit,
    onTogglePrivacy: () -> Unit
) {
    val lenses = remember(camera) { camera.getLensDescriptors() }

    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (camera.sirenActive) AlertCrimson else GlassBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenLiveView() }
            .testTag("imou_device_card_${camera.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Header: Camera Name, Multi-Lens Badge, LAN IP, Settings Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = camera.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPureWhite,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = ImouOrangeContainer,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImouOrange)
                        ) {
                            Text(
                                text = "${camera.lensCount}-LENS",
                                style = MaterialTheme.typography.labelSmall,
                                color = ImouOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${camera.modelName} • LAN IP: ${camera.ipAddress}:${camera.rtspPort}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFamily,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleSpotlight,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashlightOn,
                            contentDescription = "Spotlight",
                            tint = if (camera.spotlightActive) WarningAmber else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleSiren,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Siren",
                            tint = if (camera.sirenActive) AlertCrimson else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Privacy Mode",
                            tint = if (camera.privacyMaskEnabled) ImouOrange else TextSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onEditSettings,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPureWhite,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            // Multi-Lens Simultaneous Preview Thumbnail Box (Shows 3 Lenses or 2 Lenses split inside the card!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (camera.lensCount >= 2) 168.dp else 148.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(PitchBlack)
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    lenses.forEach { lens ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(PitchBlack)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val accent = if (lens.isPtzLens) ImouOrange else CyberCyan
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(accent.copy(alpha = 0.20f), PitchBlack),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        radius = size.maxDimension * 0.65f
                                    ),
                                    radius = size.maxDimension,
                                    center = Offset(size.width / 2f, size.height / 2f)
                                )
                                // Subtle grid
                                for (i in 1..3) {
                                    val x = (size.width / 4f) * i
                                    drawLine(accent.copy(alpha = 0.12f), Offset(x, 0f), Offset(x, size.height), 1f)
                                }
                                for (j in 1..2) {
                                    val y = (size.height / 3f) * j
                                    drawLine(accent.copy(alpha = 0.12f), Offset(0f, y), Offset(size.width, y), 1f)
                                }
                                drawCircle(
                                    color = accent.copy(alpha = 0.45f),
                                    radius = 16f,
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    style = Stroke(width = 1.2f)
                                )
                            }

                            // Lens Channel Top Pill
                            Surface(
                                color = PitchBlack.copy(alpha = 0.82f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (lens.isPtzLens) ImouOrange else CyberCyan
                                ),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "লেন্স ${lens.lensIndex} (CH${lens.channel})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (lens.isPtzLens) ImouOrange else CyberCyan,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = if (lens.isPtzLens) "360° PTZ ট্র্যাকিং" else "ফিক্সড ওয়াইড ভিউ",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp)
                            )
                        }
                    }
                }

                // Center Play Multi-Lens Overlay Button
                Surface(
                    color = ImouOrange.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play All Lenses",
                            tint = PitchBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (camera.lensCount >= 2) {
                                "সব ${camera.lensCount}টি লেন্স একসাথে প্লে করুন"
                            } else {
                                "লাইভ ভিউ চালু করুন"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = PitchBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Card Footer: Click2Track Status & LAN RTSP Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdsClick,
                        contentDescription = null,
                        tint = if (camera.click2TrackEnabled && camera.lensCount >= 2) LanEmerald else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (camera.lensCount >= 2) {
                            "Click2Track মাল্টি-লেন্স সিঙ্ক • ${camera.nightVisionMode}"
                        } else {
                            "360° PTZ কন্ট্রোল • ${camera.nightVisionMode}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Text(
                    text = if (probeResult?.rtspPortOpen == true) {
                        "RTSP ONLINE (${probeResult.latencyMs}ms)"
                    } else {
                        "LAN DIRECT READY"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = JetBrainsMonoFamily,
                    color = LanEmerald
                )
            }
        }
    }
}
