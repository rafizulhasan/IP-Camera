package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ImouCameraRepository(private val dao: ImouCameraDao) {
    val allCameras: Flow<List<ImouCameraEntity>> = dao.getAllCameras()
    val allPresets: Flow<List<PtzPresetEntity>> = dao.getAllPresets()
    val allVaultEvents: Flow<List<VaultEventEntity>> = dao.getAllVaultEvents()

    suspend fun ensureDefaultCamerasSeeded(detectedSubnetPrefix: String = "192.168.1") {
        if (dao.getCameraCount() > 0) return

        val c1Id = dao.insertCamera(
            ImouCameraEntity(
                name = "মেইন গেট • Cruiser SE+",
                modelName = "Imou Cruiser SE+ 4MP PTZ",
                ipAddress = "$detectedSubnetPrefix.108",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "L28A9F4C",
                channel = 1,
                preferredSubtype = 0,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = true,
                supportsSiren = true,
                nightVisionMode = "SMART",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = true,
                zoneName = "প্রবেশদ্বার (Gate)",
                panAngle = -18f,
                tiltAngle = 12f,
                zoomLevel = 1.0f,
                isFavorite = true
            )
        )

        val c2Id = dao.insertCamera(
            ImouCameraEntity(
                name = "লিভিং রুম • Ranger 2",
                modelName = "Imou Ranger 2 2K 360°",
                ipAddress = "$detectedSubnetPrefix.109",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "K94D2E7B",
                channel = 1,
                preferredSubtype = 0,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = false,
                supportsSiren = true,
                nightVisionMode = "IR_BW",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = true,
                zoneName = "ইনডোর (Indoor)",
                panAngle = 35f,
                tiltAngle = 5f,
                zoomLevel = 1.2f,
                isFavorite = true
            )
        )

        val c3Id = dao.insertCamera(
            ImouCameraEntity(
                name = "ড্রাইভওয়ে • Bullet 2E",
                modelName = "Imou Bullet 2E Full-Color",
                ipAddress = "$detectedSubnetPrefix.110",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "M71C5X8A",
                channel = 1,
                preferredSubtype = 0,
                supportsPtz = false,
                supportsAudio = true,
                supportsSpotlight = true,
                supportsSiren = true,
                nightVisionMode = "FULL_COLOR",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = false,
                zoneName = "পার্কিং জোন (Parking)",
                panAngle = 0f,
                tiltAngle = 0f,
                zoomLevel = 1.0f,
                isFavorite = false
            )
        )

        val c4Id = dao.insertCamera(
            ImouCameraEntity(
                name = "ছাদ ও করিডোর • Rex 3D",
                modelName = "Imou Rex 3D 5MP Pan/Tilt",
                ipAddress = "$detectedSubnetPrefix.111",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "P63V8N2Q",
                channel = 1,
                preferredSubtype = 1,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = true,
                supportsSiren = true,
                nightVisionMode = "AUTO",
                motionAlertEnabled = true,
                humanDetectEnabled = false,
                smartTrackingEnabled = true,
                zoneName = "পেরিমিটার (Rooftop)",
                panAngle = -42f,
                tiltAngle = 20f,
                zoomLevel = 1.5f,
                isFavorite = false
            )
        )

        // Seed default PTZ presets for the PTZ cameras
        listOf(
            PtzPresetEntity(cameraId = c1Id, presetNumber = 1, title = "পকেট গেট (Main Entry)", panAngle = -35f, tiltAngle = 10f, zoomLevel = 1.2f),
            PtzPresetEntity(cameraId = c1Id, presetNumber = 2, title = "গাড়ি পার্কিং (Car Porch)", panAngle = 15f, tiltAngle = 18f, zoomLevel = 1.6f),
            PtzPresetEntity(cameraId = c1Id, presetNumber = 3, title = "সামনের রাস্তা (Street View)", panAngle = 68f, tiltAngle = 4f, zoomLevel = 2.0f),
            PtzPresetEntity(cameraId = c2Id, presetNumber = 1, title = "সোফা ও টিভি জোন", panAngle = 0f, tiltAngle = 5f, zoomLevel = 1.0f),
            PtzPresetEntity(cameraId = c2Id, presetNumber = 2, title = "বারান্দার দরজা", panAngle = -85f, tiltAngle = 12f, zoomLevel = 1.4f),
            PtzPresetEntity(cameraId = c4Id, presetNumber = 1, title = "সিঁড়ি ঘর (Staircase)", panAngle = -20f, tiltAngle = 15f, zoomLevel = 1.0f)
        ).forEach { dao.insertPreset(it) }

        // Seed initial LAN Event logs
        dao.insertVaultEvent(
            VaultEventEntity(
                cameraId = c1Id,
                cameraName = "মেইন গেট • Cruiser SE+",
                cameraIp = "$detectedSubnetPrefix.108",
                eventType = "CGI_COMMAND",
                title = "অফলাইন LAN ম্যাট্রিক্স প্রোফাইল প্রস্তুত",
                details = "RTSP Port 554 (TCP) এবং Dahua HTTP CGI / ONVIF লোকাল কন্ট্রোল সক্রিয়। রাউটারে ইন্টারনেট না থাকলেও লোকাল ওয়াইফাই দিয়ে সরাসরি স্ট্রিমিং ও PTZ কাজ করবে।",
                resolution = "2560x1440 (2K HEVC)"
            )
        )
    }

    suspend fun saveCamera(camera: ImouCameraEntity): Long = dao.insertCamera(camera)
    suspend fun updateCamera(camera: ImouCameraEntity) = dao.updateCamera(camera)
    suspend fun deleteCamera(camera: ImouCameraEntity) = dao.deleteCamera(camera)

    suspend fun savePreset(preset: PtzPresetEntity): Long = dao.insertPreset(preset)
    suspend fun deletePreset(presetId: Long) = dao.deletePreset(presetId)

    suspend fun logEvent(event: VaultEventEntity): Long = dao.insertVaultEvent(event)
    suspend fun deleteEvent(eventId: Long) = dao.deleteVaultEvent(eventId)
    suspend fun clearEvents() = dao.clearAllVaultEvents()
}
