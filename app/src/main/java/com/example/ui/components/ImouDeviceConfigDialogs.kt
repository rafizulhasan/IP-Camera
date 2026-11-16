package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImouOrange
import com.example.ui.theme.ImouOrangeContainer
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImouCameraEditorDialog(
    initialCamera: ImouCameraEntity?,
    defaultSubnetPrefix: String,
    prefillIp: String? = null,
    onDismiss: () -> Unit,
    onSave: (ImouCameraEntity) -> Unit,
    onDelete: ((ImouCameraEntity) -> Unit)? = null
) {
    val clipboard = LocalClipboardManager.current

    var name by remember { mutableStateOf(initialCamera?.name ?: "বারান্দা • Imou Dual") }
    var modelName by remember { mutableStateOf(initialCamera?.modelName ?: "Imou Cruiser Dual 2 (10MP)") }
    var roomName by remember { mutableStateOf(initialCamera?.roomName ?: "আউটডোর") }
    var ipAddress by remember {
        mutableStateOf(prefillIp ?: initialCamera?.ipAddress ?: "$defaultSubnetPrefix.112")
    }
    var rtspPortText by remember { mutableStateOf((initialCamera?.rtspPort ?: 554).toString()) }
    var httpPortText by remember { mutableStateOf((initialCamera?.httpPort ?: 80).toString()) }
    var username by remember { mutableStateOf(initialCamera?.username ?: "admin") }
    var safetyCode by remember { mutableStateOf(initialCamera?.safetyCode ?: "L28A9F4C") }
    var showPassword by remember { mutableStateOf(false) }

    var lensCount by remember { mutableIntStateOf(initialCamera?.lensCount ?: 2) }
    var lens1ChannelText by remember { mutableStateOf((initialCamera?.lens1Channel ?: 1).toString()) }
    var lens2ChannelText by remember { mutableStateOf((initialCamera?.lens2Channel ?: 2).toString()) }
    var lens3ChannelText by remember { mutableStateOf((initialCamera?.lens3Channel ?: 3).toString()) }
    var ptzChannelText by remember {
        mutableStateOf((initialCamera?.ptzControlChannel ?: if (lensCount == 3) 3 else if (lensCount == 2) 2 else 1).toString())
    }
    var preferredSubtype by remember { mutableIntStateOf(initialCamera?.preferredSubtype ?: 0) }

    val previewCamera = remember(
        name, modelName, roomName, ipAddress, rtspPortText, httpPortText, username,
        safetyCode, lensCount, lens1ChannelText, lens2ChannelText, lens3ChannelText,
        ptzChannelText, preferredSubtype
    ) {
        val ch1 = lens1ChannelText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val ch2 = lens2ChannelText.toIntOrNull()?.coerceAtLeast(1) ?: 2
        val ch3 = lens3ChannelText.toIntOrNull()?.coerceAtLeast(1) ?: 3
        val ptzCh = ptzChannelText.toIntOrNull()?.coerceAtLeast(1) ?: if (lensCount == 3) ch3 else if (lensCount == 2) ch2 else ch1

        ImouCameraEntity(
            id = initialCamera?.id ?: 0L,
            name = name.ifBlank { "Imou LAN Camera" },
            modelName = modelName,
            roomName = roomName.ifBlank { "আউটডোর" },
            zoneName = roomName.ifBlank { "Default" },
            ipAddress = ipAddress.ifBlank { "$defaultSubnetPrefix.108" },
            rtspPort = rtspPortText.toIntOrNull() ?: 554,
            httpPort = httpPortText.toIntOrNull() ?: 80,
            onvifPort = httpPortText.toIntOrNull() ?: 80,
            username = username.ifBlank { "admin" },
            safetyCode = safetyCode,
            channel = ch1,
            lensCount = lensCount,
            lens1Channel = ch1,
            lens2Channel = ch2,
            lens3Channel = ch3,
            lens1Name = if (lensCount == 3) "লেন্স ১ • বাম প্যানোরামা (Fixed)" else if (lensCount == 2) "লেন্স ১ • ফিক্সড ওয়াইড ভিউ" else "লেন্স ১ • 360° মেইন ভিউ",
            lens2Name = if (lensCount == 3) "লেন্স ২ • ডান প্যানোরামা (Fixed)" else "লেন্স ২ • 360° PTZ ট্র্যাকিং",
            lens3Name = "লেন্স ৩ • 360° PTZ ট্র্যাকিং",
            ptzControlChannel = ptzCh,
            preferredSubtype = preferredSubtype,
            nightVisionMode = initialCamera?.nightVisionMode ?: "SMART",
            panAngle = initialCamera?.panAngle ?: 0f,
            tiltAngle = initialCamera?.tiltAngle ?: 0f,
            zoomLevel = initialCamera?.zoomLevel ?: 1.0f,
            isFavorite = initialCamera?.isFavorite ?: true
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            color = ObsidianSurface,
            shape = RoundedCornerShape(22.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ImouOrange.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialCamera == null) "নতুন Imou ক্যামেরা যুক্ত করুন" else "ডিভাইস সেটিংস ও লেন্স কনফিগ",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPureWhite
                        )
                        Text(
                            text = "১-লেন্স, ২-লেন্স (Dual) বা ৩-লেন্স (Triple) লোকাল LAN সেটআপ",
                            style = MaterialTheme.typography.bodySmall,
                            color = ImouOrange
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = "ক্যামেরার লেন্স সংখ্যা নির্বাচন করুন (সব লেন্স একসাথে প্লে হবে):",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        3 to "3-Lens (ট্রিপল লেন্স)",
                        2 to "2-Lens (ডুয়াল লেন্স)",
                        1 to "1-Lens (সিঙ্গেল)"
                    ).forEach { (count, label) ->
                        val selected = lensCount == count
                        FilterChip(
                            selected = selected,
                            onClick = {
                                lensCount = count
                                ptzChannelText = count.toString()
                                modelName = when (count) {
                                    3 -> "Imou Cruiser Triple 3-Lens (12MP)"
                                    2 -> "Imou Cruiser Dual 2 (10MP)"
                                    else -> "Imou Ranger 2 / Rex 3D"
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
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

                val models = listOf(
                    "Imou Cruiser Triple 3-Lens (12MP)",
                    "Imou Cruiser Dual 2 (10MP)",
                    "Imou Ranger Dual (6MP)",
                    "Imou Bullet Dual",
                    "Imou Cruiser SE+ 4MP",
                    "Imou Ranger 2 / Rex 3D"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    models.forEach { m ->
                        FilterChip(
                            selected = modelName == m,
                            onClick = {
                                modelName = m
                                if (m.contains("Triple")) {
                                    lensCount = 3
                                    ptzChannelText = "3"
                                } else if (m.contains("Dual")) {
                                    lensCount = 2
                                    ptzChannelText = "2"
                                } else {
                                    lensCount = 1
                                    ptzChannelText = "1"
                                }
                            },
                            label = { Text(m, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ImouOrangeContainer,
                                selectedLabelColor = ImouOrange,
                                containerColor = PitchBlack,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                val tfColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ImouOrange,
                    unfocusedBorderColor = GlassBorder,
                    focusedLabelColor = ImouOrange,
                    unfocusedLabelColor = TextSecondary,
                    focusedTextColor = TextPureWhite,
                    unfocusedTextColor = TextPureWhite,
                    cursorColor = ImouOrange,
                    focusedContainerColor = PitchBlack,
                    unfocusedContainerColor = PitchBlack
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("ক্যামেরার নাম") },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("input_camera_name")
                    )
                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("রুম / জোন") },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("লোকাল LAN IP Address") },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("input_camera_ip")
                    )
                    OutlinedTextField(
                        value = rtspPortText,
                        onValueChange = { rtspPortText = it.filter { c -> c.isDigit() } },
                        label = { Text("RTSP Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("ইউজারনেম (admin)") },
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = httpPortText,
                        onValueChange = { httpPortText = it.filter { c -> c.isDigit() } },
                        label = { Text("HTTP/CGI Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = safetyCode,
                    onValueChange = { safetyCode = it },
                    label = { Text("Safety Code / ডিভাইস পাসওয়ার্ড") },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Show Password",
                                tint = ImouOrange
                            )
                        }
                    },
                    colors = tfColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_safety_code")
                )

                Text(
                    text = "লেন্স RTSP চ্যানেল ম্যাপিং (Dahua / Imou Multi-Channel):",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lens1ChannelText,
                        onValueChange = { lens1ChannelText = it.filter { c -> c.isDigit() } },
                        label = { Text("লেন্স ১ CH") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                    if (lensCount >= 2) {
                        OutlinedTextField(
                            value = lens2ChannelText,
                            onValueChange = { lens2ChannelText = it.filter { c -> c.isDigit() } },
                            label = { Text("লেন্স ২ CH") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = tfColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (lensCount >= 3) {
                        OutlinedTextField(
                            value = lens3ChannelText,
                            onValueChange = { lens3ChannelText = it.filter { c -> c.isDigit() } },
                            label = { Text("লেন্স ৩ CH") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = tfColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = ptzChannelText,
                        onValueChange = { ptzChannelText = it.filter { c -> c.isDigit() } },
                        label = { Text("PTZ CH") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = tfColors,
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = PitchBlack,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "মাল্টি-লেন্স লোকাল RTSP স্ট্রিম লিংক (${lensCount}টি লেন্স):",
                            style = MaterialTheme.typography.labelSmall,
                            color = LanEmerald
                        )
                        previewCamera.getLensDescriptors().forEach { l ->
                            val url = if (showPassword) {
                                previewCamera.buildRtspUrlForChannel(l.channel)
                            } else {
                                previewCamera.buildMaskedRtspUrl(l.channel)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "L${l.lensIndex}: $url",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = JetBrainsMonoFamily,
                                    color = TextPureWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        clipboard.setText(
                                            AnnotatedString(previewCamera.buildRtspUrlForChannel(l.channel))
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = ImouOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (initialCamera != null && onDelete != null) {
                        OutlinedButton(
                            onClick = { onDelete(initialCamera) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCrimson),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("মুছুন")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("বাতিল", color = TextSecondary)
                        }
                        Button(
                            onClick = { onSave(previewCamera) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImouOrange,
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
fun ImouSavePresetDialog(
    camera: ImouCameraEntity,
    nextPresetNumber: Int,
    onDismiss: () -> Unit,
    onConfirmSave: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("পয়েন্ট #$nextPresetNumber") }
    var presetNumberText by remember { mutableStateOf(nextPresetNumber.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        titleContentColor = TextPureWhite,
        textContentColor = TextSecondary,
        title = {
            Text("কালেকশন পয়েন্ট (Favorite Angle) সেভ করুন", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PTZ লেন্স পজিশন: PAN ${camera.panAngle.toInt()}° • TILT ${camera.tiltAngle.toInt()}° • ZOOM ${"%.1f".format(camera.zoomLevel)}x",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = JetBrainsMonoFamily,
                    color = ImouOrange
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("পয়েন্টের নাম (যেমন: মেইন গেট, গ্যারেজ)") },
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
                    onConfirmSave(title.ifBlank { "Point #$num" }, num)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ImouOrange, contentColor = PitchBlack)
            ) {
                Text("সেভ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextSecondary)
            }
        }
    )
}
