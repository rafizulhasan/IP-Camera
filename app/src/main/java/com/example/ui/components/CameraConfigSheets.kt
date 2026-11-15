package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ImouCameraEntity
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraEditorDialog(
    initialCamera: ImouCameraEntity?,
    defaultSubnetPrefix: String,
    prefillIp: String? = null,
    onDismiss: () -> Unit,
    onSave: (ImouCameraEntity) -> Unit,
    onDelete: ((ImouCameraEntity) -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current

    var name by remember { mutableStateOf(initialCamera?.name ?: "বারান্দা • Imou Cam") }
    var modelName by remember { mutableStateOf(initialCamera?.modelName ?: "Imou Ranger 2 2K") }
    var ipAddress by remember {
        mutableStateOf(prefillIp ?: initialCamera?.ipAddress ?: "$defaultSubnetPrefix.112")
    }
    var rtspPortText by remember { mutableStateOf((initialCamera?.rtspPort ?: 554).toString()) }
    var httpPortText by remember { mutableStateOf((initialCamera?.httpPort ?: 80).toString()) }
    var username by remember { mutableStateOf(initialCamera?.username ?: "admin") }
    var safetyCode by remember { mutableStateOf(initialCamera?.safetyCode ?: "") }
    var showPassword by remember { mutableStateOf(false) }
    var channelText by remember { mutableStateOf((initialCamera?.channel ?: 1).toString()) }
    var preferredSubtype by remember { mutableIntStateOf(initialCamera?.preferredSubtype ?: 0) }
    var customRtspPath by remember {
        mutableStateOf(initialCamera?.customRtspPath ?: "/cam/realmonitor?channel={ch}&subtype={sub}")
    }
    var zoneName by remember { mutableStateOf(initialCamera?.zoneName ?: "আউটডোর (Outdoor)") }
    var supportsPtz by remember { mutableStateOf(initialCamera?.supportsPtz ?: true) }
    var supportsSpotlight by remember { mutableStateOf(initialCamera?.supportsSpotlight ?: true) }
    var supportsSiren by remember { mutableStateOf(initialCamera?.supportsSiren ?: true) }

    val previewCamera = remember(
        name, modelName, ipAddress, rtspPortText, httpPortText, username,
        safetyCode, channelText, preferredSubtype, customRtspPath, zoneName,
        supportsPtz, supportsSpotlight, supportsSiren
    ) {
        ImouCameraEntity(
            id = initialCamera?.id ?: 0L,
            name = name.ifBlank { "Imou LAN Cam" },
            modelName = modelName,
            ipAddress = ipAddress.ifBlank { "$defaultSubnetPrefix.108" },
            rtspPort = rtspPortText.toIntOrNull() ?: 554,
            httpPort = httpPortText.toIntOrNull() ?: 80,
            onvifPort = httpPortText.toIntOrNull() ?: 80,
            username = username.ifBlank { "admin" },
            safetyCode = safetyCode,
            channel = channelText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            preferredSubtype = preferredSubtype,
            customRtspPath = customRtspPath.ifBlank { "/cam/realmonitor?channel={ch}&subtype={sub}" },
            supportsPtz = supportsPtz,
            supportsSpotlight = supportsSpotlight,
            supportsSiren = supportsSiren,
            zoneName = zoneName.ifBlank { "LAN Zone" },
            nightVisionMode = initialCamera?.nightVisionMode ?: "AUTO",
            panAngle = initialCamera?.panAngle ?: 0f,
            tiltAngle = initialCamera?.tiltAngle ?: 0f,
            zoomLevel = initialCamera?.zoomLevel ?: 1.0f,
            isFavorite = initialCamera?.isFavorite ?: false
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            color = ObsidianSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialCamera == null) "নতুন Imou LAN ক্যামেরা যুক্ত করুন" else "ক্যামেরা কনফিগারেশন এডিট",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPureWhite
                        )
                        Text(
                            text = "রাউটারে ইন্টারনেট ছাড়াই লোকাল RTSP ও CGI কানেকশন",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Quick Imou Model Presets
                Text(
                    text = "ক্যামেরা মডেল নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                val models = listOf(
                    "Imou Cruiser SE+ 4MP",
                    "Imou Ranger 2 360°",
                    "Imou Bullet 2E",
                    "Imou Rex 3D",
                    "Imou Versa / Cue 2",
                    "Dahua / ONVIF NVR"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    models.forEach { m ->
                        FilterChip(
                            selected = modelName == m,
                            onClick = {
                                modelName = m
                                supportsPtz = !m.contains("Bullet") && !m.contains("Cue") && !m.contains("Versa")
                            },
                            label = { Text(m, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyanContainer,
                                selectedLabelColor = CyberCyan,
                                containerColor = PitchBlack,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = CyberCyan,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPureWhite,
                    unfocusedTextColor = TextPureWhite,
                    cursorColor = CyberCyan,
                    focusedContainerColor = PitchBlack,
                    unfocusedContainerColor = PitchBlack
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ক্যামেরার নাম (যেমন: মেইন গেট, ড্রয়িং রুম)") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_camera_name")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("লোকাল LAN IP Address") },
                        placeholder = { Text("192.168.1.108") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier
                            .weight(1.6f)
                            .testTag("input_camera_ip")
                    )
                    OutlinedTextField(
                        value = rtspPortText,
                        onValueChange = { rtspPortText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("RTSP Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username (ডিফল্ট: admin)") },
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = httpPortText,
                        onValueChange = { httpPortText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("HTTP/ONVIF Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = safetyCode,
                    onValueChange = { safetyCode = it },
                    label = { Text("Safety Code / ক্যামেরা পাসওয়ার্ড (8-char)") },
                    placeholder = { Text("ক্যামেরার গায়ের স্টিকারে লেখা Safety Code") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Safety Code Visibility",
                                tint = CyberCyan
                            )
                        }
                    },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_safety_code")
                )

                // Helpful Imou Safety Code Tip Box
                Surface(
                    color = PitchBlack,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "টিপস: Imou ক্যামেরার নিচে QR স্টিকারে ৮-অক্ষরের 'Safety Code' লেখা থাকে, অথবা Imou Life অ্যাপে Device Settings -> Device Information -> Device Password থেকে পাবেন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                // Stream Type & Channel Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = channelText,
                        onValueChange = { channelText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("চ্যানেল (1..16)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = textFieldColors,
                        modifier = Modifier.weight(0.9f)
                    )

                    Column(modifier = Modifier.weight(1.5f)) {
                        Text("ডিফল্ট স্ট্রিম কোয়ালিটি:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = preferredSubtype == 0,
                                onClick = { preferredSubtype = 0 },
                                label = { Text("Main (2K/HD)", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyanContainer,
                                    selectedLabelColor = CyberCyan
                                )
                            )
                            FilterChip(
                                selected = preferredSubtype == 1,
                                onClick = { preferredSubtype = 1 },
                                label = { Text("Sub (SD ফাস্ট)", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberCyanContainer,
                                    selectedLabelColor = CyberCyan
                                )
                            )
                        }
                    }
                }

                // Capability Switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PTZ মোটর ও রোটেশন সাপোর্ট", style = MaterialTheme.typography.bodyMedium, color = TextPureWhite)
                    Switch(
                        checked = supportsPtz,
                        onCheckedChange = { supportsPtz = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PitchBlack, checkedTrackColor = CyberCyan)
                    )
                }

                // Live Generated RTSP URI Preview Card
                Surface(
                    color = PitchBlack,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "জেনারেটেড লোকাল RTSP URL:",
                                style = MaterialTheme.typography.labelSmall,
                                color = LanEmerald
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (showPassword) previewCamera.buildRtspUrl() else previewCamera.buildMaskedRtspUrl(),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = JetBrainsMonoFamily,
                                color = TextPureWhite
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(previewCamera.buildRtspUrl()))
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

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialCamera != null && onDelete != null) {
                        OutlinedButton(
                            onClick = { onDelete(initialCamera) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_delete_camera")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Camera", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মুছুন")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("বাতিল", color = TextSecondary)
                        }
                        Button(
                            onClick = { onSave(previewCamera) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = PitchBlack
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_save_camera")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সেভ করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavePtzPresetDialog(
    camera: ImouCameraEntity,
    nextPresetNumber: Int,
    onDismiss: () -> Unit,
    onConfirmSave: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("পজিশন #$nextPresetNumber") }
    var presetNumberText by remember { mutableStateOf(nextPresetNumber.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        titleContentColor = TextPureWhite,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "PTZ প্রিসেট মেমোরিতে সেভ করুন",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "বর্তমান অ্যাঙ্গেল: PAN ${camera.panAngle.toInt()}° • TILT ${camera.tiltAngle.toInt()}° • ZOOM ${"%.1f".format(camera.zoomLevel)}x",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = JetBrainsMonoFamily,
                    color = CyberCyan
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("প্রিসেটের নাম (যেমন: মেইন গেট, সিঁড়ি)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = presetNumberText,
                    onValueChange = { presetNumberText = it.filter { c -> c.isDigit() } },
                    label = { Text("প্রিসেট নম্বর (1 - 16)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = presetNumberText.toIntOrNull()?.coerceIn(1, 25) ?: nextPresetNumber
                    onConfirmSave(title.ifBlank { "Preset #$num" }, num)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LanEmerald, contentColor = PitchBlack)
            ) {
                Text("প্রিসেট সেভ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextSecondary)
            }
        }
    )
}
