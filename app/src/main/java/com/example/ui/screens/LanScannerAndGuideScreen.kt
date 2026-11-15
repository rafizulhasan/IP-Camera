package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.network.DiscoveredLanDevice
import com.example.network.LocalNetworkInfo
import com.example.network.RtspProbeResult
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.ElevatedCarbon
import com.example.ui.theme.GlassBorder
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
fun LanScannerScreen(
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
    var customSubnet by remember(networkInfo.subnetPrefix) { mutableStateOf(networkInfo.subnetPrefix) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Hero Banner Card with Local Subnet Telemetry
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_lan_security_banner_1794752362654),
                            contentDescription = "LAN Security Network",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            PitchBlack.copy(alpha = 0.25f),
                                            PitchBlack.copy(alpha = 0.92f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Surface(
                                color = LanEmeraldContainer,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LanEmerald)
                            ) {
                                Text(
                                    text = "OFF-GRID LAN DISCOVERY • WAN NOT REQUIRED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LanEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "রাউটার LAN স্ক্যানার ও RTSP টেস্টার",
                                style = MaterialTheme.typography.headlineMedium,
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ফোনের লোকাল IP: ${networkInfo.deviceIp} (${networkInfo.interfaceName})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = CyberCyan
                                )
                                Text(
                                    text = "রাউটারের Port 554 (RTSP), 80 (ONVIF/CGI) এবং 37777 (Dahua) স্ক্যান",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customSubnet,
                                onValueChange = { customSubnet = it },
                                label = { Text("রাউটার সাবনেট (যেমন 192.168.0 বা 192.168.1)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
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
                                    containerColor = CyberCyan,
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

                        // 1-Tap Subnet Sync Button for existing cameras
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSyncSubnetForAllCameras(customSubnet.trim()) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LanEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_sync_subnet")
                            ) {
                                Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("সব ক্যামেরা ${customSubnet}.x সাবনেটে সিঙ্ক করুন", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        AnimatedVisibility(visible = isScanning) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                LinearProgressIndicator(
                                    progress = { scanProgress / 100f },
                                    color = CyberCyan,
                                    trackColor = ElevatedCarbon,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Text(
                                    text = "স্ক্যানিং চলছে: $currentScanIp (Port 554 RTSP • 80 ONVIF • 37777 Dahua)...",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // Discovered LAN Devices Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "স্ক্যানে পাওয়া LAN ডিভাইস (${discoveredDevices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPureWhite
                )
                Text(
                    text = "WS-Discovery + TCP Sweep",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        if (discoveredDevices.isEmpty() && !isScanning) {
            item {
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "রাউটারের সাবনেট স্ক্যান করতে উপরের 'স্ক্যান' বাটনে ট্যাপ করুন",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPureWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "আপনার ফোন ও Imou ক্যামেরা একই ওয়াইফাই রাউটারে যুক্ত থাকলে ইন্টারনেট ছাড়াই ক্যামেরার IP ও RTSP পোর্ট এখানে খুঁজে পাওয়া যাবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(discoveredDevices, key = { it.ipAddress }) { dev ->
                Surface(
                    color = ObsidianSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LanEmerald.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(LanEmerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = dev.ipAddress,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = TextPureWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${dev.latencyMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LanEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dev.vendorHint} • ${dev.serverBanner}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan
                            )
                            Text(
                                text = buildString {
                                    append("Open Ports: ")
                                    if (dev.rtspPortOpen) append("[554 RTSP] ")
                                    if (dev.httpPortOpen) append("[80 HTTP/ONVIF] ")
                                    if (dev.dahuaTcpPortOpen) append("[37777 Dahua] ")
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = { onAddDiscoveredCamera(dev.ipAddress) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LanEmerald,
                                contentColor = PitchBlack
                            ),
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

        // Configured Cameras RTSP Handshake Diagnostic Table
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RTSP 554 হ্যান্ডশেক ও পিং ডায়াগনস্টিক",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPureWhite
                    )
                    Text(
                        text = "সংরক্ষিত প্রতিটি ক্যামেরার পোর্ট ও রেসপন্স চেক করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                OutlinedButton(
                    onClick = onProbeAllCameras,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("btn_probe_all_cameras")
                ) {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পিং টেস্ট")
                }
            }
        }

        items(cameras, key = { "probe_${it.id}" }) { cam ->
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
                            text = cam.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPureWhite
                        )
                        Surface(
                            color = if (probe?.rtspPortOpen == true) LanEmeraldContainer else CarbonCard,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (probe?.rtspPortOpen == true) LanEmerald else GlassBorder
                            )
                        ) {
                            Text(
                                text = if (probe?.rtspPortOpen == true) {
                                    "ONLINE • ${probe.latencyMs}ms"
                                } else {
                                    "STANDBY • ${cam.ipAddress}:${cam.rtspPort}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (probe?.rtspPortOpen == true) LanEmerald else CyberCyan,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = cam.buildMaskedRtspUrl(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = JetBrainsMonoFamily,
                        color = TextSecondary
                    )

                    if (probe != null) {
                        Text(
                            text = "Handshake: ${probe.rtspStatusLine}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = JetBrainsMonoFamily,
                            color = if (probe.rtspPortOpen) LanEmerald else WarningAmber
                        )
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
fun OfflineLanGuideScreen(
    cameras: List<ImouCameraEntity>,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = LanEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ইন্টারনেট ছাড়া Imou ক্যামেরা LAN-এ চালানোর পূর্ণাঙ্গ গাইড",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPureWhite
                        )
                    }
                    Text(
                        text = "আপনার রাউটারে ব্রডব্যান্ড ইন্টারনেট না থাকলেও শুধুমাত্র ওয়াইফাই রাউটারের লোকাল নেটওয়ার্ক (LAN) ব্যবহার করে সব Imou ক্যামেরা লাইভ দেখা ও কন্ট্রোল করার ৪টি ধাপ:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            GuideStepCard(
                stepNumber = "ধাপ ১",
                title = "ক্যামেরার Safety Code (পাসওয়ার্ড) সংগ্রহ করুন",
                icon = Icons.Default.Key,
                accentColor = CyberCyan,
                bullets = listOf(
                    "Imou ক্যামেরার লোকাল RTSP ও ONVIF ইউজারনেম সবসময় 'admin' হয়।",
                    "পাসওয়ার্ড হলো ক্যামেরার নিচে বা পেছনে স্টিকারে লেখা ৮-অক্ষরের 'Safety Code' (যেমন: L28A9F4C)।",
                    "আপনি যদি আগে Imou Life অ্যাপ থেকে পাসওয়ার্ড পরিবর্তন করে থাকেন, তবে Imou Life অ্যাপের Device Settings -> Device Information -> Device Password-এ সেটি পাবেন।"
                )
            )
        }

        item {
            GuideStepCard(
                stepNumber = "ধাপ ২",
                title = "রাউটারে Static IP / DHCP Reservation সেট করুন",
                icon = Icons.Default.Router,
                accentColor = LanEmerald,
                bullets = listOf(
                    "কারেন্ট চলে গেলে বা রাউটার রিস্টার্ট নিলে ক্যামেরার IP যেন পরিবর্তন না হয়, সেজন্য রাউটারের অ্যাডমিন পেজে (192.168.0.1 বা 192.168.1.1) ঢুকে DHCP -> Address Reservation-এ যান।",
                    "প্রতিটি Imou ক্যামেরার MAC Address-এর বিপরীতে একটি স্থায়ী IP (যেমন 192.168.1.108, .109, .110, .111) বাইন্ড করে দিন।",
                    "এতে রাউটারে ইন্টারনেট না থাকলেও ক্যামেরাগুলো সবসময় একই IP-তে সংযুক্ত থাকবে।"
                )
            )
        }

        item {
            GuideStepCard(
                stepNumber = "ধাপ ৩",
                title = "Imou / Dahua ডিরেক্ট RTSP ও CGI প্রোটোকল সূত্র",
                icon = Icons.Default.Terminal,
                accentColor = WarningAmber,
                bullets = listOf(
                    "মেইন স্ট্রিম (2K / 1080p HD): rtsp://admin:SAFETY_CODE@IP:554/cam/realmonitor?channel=1&subtype=0",
                    "সাব স্ট্রিম (SD / ৪-ক্যামেরা গ্রিডের জন্য দ্রুত): rtsp://admin:SAFETY_CODE@IP:554/cam/realmonitor?channel=1&subtype=1",
                    "PTZ ও নাইট ভিশন পোর্ট: Port 80 (Dahua HTTP Digest CGI এবং ONVIF /onvif/ptz_service)।"
                )
            )
        }

        item {
            GuideStepCard(
                stepNumber = "ধাপ ৪",
                title = "ক্লাউড ছাড়া ১০০% প্রাইভেসি ও অফলাইন সিকিউরিটি",
                icon = Icons.Default.Security,
                accentColor = CyberCyan,
                bullets = listOf(
                    "আপনি চাইলে রাউটারের Access Control / Firewall থেকে ক্যামেরার ইন্টারনেট (WAN) অ্যাক্সেস ব্লক করে দিতে পারেন।",
                    "WAN ব্লক থাকলেও লোকাল ওয়াইফাই (LAN) দিয়ে এই অ্যাপের মাধ্যমে 2K লাইভ স্ট্রিম, PTZ রোটেশন, সাইরেন ও স্ন্যাপশট ১০০% সচল থাকবে।"
                )
            )
        }

        // Quick Copy RTSP URLs for User's Cameras
        item {
            Surface(
                color = ObsidianSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "আপনার ক্যামেরাগুলোর রেডি RTSP লিংক (VLC / NVR / LAN)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPureWhite
                    )
                    cameras.forEach { cam ->
                        Surface(
                            color = PitchBlack,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${cam.name} (${cam.modelName})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = LanEmerald
                                    )
                                    Text(
                                        text = cam.buildRtspUrl(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = JetBrainsMonoFamily,
                                        color = TextPureWhite
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(cam.buildRtspUrl()))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy RTSP URL",
                                        tint = CyberCyan
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

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    bullets: List<String>
) {
    Surface(
        color = ObsidianSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = PitchBlack,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                ) {
                    Text(
                        text = stepNumber,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPureWhite
                )
            }

            bullets.forEach { line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "▸ ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor
                    )
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
