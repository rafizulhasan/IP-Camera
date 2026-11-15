package com.example.data.local

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imou_cameras")
data class ImouCameraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val modelName: String,
    val roomName: String = "আউটডোর",
    val ipAddress: String,
    val rtspPort: Int = 554,
    val httpPort: Int = 80,
    val onvifPort: Int = 80,
    val username: String = "admin",
    val safetyCode: String,
    // Multi-Lens Configuration (1, 2, or 3 lenses playing simultaneously)
    val lensCount: Int = 2, // 1 = Single PTZ/Fixed, 2 = Dual Lens (Fixed + PTZ), 3 = Triple Lens (2x Fixed Wide + 1x PTZ)
    val lens1Channel: Int = 1,
    val lens2Channel: Int = 2,
    val lens3Channel: Int = 3,
    val lens1Name: String = "লেন্স ১ • ফিক্সড ওয়াইড ভিউ",
    val lens2Name: String = "লেন্স ২ • 360° PTZ ট্র্যাকিং",
    val lens3Name: String = "লেন্স ৩ • PTZ জুম লেন্স",
    val ptzControlChannel: Int = 1,
    val preferredSubtype: Int = 0, // 0 = Main Stream (2K/HD), 1 = Sub Stream (SD)
    val customRtspPath: String = "/cam/realmonitor?channel={ch}&subtype={sub}",
    val supportsPtz: Boolean = true,
    val supportsAudio: Boolean = true,
    val supportsSpotlight: Boolean = true,
    val supportsSiren: Boolean = true,
    val nightVisionMode: String = "SMART", // SMART, FULL_COLOR, IR_BW, OFF
    val motionAlertEnabled: Boolean = true,
    val humanDetectEnabled: Boolean = true,
    val smartTrackingEnabled: Boolean = true,
    val click2TrackEnabled: Boolean = true,
    val autoCruiseEnabled: Boolean = false,
    val privacyMaskEnabled: Boolean = false,
    val imageFlip180: Boolean = false,
    val spotlightActive: Boolean = false,
    val sirenActive: Boolean = false,
    val panAngle: Float = 0f,
    val tiltAngle: Float = 0f,
    val zoomLevel: Float = 1.0f,
    // Click2Track Normalized Coordinates on Fixed Lens (0.0 .. 1.0)
    val linkageTargetX: Float = 0.5f,
    val linkageTargetY: Float = 0.5f,
    val isFavorite: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun buildRtspUrlForChannel(channelNumber: Int, subtypeOverride: Int? = null): String {
        val sub = subtypeOverride ?: preferredSubtype
        val encodedUser = Uri.encode(username.trim())
        val encodedPass = Uri.encode(safetyCode.trim())
        val normalizedPath = customRtspPath
            .replace("{ch}", channelNumber.toString())
            .replace("{sub}", sub.toString())
            .let { if (it.startsWith("/")) it else "/$it" }
        val authPart = if (safetyCode.isNotBlank()) "$encodedUser:$encodedPass@" else "$encodedUser@"
        return "rtsp://$authPart${ipAddress.trim()}:$rtspPort$normalizedPath"
    }

    fun buildRtspUrl(subtypeOverride: Int? = null): String {
        return buildRtspUrlForChannel(lens1Channel, subtypeOverride)
    }

    fun buildMaskedRtspUrl(channelNumber: Int = lens1Channel, subtypeOverride: Int? = null): String {
        val sub = subtypeOverride ?: preferredSubtype
        val normalizedPath = customRtspPath
            .replace("{ch}", channelNumber.toString())
            .replace("{sub}", sub.toString())
            .let { if (it.startsWith("/")) it else "/$it" }
        val maskedPass = if (safetyCode.isNotBlank()) "••••••••" else "(empty)"
        return "rtsp://${username.trim()}:$maskedPass@${ipAddress.trim()}:$rtspPort$normalizedPath"
    }

    fun buildSnapshotCgiUrl(channelNumber: Int = ptzControlChannel): String {
        return "http://${ipAddress.trim()}:$httpPort/cgi-bin/snapshot.cgi?channel=$channelNumber"
    }

    fun getLensDescriptors(): List<LensDescriptor> {
        return when (lensCount.coerceIn(1, 3)) {
            3 -> listOf(
                LensDescriptor(
                    lensIndex = 1,
                    channel = lens1Channel,
                    title = lens1Name,
                    badge = "FIXED LENS 1 (বাম)",
                    isPtzLens = false,
                    defaultPanOffset = -45f
                ),
                LensDescriptor(
                    lensIndex = 2,
                    channel = lens2Channel,
                    title = lens2Name,
                    badge = "FIXED LENS 2 (ডান)",
                    isPtzLens = false,
                    defaultPanOffset = 45f
                ),
                LensDescriptor(
                    lensIndex = 3,
                    channel = lens3Channel,
                    title = lens3Name,
                    badge = "PTZ TRACKING LENS 3",
                    isPtzLens = true,
                    defaultPanOffset = 0f
                )
            )
            2 -> listOf(
                LensDescriptor(
                    lensIndex = 1,
                    channel = lens1Channel,
                    title = lens1Name,
                    badge = "FIXED WIDE LENS 1",
                    isPtzLens = false,
                    defaultPanOffset = 0f
                ),
                LensDescriptor(
                    lensIndex = 2,
                    channel = lens2Channel,
                    title = lens2Name,
                    badge = "PTZ DOMED LENS 2",
                    isPtzLens = true,
                    defaultPanOffset = 0f
                )
            )
            else -> listOf(
                LensDescriptor(
                    lensIndex = 1,
                    channel = lens1Channel,
                    title = lens1Name,
                    badge = if (supportsPtz) "PTZ LENS" else "FIXED LENS",
                    isPtzLens = supportsPtz,
                    defaultPanOffset = 0f
                )
            )
        }
    }
}

data class LensDescriptor(
    val lensIndex: Int,
    val channel: Int,
    val title: String,
    val badge: String,
    val isPtzLens: Boolean,
    val defaultPanOffset: Float
)

@Entity(tableName = "ptz_presets")
data class PtzPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cameraId: Long,
    val presetNumber: Int,
    val title: String,
    val panAngle: Float,
    val tiltAngle: Float,
    val zoomLevel: Float = 1.0f
)

@Entity(tableName = "vault_events")
data class VaultEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cameraId: Long,
    val cameraName: String,
    val cameraIp: String,
    val eventType: String, // SNAPSHOT, RECORDING, SIREN_TRIGGER, PTZ_PRESET, CLICK2TRACK, CGI_COMMAND
    val title: String,
    val details: String,
    val resolution: String = "Multi-Lens 2K HEVC",
    val timestamp: Long = System.currentTimeMillis()
)
