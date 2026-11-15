package com.example.data.local

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imou_cameras")
data class ImouCameraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val modelName: String,
    val ipAddress: String,
    val rtspPort: Int = 554,
    val httpPort: Int = 80,
    val onvifPort: Int = 80,
    val username: String = "admin",
    val safetyCode: String,
    val channel: Int = 1,
    val preferredSubtype: Int = 0, // 0 = Main Stream (HD/2K), 1 = Sub Stream (SD)
    val customRtspPath: String = "/cam/realmonitor?channel={ch}&subtype={sub}",
    val supportsPtz: Boolean = true,
    val supportsAudio: Boolean = true,
    val supportsSpotlight: Boolean = true,
    val supportsSiren: Boolean = true,
    val nightVisionMode: String = "AUTO", // AUTO, FULL_COLOR, IR_BW, SMART
    val motionAlertEnabled: Boolean = true,
    val humanDetectEnabled: Boolean = true,
    val smartTrackingEnabled: Boolean = false,
    val privacyMaskEnabled: Boolean = false,
    val imageFlip180: Boolean = false,
    val spotlightActive: Boolean = false,
    val sirenActive: Boolean = false,
    val zoneName: String = "Perimeter",
    val panAngle: Float = 0f,
    val tiltAngle: Float = 0f,
    val zoomLevel: Float = 1.0f,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun buildRtspUrl(subtypeOverride: Int? = null): String {
        val sub = subtypeOverride ?: preferredSubtype
        val encodedUser = Uri.encode(username.trim())
        val encodedPass = Uri.encode(safetyCode.trim())
        val normalizedPath = customRtspPath
            .replace("{ch}", channel.toString())
            .replace("{sub}", sub.toString())
            .let { if (it.startsWith("/")) it else "/$it" }
        val authPart = if (safetyCode.isNotBlank()) "$encodedUser:$encodedPass@" else "$encodedUser@"
        return "rtsp://$authPart${ipAddress.trim()}:$rtspPort$normalizedPath"
    }

    fun buildMaskedRtspUrl(subtypeOverride: Int? = null): String {
        val sub = subtypeOverride ?: preferredSubtype
        val normalizedPath = customRtspPath
            .replace("{ch}", channel.toString())
            .replace("{sub}", sub.toString())
            .let { if (it.startsWith("/")) it else "/$it" }
        val maskedPass = if (safetyCode.isNotBlank()) "••••••••" else "(empty)"
        return "rtsp://${username.trim()}:$maskedPass@${ipAddress.trim()}:$rtspPort$normalizedPath"
    }

    fun buildSnapshotCgiUrl(): String {
        return "http://${ipAddress.trim()}:$httpPort/cgi-bin/snapshot.cgi?channel=$channel"
    }

    fun buildOnvifServiceUrl(): String {
        return "http://${ipAddress.trim()}:$onvifPort/onvif/device_service"
    }
}

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
    val eventType: String, // SNAPSHOT, RECORDING, SIREN_TRIGGER, PTZ_PRESET, CGI_COMMAND, LAN_SCAN
    val title: String,
    val details: String,
    val resolution: String = "2560x1440 (2K HEVC)",
    val timestamp: Long = System.currentTimeMillis()
)
