package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.ImouCameraEntity
import com.example.data.local.VaultEventEntity
import com.example.network.DiscoveredLanDevice
import com.example.network.LocalNetworkInfo
import com.example.network.RtspProbeResult
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElevatedCarbon
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImouMessagesAndAlbumScreen(
    events: List<VaultEventEntity>,
    onDeleteEvent: (Long) -> Unit,
    onClearAllEvents: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredEvents = remember(events, selectedFilter) {
        if (selectedFilter == "ALL") events
        else events.filter { it.eventType == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "মেসেজ সেন্টার ও লোকাল অ্যালবাম",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPureWhite
                            )
                            Text(
                                text = "স্ন্যাপশট, রেকর্ডিং, Click2Track ও সাইরেন লগ (১০০% অফলাইন)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        if (events.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onClearAllEvents,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_clear_events")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AlertCrimson, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("সব মুছুন", color = AlertCrimson, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "সব (${events.size})",
                            "SNAPSHOT" to "স্ন্যাপশট",
                            "RECORDING" to "রেকর্ডিং",
                            "CLICK2TRACK" to "Click2Track",
                            "SIREN_TRIGGER" to "অ্যালার্ম"
                        ).forEach { (code, label) ->
                            FilterChip(
                                selected = selectedFilter == code,
                                onClick = { selectedFilter = code },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ImouOrangeContainer,
                                    selectedLabelColor = ImouOrange,
                                    containerColor = PitchBlack,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filteredEvents.isEmpty()) {
            item {
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
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = ImouOrange, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("কোনো অ্যালবাম ফাইল বা ইভেন্ট নেই", style = MaterialTheme.typography.titleMedium, color = TextPureWhite)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "লাইভ ভিউ থেকে স্ন্যাপশট নিলে বা ফিক্সড লেন্সে ট্যাপ করে Click2Track ব্যবহার করলে এখানে সংরক্ষিত হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                val accent = when (event.eventType) {
                    "SNAPSHOT" -> ImouOrange
                    "RECORDING" -> AlertCrimson
                    "CLICK2TRACK" -> LanEmerald
                    "SIREN_TRIGGER" -> WarningAmber
                    else -> CyberCyan
                }
                val icon = when (event.eventType) {
                    "SNAPSHOT" -> Icons.Default.CameraAlt
                    "RECORDING" -> Icons.Default.FiberManualRecord
                    "CLICK2TRACK" -> Icons.Default.AdsClick
                    "SIREN_TRIGGER" -> Icons.Default.NotificationsActive
                    "PTZ_PRESET" -> Icons.Default.MyLocation
                    else -> Icons.Default.Terminal
                }
                val timeStr = remember(event.timestamp) {
                    SimpleDateFormat("dd MMM • HH:mm:ss", Locale.US).format(Date(event.timestamp))
                }

                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PitchBlack),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = event.resolution,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = accent
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${event.cameraName} • ${event.cameraIp} • $timeStr",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFamily,
                                color = ImouOrange
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = event.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = { onDeleteEvent(event.id) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ImouLanCenterScreen(
    networkInfo: LocalNetworkInfo,
    isScanning: Boolean,
    scanProgress: Int,
    currentScanIp: String,
    discoveredDevices: List<DiscoveredLanDevice>,
    cameras: List<ImouCameraEntity>,
    probeResults: Map<Long, RtspProbeResult>,
    onStartSubnetScan: (String) -> Unit,
    onProbeAllCameras: () -> Unit,
    onSyncSubnetForAllCameras: (String) -> Unit,
    onAddDiscoveredCamera: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    var customSubnet by remember(networkInfo.subnetPrefix) { mutableStateOf(networkInfo.subnetPrefix) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(136.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lan_security_banner_1794752362654),
                            contentDescription = "Imou LAN Network",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(PitchBlack.copy(alpha = 0.2f), PitchBlack.copy(alpha = 0.94f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Surface(
                                color = ImouOrangeContainer,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ImouOrange)
                            ) {
                                Text(
                                    text = "MULTI-LENS LAN HUB • NO INTERNET NEEDED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ImouOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "রাউটার LAN স্ক্যানার ও মাল্টি-লেন্স RTSP হাব",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPureWhite
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ফোনের লোকাল IP: ${networkInfo.deviceIp} (${networkInfo.interfaceName})",
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = JetBrainsMonoFamily,
                            color = LanEmerald
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customSubnet,
                                onValueChange = { customSubnet = it },
                                label = { Text("রাউটার সাবনেট (192.168.0 বা 192.168.1)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImouOrange,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedTextColor = TextPureWhite,
                                    unfocusedTextColor = TextPureWhite,
                                    focusedContainerColor = PitchBlack,
                                    unfocusedContainerColor = PitchBlack
                                ),
                                modifier = Modifier
                                    .weight(1.4f)
                                    .testTag("input_subnet_prefix")
                            )

                            Button(
                                onClick = { onStartSubnetScan(customSubnet.trim()) },
                                enabled = !isScanning,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ImouOrange,
                                    contentColor = PitchBlack
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(54.dp)
                                    .testTag("btn_start_lan_scan")
                            ) {
                                Icon(Icons.Default.Radar, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isScanning) "${scanProgress}%" else "স্ক্যান", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { onSyncSubnetForAllCameras(customSubnet.trim()) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LanEmerald),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_sync_subnet")
                        ) {
                            Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সব ক্যামেরা ${customSubnet}.x সাবনেটে সিঙ্ক করুন")
                        }

                        AnimatedVisibility(visible = isScanning) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LinearProgressIndicator(
                                    progress = { scanProgress / 100f },
                                    color = ImouOrange,
                                    trackColor = ElevatedCarbon,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = "স্ক্যানিং: $currentScanIp (RTSP 554 • ONVIF 80 • Dahua 37777)...",
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

        // Discovered LAN Cameras
        if (discoveredDevices.isNotEmpty()) {
            item {
                Text(
                    text = "খুঁজে পাওয়া LAN ক্যামেরা (${discoveredDevices.size}):",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPureWhite
                )
            }
            items(discoveredDevices, key = { it.ipAddress }) { dev ->
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LanEmerald)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${dev.ipAddress} (${dev.latencyMs}ms)",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = JetBrainsMonoFamily,
                                color = LanEmerald
                            )
                            Text(
                                text = "${dev.vendorHint} • ${dev.serverBanner}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPureWhite
                            )
                        }
                        Button(
                            onClick = { onAddDiscoveredCamera(dev.ipAddress) },
                            colors = ButtonDefaults.buttonColors(containerColor = ImouOrange, contentColor = PitchBlack),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("যুক্ত করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Multi-Lens Channel RTSP Guide Card
        item {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImouOrange.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Imou 2-Lens ও 3-Lens ক্যামেরা LAN RTSP চ্যানেল সূত্র",
                        style = MaterialTheme.typography.titleMedium,
                        color = ImouOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "▸ লেন্স ১ (Fixed Wide Lens): channel=1 (যেমন: /cam/realmonitor?channel=1&subtype=0)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPureWhite
                    )
                    Text(
                        text = "▸ লেন্স ২ (Dual PTZ বা ২য় Fixed Lens): channel=2 (/cam/realmonitor?channel=2&subtype=0)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPureWhite
                    )
                    Text(
                        text = "▸ লেন্স ৩ (Triple Camera PTZ Lens): channel=3 (/cam/realmonitor?channel=3&subtype=0)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPureWhite
                    )
                    Text(
                        text = "▸ ইউজারনেম: admin এবং পাসওয়ার্ড: ক্যামেরার গায়ের স্টিকারে লেখা ৮-অক্ষরের Safety Code।",
                        style = MaterialTheme.typography.bodySmall,
                        color = LanEmerald
                    )
                }
            }
        }

        // Configured Multi-Lens Camera RTSP Links & Ping Table
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "আপনার ক্যামেরাগুলোর সব লেন্সের RTSP লিংক",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPureWhite
                )
                OutlinedButton(
                    onClick = onProbeAllCameras,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ImouOrange)
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("পিং চেক")
                }
            }
        }

        items(cameras, key = { "lan_cam_${it.id}" }) { cam ->
            val probe = probeResults[cam.id]
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${cam.name} (${cam.lensCount}-Lens)",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPureWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (probe?.rtspPortOpen == true) "ONLINE • ${probe.latencyMs}ms" else "LAN READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = LanEmerald
                        )
                    }

                    cam.getLensDescriptors().forEach { l ->
                        val url = cam.buildRtspUrlForChannel(l.channel)
                        Surface(
                            color = PitchBlack,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "লেন্স ${l.lensIndex} (CH${l.channel}) • ${l.badge}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (l.isPtzLens) ImouOrange else CyberCyan
                                    )
                                    Text(
                                        text = cam.buildMaskedRtspUrl(l.channel),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = JetBrainsMonoFamily,
                                        color = TextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { clipboard.setText(AnnotatedString(url)) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy RTSP",
                                        tint = ImouOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
