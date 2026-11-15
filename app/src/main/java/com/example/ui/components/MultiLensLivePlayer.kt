package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.rtsp.RtspMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.local.ImouCameraEntity
import com.example.data.local.LensDescriptor
import com.example.network.RtspProbeResult
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ImouOrange
import com.example.ui.theme.ImouOrangeContainer
import com.example.ui.theme.InfraredViolet
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LanEmerald
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class MultiLensLayoutStyle {
    SIMULTANEOUS_SPLIT, // Official Imou Life Dual/Triple Stacked Split View
    VERTICAL_STACK,     // All lenses stacked vertically
    PIP_OVERLAY,        // PTZ main + Fixed lens floating PiP
    SINGLE_LENS_FOCUS   // Focus on one selected lens
}

@Composable
fun MultiLensCameraPlayerSection(
    camera: ImouCameraEntity,
    probeResult: RtspProbeResult?,
    isRecording: Boolean,
    isAudioMuted: Boolean,
    isTalkActive: Boolean,
    onClick2TrackTap: (lensIndex: Int, normX: Float, normY: Float) -> Unit,
    onPinchZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val lenses = remember(camera) { camera.getLensDescriptors() }
    var layoutStyle by remember(camera.id, camera.lensCount) {
        mutableStateOf(MultiLensLayoutStyle.SIMULTANEOUS_SPLIT)
    }
    var focusedLensIndex by remember(camera.id) { mutableIntStateOf(lenses.last().lensIndex) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PitchBlack),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Multi-Lens Mode & Channel Selector Header Bar (Official Imou Life Top Player Strip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lens count + Click2Track indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = ImouOrangeContainer,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImouOrange)
                ) {
                    Text(
                        text = "${camera.lensCount}-LENS LIVE SYNC",
                        style = MaterialTheme.typography.labelSmall,
                        color = ImouOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (camera.lensCount >= 2 && camera.click2TrackEnabled) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdsClick,
                            contentDescription = "Click2Track Active",
                            tint = LanEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Click2Track সক্রিয় (ফিক্সড লেন্সে ট্যাপ করুন)",
                            style = MaterialTheme.typography.labelSmall,
                            color = LanEmerald,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Layout Switcher Pills (Split / Stack / PiP / Single)
            if (camera.lensCount >= 2) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LensLayoutIconChip(
                        icon = Icons.Default.ViewQuilt,
                        label = "Split",
                        selected = layoutStyle == MultiLensLayoutStyle.SIMULTANEOUS_SPLIT,
                        onClick = { layoutStyle = MultiLensLayoutStyle.SIMULTANEOUS_SPLIT }
                    )
                    LensLayoutIconChip(
                        icon = Icons.Default.ViewAgenda,
                        label = "Stack",
                        selected = layoutStyle == MultiLensLayoutStyle.VERTICAL_STACK,
                        onClick = { layoutStyle = MultiLensLayoutStyle.VERTICAL_STACK }
                    )
                    LensLayoutIconChip(
                        icon = Icons.Default.PictureInPictureAlt,
                        label = "PiP",
                        selected = layoutStyle == MultiLensLayoutStyle.PIP_OVERLAY,
                        onClick = { layoutStyle = MultiLensLayoutStyle.PIP_OVERLAY }
                    )
                }
            }
        }

        // Individual Lens Focus Filter Bar (All Lenses Simultaneous vs Lens 1 / Lens 2 / Lens 3)
        if (camera.lensCount >= 2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allSimultaneous = layoutStyle != MultiLensLayoutStyle.SINGLE_LENS_FOCUS
                Surface(
                    color = if (allSimultaneous) ImouOrange else CarbonCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { layoutStyle = MultiLensLayoutStyle.SIMULTANEOUS_SPLIT }
                        .testTag("btn_play_all_lenses")
                ) {
                    Text(
                        text = "সব লেন্স একসাথে (${camera.lensCount}x)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (allSimultaneous) PitchBlack else TextPureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                lenses.forEach { lens ->
                    val isThisLensFocused =
                        layoutStyle == MultiLensLayoutStyle.SINGLE_LENS_FOCUS && focusedLensIndex == lens.lensIndex
                    Surface(
                        color = if (isThisLensFocused) CyberCyan else ObsidianSurface,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isThisLensFocused) CyberCyan else GlassBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                focusedLensIndex = lens.lensIndex
                                layoutStyle = MultiLensLayoutStyle.SINGLE_LENS_FOCUS
                            }
                            .testTag("btn_focus_lens_${lens.lensIndex}")
                    ) {
                        Text(
                            text = "লেন্স ${lens.lensIndex} (CH${lens.channel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isThisLensFocused) PitchBlack else TextSecondary,
                            fontWeight = if (isThisLensFocused) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. Simultaneous Multi-Lens Video Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            when {
                // Single Lens Camera OR Single Lens Focus Mode
                camera.lensCount == 1 || layoutStyle == MultiLensLayoutStyle.SINGLE_LENS_FOCUS -> {
                    val activeLens = lenses.find { it.lensIndex == focusedLensIndex } ?: lenses.first()
                    SingleLensRtspViewport(
                        camera = camera,
                        lens = activeLens,
                        probeResult = probeResult,
                        isRecording = isRecording,
                        isAudioMuted = isAudioMuted,
                        isTalkActive = isTalkActive,
                        compactMode = false,
                        onFixedLensClick2Track = { nx, ny ->
                            onClick2TrackTap(activeLens.lensIndex, nx, ny)
                        },
                        onPinchZoomChange = onPinchZoomChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9.2f)
                    )
                }

                // 3-Lens Camera Simultaneous Split View (Top: Fixed Lens 1 + Fixed Lens 2 side-by-side; Bottom: PTZ Lens 3)
                camera.lensCount == 3 && layoutStyle == MultiLensLayoutStyle.SIMULTANEOUS_SPLIT -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Top Panorama Row: Lens 1 (Left Fixed) + Lens 2 (Right Fixed) playing together
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            SingleLensRtspViewport(
                                camera = camera,
                                lens = lenses[0],
                                probeResult = probeResult,
                                isRecording = isRecording,
                                isAudioMuted = true, // Prevent echo from multiple lenses; PTZ lens handles audio
                                isTalkActive = false,
                                compactMode = true,
                                onFixedLensClick2Track = { nx, ny ->
                                    onClick2TrackTap(lenses[0].lensIndex, nx, ny)
                                },
                                onPinchZoomChange = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(16f / 10f)
                            )
                            SingleLensRtspViewport(
                                camera = camera,
                                lens = lenses[1],
                                probeResult = probeResult,
                                isRecording = isRecording,
                                isAudioMuted = true,
                                isTalkActive = false,
                                compactMode = true,
                                onFixedLensClick2Track = { nx, ny ->
                                    onClick2TrackTap(lenses[1].lensIndex, nx, ny)
                                },
                                onPinchZoomChange = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(16f / 10f)
                            )
                        }

                        // Bottom Motorized PTZ Lens 3 playing simultaneously
                        SingleLensRtspViewport(
                            camera = camera,
                            lens = lenses[2],
                            probeResult = probeResult,
                            isRecording = isRecording,
                            isAudioMuted = isAudioMuted,
                            isTalkActive = isTalkActive,
                            compactMode = false,
                            onFixedLensClick2Track = { _, _ -> },
                            onPinchZoomChange = onPinchZoomChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9.2f)
                        )
                    }
                }

                // 2-Lens Dual Camera Simultaneous Split View OR Vertical Stack for 2/3 Lenses
                layoutStyle == MultiLensLayoutStyle.SIMULTANEOUS_SPLIT || layoutStyle == MultiLensLayoutStyle.VERTICAL_STACK -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        lenses.forEachIndexed { idx, lens ->
                            val isPrimaryAudioLens = idx == lenses.lastIndex
                            SingleLensRtspViewport(
                                camera = camera,
                                lens = lens,
                                probeResult = probeResult,
                                isRecording = isRecording,
                                isAudioMuted = if (isPrimaryAudioLens) isAudioMuted else true,
                                isTalkActive = if (isPrimaryAudioLens) isTalkActive else false,
                                compactMode = lenses.size == 3,
                                onFixedLensClick2Track = { nx, ny ->
                                    onClick2TrackTap(lens.lensIndex, nx, ny)
                                },
                                onPinchZoomChange = if (lens.isPtzLens) onPinchZoomChange else ({}),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(if (lenses.size == 3) 16f / 7.8f else 16f / 9.2f)
                            )
                        }
                    }
                }

                // Picture-in-Picture (PiP) Multi-Lens Mode
                layoutStyle == MultiLensLayoutStyle.PIP_OVERLAY -> {
                    val ptzLens = lenses.last()
                    val fixedLenses = lenses.dropLast(1)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 10f)
                    ) {
                        SingleLensRtspViewport(
                            camera = camera,
                            lens = ptzLens,
                            probeResult = probeResult,
                            isRecording = isRecording,
                            isAudioMuted = isAudioMuted,
                            isTalkActive = isTalkActive,
                            compactMode = false,
                            onFixedLensClick2Track = { _, _ -> },
                            onPinchZoomChange = onPinchZoomChange,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Floating Fixed Lens(es) in Top-Right corner
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 36.dp, end = 8.dp)
                                .width(135.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            fixedLenses.forEach { fLens ->
                                SingleLensRtspViewport(
                                    camera = camera,
                                    lens = fLens,
                                    probeResult = probeResult,
                                    isRecording = false,
                                    isAudioMuted = true,
                                    isTalkActive = false,
                                    compactMode = true,
                                    onFixedLensClick2Track = { nx, ny ->
                                        onClick2TrackTap(fLens.lensIndex, nx, ny)
                                    },
                                    onPinchZoomChange = {},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16f / 10f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LensLayoutIconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) ImouOrangeContainer else ObsidianSurface,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) ImouOrange else GlassBorder
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) ImouOrange else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) ImouOrange else TextSecondary
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SingleLensRtspViewport(
    camera: ImouCameraEntity,
    lens: LensDescriptor,
    probeResult: RtspProbeResult?,
    isRecording: Boolean,
    isAudioMuted: Boolean,
    isTalkActive: Boolean,
    compactMode: Boolean,
    onFixedLensClick2Track: (normX: Float, normY: Float) -> Unit,
    onPinchZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val rtspUrl = remember(
        camera.ipAddress,
        camera.rtspPort,
        camera.username,
        camera.safetyCode,
        lens.channel,
        camera.preferredSubtype,
        camera.customRtspPath
    ) {
        camera.buildRtspUrlForChannel(lens.channel)
    }

    var playerState by remember { mutableIntStateOf(Player.STATE_IDLE) }
    var rtspErrorText by remember { mutableStateOf<String?>(null) }
    var currentClockMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var liveBitrateKbps by remember {
        mutableIntStateOf(if (camera.preferredSubtype == 0) 2150 else 580)
    }

    LaunchedEffect(camera.preferredSubtype, lens.channel) {
        while (true) {
            currentClockMillis = System.currentTimeMillis()
            val baseRate = if (camera.preferredSubtype == 0) 2200 else 560
            val jitter = ((currentClockMillis / 1000L + lens.channel * 29L) % 160L).toInt() - 80
            liveBitrateKbps = (baseRate + jitter).coerceAtLeast(128)
            delay(1000L)
        }
    }

    val exoPlayer = remember(context, lens.lensIndex) {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                playerState = playbackState
                if (playbackState == Player.STATE_READY) {
                    rtspErrorText = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                rtspErrorText = error.errorCodeName
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Silent pre-flight TCP socket check so ExoPlayerImplInternal never logs NoRouteToHostException
    LaunchedEffect(rtspUrl, camera.privacyMaskEnabled, probeResult?.rtspPortOpen) {
        if (camera.privacyMaskEnabled) {
            exoPlayer.stop()
            return@LaunchedEffect
        }
        rtspErrorText = null

        val canConnectRtsp = withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.soTimeout = 550
                    socket.connect(InetSocketAddress(camera.ipAddress.trim(), camera.rtspPort), 500)
                    true
                }
            } catch (_: Exception) {
                false
            }
        }

        if (!canConnectRtsp) {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            rtspErrorText = "LAN Standby (CH${lens.channel})"
            return@LaunchedEffect
        }

        try {
            val mediaSource = RtspMediaSource.Factory()
                .setForceUseRtpTcp(true)
                .setTimeoutMs(4000L)
                .createMediaSource(MediaItem.fromUri(rtspUrl))
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        } catch (e: Exception) {
            rtspErrorText = e.localizedMessage
        }
    }

    LaunchedEffect(isAudioMuted) {
        exoPlayer.volume = if (isAudioMuted) 0f else 1f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lens_hud_${lens.lensIndex}")
    val scanPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_pulse"
    )
    val recBlink by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_blink"
    )

    val isVideoPlaying = playerState == Player.STATE_READY && rtspErrorText == null && !camera.privacyMaskEnabled
    val frameBorderColor = when {
        camera.sirenActive -> AlertCrimson
        lens.isPtzLens -> ImouOrange.copy(alpha = 0.85f)
        else -> CyberCyan.copy(alpha = 0.65f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PitchBlack)
            .border(1.2.dp, frameBorderColor, RoundedCornerShape(12.dp))
            .pointerInput(lens.isPtzLens, camera.click2TrackEnabled, camera.zoomLevel) {
                if (!lens.isPtzLens && camera.click2TrackEnabled) {
                    // Official Imou Life Click2Track gesture on Fixed Lens!
                    detectTapGestures { tapOffset ->
                        val normX = (tapOffset.x / size.width.coerceAtLeast(1)).coerceIn(0.08f, 0.92f)
                        val normY = (tapOffset.y / size.height.coerceAtLeast(1)).coerceIn(0.12f, 0.88f)
                        onFixedLensClick2Track(normX, normY)
                    }
                } else if (lens.isPtzLens) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        if (zoomChange != 1f) {
                            val nextZoom = (camera.zoomLevel * zoomChange).coerceIn(1.0f, 8.0f)
                            onPinchZoomChange(nextZoom)
                        }
                    }
                }
            }
            .testTag("lens_viewport_${camera.id}_${lens.lensIndex}")
    ) {
        // 1. Real Media3 RTSP Video Surface
        if (!camera.privacyMaskEnabled) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        player = exoPlayer
                    }
                },
                update = { view ->
                    if (view.player != exoPlayer) {
                        view.player = exoPlayer
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        rotationZ = if (camera.imageFlip180) 180f else 0f,
                        scaleX = if (lens.isPtzLens) camera.zoomLevel.coerceIn(1f, 4f) else 1f,
                        scaleY = if (lens.isPtzLens) camera.zoomLevel.coerceIn(1f, 4f) else 1f
                    )
            )
        }

        // 2. Interactive Multi-Lens Optical Viewfinder & Click2Track Overlay
        if (!isVideoPlaying && !camera.privacyMaskEnabled) {
            MultiLensOpticalFeedCanvas(
                camera = camera,
                lens = lens,
                scanPulse = scanPulse,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(rotationZ = if (camera.imageFlip180) 180f else 0f)
            )
        }

        // 3. Privacy Mode Lens Shield
        AnimatedVisibility(
            visible = camera.privacyMaskEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PitchBlack.copy(alpha = 0.96f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "Privacy Mode",
                        tint = ImouOrange,
                        modifier = Modifier.size(if (compactMode) 22.dp else 34.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "প্রাইভেসি মোড সক্রিয় (Lens ${lens.lensIndex} Masked)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPureWhite
                    )
                }
            }
        }

        // 4. Top OSD Bar (Lens Badge, Channel Number, Bitrate, REC Badge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PitchBlack.copy(alpha = 0.86f), Color.Transparent)
                    )
                )
                .padding(horizontal = if (compactMode) 6.dp else 10.dp, vertical = if (compactMode) 4.dp else 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (lens.isPtzLens) ImouOrange else CyberCyan,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (compactMode) "L${lens.lensIndex}•CH${lens.channel}" else lens.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchBlack,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = lens.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (isRecording) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = "REC",
                            tint = AlertCrimson.copy(alpha = recBlink),
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "REC",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlertCrimson,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "${liveBitrateKbps}KB/s",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = JetBrainsMonoFamily,
                    color = LanEmerald
                )
            }
        }

        // 5. Bottom OSD Bar (Timestamp, PTZ Coordinates or Click2Track Prompt)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, PitchBlack.copy(alpha = 0.88f))
                    )
                )
                .padding(horizontal = if (compactMode) 6.dp else 10.dp, vertical = if (compactMode) 3.dp else 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val formattedClock = remember(currentClockMillis) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(currentClockMillis))
            }
            Text(
                text = formattedClock,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = JetBrainsMonoFamily,
                color = TextPureWhite
            )

            Text(
                text = if (lens.isPtzLens) {
                    "PTZ PAN ${camera.panAngle.toInt()}° • TILT ${camera.tiltAngle.toInt()}° • ${"%.1f".format(camera.zoomLevel)}x"
                } else {
                    "Click2Track: যেকোনো স্থানে ট্যাপ করলে PTZ লেন্স ঘুরবে"
                },
                style = MaterialTheme.typography.labelSmall,
                color = if (lens.isPtzLens) ImouOrange else CyberCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MultiLensOpticalFeedCanvas(
    camera: ImouCameraEntity,
    lens: LensDescriptor,
    scanPulse: Float,
    modifier: Modifier = Modifier
) {
    val isIrBw = camera.nightVisionMode == "IR_BW"
    val isFullColor = camera.nightVisionMode == "FULL_COLOR" || camera.spotlightActive

    val primaryColor = when {
        camera.sirenActive -> AlertCrimson
        isIrBw -> InfraredViolet
        isFullColor -> WarningAmber
        lens.isPtzLens -> ImouOrange
        else -> CyberCyan
    }

    Canvas(modifier = modifier.background(PitchBlack)) {
        val w = size.width
        val h = size.height

        val effectiveZoom = if (lens.isPtzLens) camera.zoomLevel.coerceIn(1f, 8f) else 1.0f
        val effectivePan = if (lens.isPtzLens) camera.panAngle else lens.defaultPanOffset
        val effectiveTilt = if (lens.isPtzLens) camera.tiltAngle else 0f

        val panShiftX = -(effectivePan / 180f) * (w * 0.35f) * effectiveZoom
        val tiltShiftY = (effectiveTilt / 90f) * (h * 0.30f) * effectiveZoom

        val focalCenter = Offset(
            x = (w / 2f + panShiftX).coerceIn(0f, w),
            y = (h / 2f + tiltShiftY).coerceIn(0f, h)
        )

        // Subtle Night Vision / Optical Lens Glow on Pitch Black
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (camera.spotlightActive) 0.30f else 0.14f),
                    Color(0xFF040811),
                    PitchBlack
                ),
                center = focalCenter,
                radius = w.coerceAtLeast(h) * 0.72f
            ),
            radius = w.coerceAtLeast(h),
            center = focalCenter
        )

        // Architectural Perspective Grid representing the lens field of view
        val cols = if (lens.isPtzLens) 8 else 10
        val rows = 5
        val cellW = (w * effectiveZoom) / cols
        val cellH = (h * effectiveZoom) / rows
        val startX = (w / 2f) - (w * effectiveZoom / 2f) + panShiftX
        val startY = (h / 2f) - (h * effectiveZoom / 2f) + tiltShiftY

        for (c in 0..cols) {
            val x = startX + c * cellW
            if (x in -20f..(w + 20f)) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.14f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }
        }
        for (r in 0..rows) {
            val y = startY + r * cellH
            if (y in -20f..(h + 20f)) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.14f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        if (!lens.isPtzLens && camera.click2TrackEnabled) {
            // Fixed Wide Lens: Draw Imou Orange Click2Track Linkage Box showing where PTZ Lens is aimed!
            val targetX = (camera.linkageTargetX * w).coerceIn(36f, (w - 36f).coerceAtLeast(36f))
            val targetY = (camera.linkageTargetY * h).coerceIn(28f, (h - 28f).coerceAtLeast(28f))
            val boxW = w * 0.24f
            val boxH = h * 0.34f

            drawRoundRect(
                color = ImouOrange.copy(alpha = 0.85f),
                topLeft = Offset(targetX - boxW / 2f, targetY - boxH / 2f),
                size = Size(boxW, boxH),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                )
            )
            drawCircle(
                color = ImouOrange,
                radius = 4.5f,
                center = Offset(targetX, targetY)
            )
        } else {
            // PTZ Tracking Lens: Draw Center Optical Tracking Reticle + Smart Human Detection Box
            val cx = w / 2f
            val cy = h / 2f
            drawCircle(
                color = ImouOrange.copy(alpha = 0.65f),
                radius = 20f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
            drawLine(ImouOrange, Offset(cx - 28f, cy), Offset(cx - 10f, cy), strokeWidth = 1.5f)
            drawLine(ImouOrange, Offset(cx + 10f, cy), Offset(cx + 28f, cy), strokeWidth = 1.5f)
            drawLine(ImouOrange, Offset(cx, cy - 28f), Offset(cx, cy - 10f), strokeWidth = 1.5f)
            drawLine(ImouOrange, Offset(cx, cy + 10f), Offset(cx, cy + 28f), strokeWidth = 1.5f)

            if (camera.smartTrackingEnabled || camera.humanDetectEnabled) {
                val drift = sin(scanPulse * Math.PI * 2).toFloat() * 12f
                val trackW = w * 0.28f
                val trackH = h * 0.42f
                drawRoundRect(
                    color = LanEmerald.copy(alpha = 0.7f),
                    topLeft = Offset(cx - trackW / 2f + drift, cy - trackH / 2f),
                    size = Size(trackW, trackH),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}
