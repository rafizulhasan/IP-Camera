package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ImouCameraRepository(private val dao: ImouCameraDao) {
    val allCameras: Flow<List<ImouCameraEntity>> = dao.getAllCameras()
    val allPresets: Flow<List<PtzPresetEntity>> = dao.getAllPresets()
    val allVaultEvents: Flow<List<VaultEventEntity>> = dao.getAllVaultEvents()

    suspend fun ensureDefaultCamerasSeeded(detectedSubnetPrefix: String = "192.168.1") {
        if (dao.getCameraCount() > 0) return

        // 1. 3-Lens Flagship Camera (2 Fixed Wide Lenses + 1 Motorized PTZ Lens)
        val c1Id = dao.insertCamera(
            ImouCameraEntity(
                name = "মেইন গেট ও বাউন্ডারি",
                modelName = "Imou Cruiser Triple 3-Lens (12MP)",
                roomName = "সামনের গেট",
                ipAddress = "$detectedSubnetPrefix.108",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "L28A9F4C",
                lensCount = 3,
                lens1Channel = 1,
                lens2Channel = 2,
                lens3Channel = 3,
                lens1Name = "লেন্স ১ • বাম প্যানোরামা (Fixed)",
                lens2Name = "লেন্স ২ • ডান প্যানোরামা (Fixed)",
                lens3Name = "লেন্স ৩ • 360° PTZ ট্র্যাকিং (Motor)",
                ptzControlChannel = 3,
                preferredSubtype = 0,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = true,
                supportsSiren = true,
                nightVisionMode = "SMART",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = true,
                click2TrackEnabled = true,
                panAngle = -15f,
                tiltAngle = 10f,
                zoomLevel = 1.0f,
                isFavorite = true
            )
        )

        // 2. 2-Lens Dual Camera (Cruiser Dual 2 - Fixed Wide + PTZ Dome)
        val c2Id = dao.insertCamera(
            ImouCameraEntity(
                name = "গ্যারেজ ও ড্রাইভওয়ে",
                modelName = "Imou Cruiser Dual 2 (10MP)",
                roomName = "আউটডোর",
                ipAddress = "$detectedSubnetPrefix.109",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "K94D2E7B",
                lensCount = 2,
                lens1Channel = 1,
                lens2Channel = 2,
                lens3Channel = 3,
                lens1Name = "লেন্স ১ • ফিক্সড ওয়াইড ওভারভিউ",
                lens2Name = "লেন্স ২ • 360° PTZ ডোম লেন্স",
                lens3Name = "লেন্স ৩",
                ptzControlChannel = 2,
                preferredSubtype = 0,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = true,
                supportsSiren = true,
                nightVisionMode = "FULL_COLOR",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = true,
                click2TrackEnabled = true,
                panAngle = 22f,
                tiltAngle = 8f,
                zoomLevel = 1.2f,
                isFavorite = true
            )
        )

        // 3. 2-Lens Indoor Dual Camera (Ranger Dual 6MP)
        val c3Id = dao.insertCamera(
            ImouCameraEntity(
                name = "লিভিং ও ডাইনিং রুম",
                modelName = "Imou Ranger Dual (6MP)",
                roomName = "রুম",
                ipAddress = "$detectedSubnetPrefix.110",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "M71C5X8A",
                lensCount = 2,
                lens1Channel = 1,
                lens2Channel = 2,
                lens3Channel = 3,
                lens1Name = "লেন্স ১ • ডাইনিং ফিক্সড ভিউ",
                lens2Name = "লেন্স ২ • লিভিং রুম 360° PTZ",
                lens3Name = "লেন্স ৩",
                ptzControlChannel = 2,
                preferredSubtype = 0,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = false,
                supportsSiren = true,
                nightVisionMode = "IR_BW",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = true,
                click2TrackEnabled = true,
                panAngle = 0f,
                tiltAngle = 5f,
                zoomLevel = 1.0f,
                isFavorite = true
            )
        )

        // 4. 1-Lens PTZ Camera (Rex 3D 5MP)
        val c4Id = dao.insertCamera(
            ImouCameraEntity(
                name = "ছাদ ও সিঁড়ি ঘর",
                modelName = "Imou Rex 3D (5MP PTZ)",
                roomName = "ছাদ",
                ipAddress = "$detectedSubnetPrefix.111",
                rtspPort = 554,
                httpPort = 80,
                onvifPort = 80,
                username = "admin",
                safetyCode = "P63V8N2Q",
                lensCount = 1,
                lens1Channel = 1,
                lens2Channel = 2,
                lens3Channel = 3,
                lens1Name = "লেন্স ১ • 360° PTZ মেইন লেন্স",
                lens2Name = "লেন্স ২",
                lens3Name = "লেন্স ৩",
                ptzControlChannel = 1,
                preferredSubtype = 0,
                supportsPtz = true,
                supportsAudio = true,
                supportsSpotlight = true,
                supportsSiren = true,
                nightVisionMode = "SMART",
                motionAlertEnabled = true,
                humanDetectEnabled = true,
                smartTrackingEnabled = true,
                click2TrackEnabled = false,
                panAngle = -30f,
                tiltAngle = 15f,
                zoomLevel = 1.0f,
                isFavorite = false
            )
        )

        // Seed Favorite Collection Points (Presets) like Imou Life
        listOf(
            PtzPresetEntity(cameraId = c1Id, presetNumber = 1, title = "পকেট গেট (Main Entry)", panAngle = -42f, tiltAngle = 12f, zoomLevel = 1.4f),
            PtzPresetEntity(cameraId = c1Id, presetNumber = 2, title = "সামনের রাস্তা (Roadside)", panAngle = 18f, tiltAngle = 6f, zoomLevel = 2.0f),
            PtzPresetEntity(cameraId = c1Id, presetNumber = 3, title = "বাউন্ডারি ওয়াল (Corner)", panAngle = 72f, tiltAngle = 15f, zoomLevel = 1.6f),
            PtzPresetEntity(cameraId = c2Id, presetNumber = 1, title = "গাড়ি পার্কিং (Car Porch)", panAngle = -15f, tiltAngle = 14f, zoomLevel = 1.2f),
            PtzPresetEntity(cameraId = c2Id, presetNumber = 2, title = "সিঁড়ি এন্ট্রি (Stairway)", panAngle = 48f, tiltAngle = 10f, zoomLevel = 1.5f),
            PtzPresetEntity(cameraId = c3Id, presetNumber = 1, title = "সোফা ও টিভি জোন", panAngle = 0f, tiltAngle = 5f, zoomLevel = 1.0f),
            PtzPresetEntity(cameraId = c3Id, presetNumber = 2, title = "বারান্দার দরজা", panAngle = -65f, tiltAngle = 10f, zoomLevel = 1.4f),
            PtzPresetEntity(cameraId = c4Id, presetNumber = 1, title = "চিলেকোঠা দরজা", panAngle = -20f, tiltAngle = 12f, zoomLevel = 1.0f)
        ).forEach { dao.insertPreset(it) }

        dao.insertVaultEvent(
            VaultEventEntity(
                cameraId = c1Id,
                cameraName = "মেইন গেট ও বাউন্ডারি",
                cameraIp = "$detectedSubnetPrefix.108",
                eventType = "CLICK2TRACK",
                title = "মাল্টি-লেন্স (3-Lens / 2-Lens) LAN সিস্টেম প্রস্তুত",
                details = "চ্যানেল ১, ২ ও ৩ একসাথে প্লেব্যাক এবং ফিক্সড লেন্সে ট্যাপ করে PTZ লেন্স ঘোরানোর Click2Track ফিচার সক্রিয় করা হয়েছে।",
                resolution = "3-Lens Simultaneous RTSP"
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
