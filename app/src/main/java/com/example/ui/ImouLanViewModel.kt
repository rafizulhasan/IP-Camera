package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ImouCameraEntity
import com.example.data.local.ImouCameraRepository
import com.example.data.local.ImouLanDatabase
import com.example.data.local.PtzPresetEntity
import com.example.data.local.VaultEventEntity
import com.example.network.DiscoveredLanDevice
import com.example.network.ImouLanController
import com.example.network.LocalNetworkInfo
import com.example.network.PtzDirection
import com.example.network.RtspProbeResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MatrixLayoutMode {
    SINGLE_FOCUS,
    QUAD_2X2
}

enum class MainNavTab {
    IMOU_HOME,
    MULTI_LENS_LIVE,
    MESSAGES_ALBUM,
    LAN_SETTINGS
}

data class ImouHudUiState(
    val activeTab: MainNavTab = MainNavTab.IMOU_HOME,
    val layoutMode: MatrixLayoutMode = MatrixLayoutMode.QUAD_2X2,
    val selectedCameraId: Long = 0L,
    val ptzSpeed: Int = 5,
    val recordingCameraIds: Set<Long> = emptySet(),
    val mutedCameraIds: Set<Long> = emptySet(),
    val activeTalkCameraIds: Set<Long> = emptySet(),
    val networkInfo: LocalNetworkInfo = LocalNetworkInfo("192.168.1.50", "192.168.1", "wlan0", true),
    val probeResults: Map<Long, RtspProbeResult> = emptyMap(),
    val isScanningSubnet: Boolean = false,
    val scanProgressPercent: Int = 0,
    val currentScanIp: String = "",
    val discoveredDevices: List<DiscoveredLanDevice> = emptyList(),
    val lastCommandFeedback: String = "Multi-Lens RTSP + Click2Track LAN সচল",
    val snackbarMessage: String? = null
)

class ImouLanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ImouLanDatabase.getInstance(application)
    private val repository = ImouCameraRepository(database.cameraDao())
    private val lanController = ImouLanController()

    val cameras: StateFlow<List<ImouCameraEntity>> = repository.allCameras
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<PtzPresetEntity>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultEvents: StateFlow<List<VaultEventEntity>> = repository.allVaultEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hudState = MutableStateFlow(ImouHudUiState())
    val hudState: StateFlow<ImouHudUiState> = _hudState.asStateFlow()

    init {
        val detectedNet = lanController.detectLocalNetwork()
        _hudState.update { it.copy(networkInfo = detectedNet) }

        viewModelScope.launch {
            repository.ensureDefaultCamerasSeeded(detectedNet.subnetPrefix)
            val initialList = repository.allCameras.first()
            if (initialList.isNotEmpty() && _hudState.value.selectedCameraId == 0L) {
                _hudState.update {
                    it.copy(selectedCameraId = initialList.first().id)
                }
            }
            probeAllCamerasOnLan()
        }
    }

    fun selectTab(tab: MainNavTab) {
        _hudState.update { it.copy(activeTab = tab) }
    }

    fun toggleMatrixLayoutMode() {
        _hudState.update { state ->
            val next = if (state.layoutMode == MatrixLayoutMode.QUAD_2X2) {
                MatrixLayoutMode.SINGLE_FOCUS
            } else {
                MatrixLayoutMode.QUAD_2X2
            }
            state.copy(layoutMode = next)
        }
    }

    fun openCameraLiveView(cameraId: Long) {
        _hudState.update {
            it.copy(
                selectedCameraId = cameraId,
                activeTab = MainNavTab.MULTI_LENS_LIVE
            )
        }
    }

    fun selectCamera(cameraId: Long) {
        _hudState.update { it.copy(selectedCameraId = cameraId) }
    }

    fun setPtzSpeed(speed: Int) {
        _hudState.update { it.copy(ptzSpeed = speed.coerceIn(1, 8)) }
    }

    fun clearSnackbar() {
        _hudState.update { it.copy(snackbarMessage = null) }
    }

    fun probeAllCamerasOnLan() {
        viewModelScope.launch {
            val net = lanController.detectLocalNetwork()
            _hudState.update { it.copy(networkInfo = net) }
            cameras.value.forEach { cam ->
                launch {
                    val result = lanController.verifyCameraRtspHandshake(cam)
                    _hudState.update { st ->
                        st.copy(probeResults = st.probeResults + (cam.id to result))
                    }
                }
            }
        }
    }

    fun probeSingleCamera(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val result = lanController.verifyCameraRtspHandshake(camera)
            _hudState.update { st ->
                st.copy(
                    probeResults = st.probeResults + (camera.id to result),
                    snackbarMessage = "${camera.name}: ${result.rtspStatusLine} (${result.latencyMs}ms)"
                )
            }
        }
    }

    fun startSubnetDiscovery(subnetPrefix: String) {
        if (_hudState.value.isScanningSubnet) return
        val cleanPrefix = subnetPrefix.trim().removeSuffix(".")
        viewModelScope.launch {
            _hudState.update {
                it.copy(
                    isScanningSubnet = true,
                    scanProgressPercent = 2,
                    currentScanIp = "$cleanPrefix.1",
                    discoveredDevices = emptyList()
                )
            }

            val found = lanController.scanLocalSubnet(cleanPrefix) { progress, currentIp ->
                _hudState.update { st ->
                    st.copy(
                        scanProgressPercent = progress.coerceIn(1, 100),
                        currentScanIp = currentIp
                    )
                }
            }

            _hudState.update {
                it.copy(
                    isScanningSubnet = false,
                    scanProgressPercent = 100,
                    discoveredDevices = found,
                    snackbarMessage = if (found.isNotEmpty()) {
                        "LAN স্ক্যান সম্পন্ন: ${found.size}টি Imou/ONVIF ডিভাইস পাওয়া গেছে!"
                    } else {
                        "সাবনেট $cleanPrefix.x স্ক্যান সম্পন্ন"
                    }
                )
            }
        }
    }

    fun syncAllCamerasToSubnet(newSubnetPrefix: String) {
        val parts = newSubnetPrefix.trim().removeSuffix(".").split(".")
        if (parts.size != 3) {
            _hudState.update { it.copy(snackbarMessage = "সঠিক সাবনেট দিন, যেমন: 192.168.0 বা 192.168.1") }
            return
        }
        val cleanPrefix = "${parts[0]}.${parts[1]}.${parts[2]}"
        viewModelScope.launch {
            cameras.value.forEachIndexed { idx, cam ->
                val lastOctet = cam.ipAddress.substringAfterLast(".").toIntOrNull() ?: (108 + idx)
                repository.updateCamera(cam.copy(ipAddress = "$cleanPrefix.$lastOctet"))
            }
            _hudState.update {
                it.copy(snackbarMessage = "সব ক্যামেরা $cleanPrefix.x সাবনেটে সিঙ্ক করা হয়েছে")
            }
            probeAllCamerasOnLan()
        }
    }

    fun saveOrUpdateCamera(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val savedId = repository.saveCamera(camera)
            val targetId = if (camera.id == 0L) savedId else camera.id
            _hudState.update {
                it.copy(
                    selectedCameraId = targetId,
                    snackbarMessage = "'${camera.name}' (${camera.lensCount}-Lens) সংরক্ষিত হয়েছে"
                )
            }
            probeSingleCamera(camera.copy(id = targetId))
        }
    }

    fun deleteCamera(camera: ImouCameraEntity) {
        viewModelScope.launch {
            repository.deleteCamera(camera)
            val remaining = repository.allCameras.first()
            _hudState.update {
                it.copy(
                    selectedCameraId = remaining.firstOrNull()?.id ?: 0L,
                    snackbarMessage = "'${camera.name}' মুছে ফেলা হয়েছে"
                )
            }
        }
    }

    fun performClick2Track(camera: ImouCameraEntity, sourceLensIndex: Int, normX: Float, normY: Float) {
        val mappedPan = ((normX - 0.5f) * 240f + if (camera.lensCount == 3 && sourceLensIndex == 1) -40f else if (camera.lensCount == 3 && sourceLensIndex == 2) 40f else 0f)
            .coerceIn(-175f, 175f)
        val mappedTilt = ((0.5f - normY) * 90f).coerceIn(-25f, 80f)

        viewModelScope.launch {
            val updated = camera.copy(
                linkageTargetX = normX,
                linkageTargetY = normY,
                panAngle = mappedPan,
                tiltAngle = mappedTilt
            )
            repository.updateCamera(updated)

            val res = lanController.sendClick2TrackLinkageCommand(updated, sourceLensIndex, normX, normY)
            repository.logEvent(
                VaultEventEntity(
                    cameraId = camera.id,
                    cameraName = camera.name,
                    cameraIp = camera.ipAddress,
                    eventType = "CLICK2TRACK",
                    title = "Click2Track: লেন্স $sourceLensIndex -> PTZ লেন্স ট্র্যাকিং",
                    details = "PTZ লেন্স স্বয়ংক্রিয়ভাবে PAN ${mappedPan.toInt()}° • TILT ${mappedTilt.toInt()}°-এ ফোকাস করেছে (${res.summaryMessage})",
                    resolution = "${camera.lensCount}-Lens Linkage"
                )
            )
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Click2Track (L$sourceLensIndex -> PTZ CH${camera.ptzControlChannel}): PAN ${mappedPan.toInt()}° TILT ${mappedTilt.toInt()}°",
                    snackbarMessage = "Click2Track: ফিক্সড লেন্স $sourceLensIndex-এর পজিশনে PTZ লেন্স ফোকাস করেছে"
                )
            }
        }
    }

    fun movePtz(camera: ImouCameraEntity, direction: PtzDirection) {
        val speed = _hudState.value.ptzSpeed
        val scale = speed / 5f
        val newPan = (camera.panAngle + direction.panDelta * scale).coerceIn(-180f, 180f)
        val newTilt = (camera.tiltAngle + direction.tiltDelta * scale).coerceIn(-30f, 90f)
        val newZoom = (camera.zoomLevel + direction.zoomDelta).coerceIn(1.0f, 8.0f)

        val nextLinkX = (0.5f + (newPan / 240f)).coerceIn(0.1f, 0.9f)
        val nextLinkY = (0.5f - (newTilt / 100f)).coerceIn(0.15f, 0.85f)

        viewModelScope.launch {
            val updated = camera.copy(
                panAngle = newPan,
                tiltAngle = newTilt,
                zoomLevel = newZoom,
                linkageTargetX = nextLinkX,
                linkageTargetY = nextLinkY
            )
            repository.updateCamera(updated)

            val res = lanController.sendPtzMoveCommand(updated, direction, speed)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "${direction.labelBn} (CH${camera.ptzControlChannel}) • ${res.summaryMessage}"
                )
            }
        }
    }

    fun centerCameraPtz(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val updated = camera.copy(
                panAngle = 0f,
                tiltAngle = 0f,
                zoomLevel = 1.0f,
                linkageTargetX = 0.5f,
                linkageTargetY = 0.5f
            )
            repository.updateCamera(updated)
            val res = lanController.sendPtzPresetCommand(updated, presetNumber = 1, setPreset = false)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "PTZ সেন্টার রিসেট (0°, 0°) • ${res.summaryMessage}",
                    snackbarMessage = "${camera.name} PTZ লেন্স সেন্টারে রিসেট হয়েছে"
                )
            }
        }
    }

    fun setCameraZoom(camera: ImouCameraEntity, zoomLevel: Float) {
        viewModelScope.launch {
            val clamped = zoomLevel.coerceIn(1.0f, 8.0f)
            val direction = if (clamped >= camera.zoomLevel) PtzDirection.ZOOM_IN else PtzDirection.ZOOM_OUT
            val updated = camera.copy(zoomLevel = clamped)
            repository.updateCamera(updated)
            val res = lanController.sendPtzMoveCommand(updated, direction, _hudState.value.ptzSpeed)
            _hudState.update {
                it.copy(lastCommandFeedback = "PTZ লেন্স জুম ${"%.1f".format(clamped)}x • ${res.summaryMessage}")
            }
        }
    }

    fun applyPtzPreset(camera: ImouCameraEntity, preset: PtzPresetEntity) {
        viewModelScope.launch {
            val nextLinkX = (0.5f + (preset.panAngle / 240f)).coerceIn(0.1f, 0.9f)
            val nextLinkY = (0.5f - (preset.tiltAngle / 100f)).coerceIn(0.15f, 0.85f)
            val updated = camera.copy(
                panAngle = preset.panAngle,
                tiltAngle = preset.tiltAngle,
                zoomLevel = preset.zoomLevel,
                linkageTargetX = nextLinkX,
                linkageTargetY = nextLinkY
            )
            repository.updateCamera(updated)
            val res = lanController.sendPtzPresetCommand(updated, preset.presetNumber, setPreset = false)
            repository.logEvent(
                VaultEventEntity(
                    cameraId = camera.id,
                    cameraName = camera.name,
                    cameraIp = camera.ipAddress,
                    eventType = "PTZ_PRESET",
                    title = "ফেভারিট পয়েন্ট: ${preset.title}",
                    details = "PAN ${preset.panAngle.toInt()}° • TILT ${preset.tiltAngle.toInt()}° • ZOOM ${preset.zoomLevel}x (${res.summaryMessage})"
                )
            )
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "GotoPreset #${preset.presetNumber} '${preset.title}'",
                    snackbarMessage = "PTZ লেন্স '${preset.title}' পয়েন্টে ঘোরানো হয়েছে"
                )
            }
        }
    }

    fun saveCurrentPositionAsPreset(camera: ImouCameraEntity, title: String, presetNumber: Int) {
        viewModelScope.launch {
            repository.savePreset(
                PtzPresetEntity(
                    cameraId = camera.id,
                    presetNumber = presetNumber,
                    title = title,
                    panAngle = camera.panAngle,
                    tiltAngle = camera.tiltAngle,
                    zoomLevel = camera.zoomLevel
                )
            )
            lanController.sendPtzPresetCommand(camera, presetNumber, setPreset = true)
            _hudState.update {
                it.copy(snackbarMessage = "কালেকশন পয়েন্ট '$title' সেভ করা হয়েছে")
            }
        }
    }

    fun deletePreset(presetId: Long) {
        viewModelScope.launch {
            repository.deletePreset(presetId)
        }
    }

    fun toggleStreamQuality(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val nextSub = if (camera.preferredSubtype == 0) 1 else 0
            repository.updateCamera(camera.copy(preferredSubtype = nextSub))
            val label = if (nextSub == 0) "2K/HD মেইন স্ট্রিম (subtype=0)" else "SD ফাস্ট সাব স্ট্রিম (subtype=1)"
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "সব ${camera.lensCount}টি লেন্সে $label সক্রিয়",
                    snackbarMessage = "${camera.name}: সব লেন্সে $label চালু হয়েছে"
                )
            }
        }
    }

    fun toggleAudioMute(cameraId: Long) {
        _hudState.update { state ->
            val next = if (state.mutedCameraIds.contains(cameraId)) state.mutedCameraIds - cameraId else state.mutedCameraIds + cameraId
            state.copy(mutedCameraIds = next)
        }
    }

    fun toggleTwoWayTalk(cameraId: Long) {
        _hudState.update { state ->
            val isNowActive = !state.activeTalkCameraIds.contains(cameraId)
            val next = if (isNowActive) state.activeTalkCameraIds + cameraId else state.activeTalkCameraIds - cameraId
            state.copy(
                activeTalkCameraIds = next,
                snackbarMessage = if (isNowActive) "টু-ওয়ে মাইক টক (LAN Audio) চালু হয়েছে" else "টু-ওয়ে মাইক টক বন্ধ করা হয়েছে"
            )
        }
    }

    fun captureSnapshotToVault(camera: ImouCameraEntity) {
        viewModelScope.launch {
            repository.logEvent(
                VaultEventEntity(
                    cameraId = camera.id,
                    cameraName = camera.name,
                    cameraIp = camera.ipAddress,
                    eventType = "SNAPSHOT",
                    title = "${camera.lensCount}-Lens একসাথে স্ন্যাপশট ক্যাপচার",
                    details = "CGI: ${camera.buildSnapshotCgiUrl()} • PAN ${camera.panAngle.toInt()}° TILT ${camera.tiltAngle.toInt()}°",
                    resolution = "${camera.lensCount}x 2K HEVC"
                )
            )
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Multi-Lens Snapshot saved -> Album",
                    snackbarMessage = "${camera.name}-এর সব লেন্সের স্ন্যাপশট অ্যালবামে সেভ হয়েছে"
                )
            }
        }
    }

    fun toggleRecording(camera: ImouCameraEntity) {
        val currentlyRecording = _hudState.value.recordingCameraIds.contains(camera.id)
        _hudState.update { st ->
            val next = if (currentlyRecording) st.recordingCameraIds - camera.id else st.recordingCameraIds + camera.id
            st.copy(
                recordingCameraIds = next,
                snackbarMessage = if (!currentlyRecording) {
                    "${camera.lensCount}টি লেন্সের একসাথে লোকাল রেকর্ডিং শুরু হয়েছে"
                } else {
                    "রেকর্ডিং সম্পন্ন ও লোকাল অ্যালবামে সেভ হয়েছে"
                }
            )
        }

        if (currentlyRecording) {
            viewModelScope.launch {
                repository.logEvent(
                    VaultEventEntity(
                        cameraId = camera.id,
                        cameraName = camera.name,
                        cameraIp = camera.ipAddress,
                        eventType = "RECORDING",
                        title = "${camera.lensCount}-Lens RTSP ভিডিও ক্লিপ সংরক্ষিত",
                        details = "Channels: 1..${camera.lensCount} • Direct LAN RTSP Stream Dump",
                        resolution = "${camera.lensCount}-Ch MP4"
                    )
                )
            }
        }
    }

    fun updateNightVisionMode(camera: ImouCameraEntity, mode: String) {
        viewModelScope.launch {
            val updated = camera.copy(nightVisionMode = mode)
            repository.updateCamera(updated)
            val res = lanController.setNightVisionMode(updated, mode)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "NightVision [$mode] -> ${res.summaryMessage}",
                    snackbarMessage = "${camera.name}: নাইট ভিশন '$mode' মোডে সেট হয়েছে"
                )
            }
        }
    }

    fun toggleSpotlight(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.spotlightActive
            val updated = camera.copy(spotlightActive = next)
            repository.updateCamera(updated)
            lanController.toggleWhiteLightSpotlight(updated, next)
            _hudState.update {
                it.copy(
                    snackbarMessage = "${camera.name}: স্পটলাইট ${if (next) "চালু" else "বন্ধ"} করা হয়েছে"
                )
            }
        }
    }

    fun toggleSiren(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.sirenActive
            val updated = camera.copy(sirenActive = next)
            repository.updateCamera(updated)
            val res = lanController.triggerActiveDeterrenceSiren(updated, next)
            if (next) {
                repository.logEvent(
                    VaultEventEntity(
                        cameraId = camera.id,
                        cameraName = camera.name,
                        cameraIp = camera.ipAddress,
                        eventType = "SIREN_TRIGGER",
                        title = "110dB অ্যাক্টিভ সাইরেন ট্রিগার",
                        details = "Active Deterrence Siren ON (${camera.ipAddress}) • ${res.summaryMessage}",
                        resolution = "110dB ALARM"
                    )
                )
            }
            _hudState.update {
                it.copy(
                    snackbarMessage = "${camera.name}: 110dB সাইরেন ${if (next) "বাজছে!" else "বন্ধ করা হয়েছে"}"
                )
            }
        }
    }

    fun toggleAllCamerasSiren() {
        viewModelScope.launch {
            val anyActive = cameras.value.any { it.sirenActive }
            val targetState = !anyActive
            cameras.value.forEach { cam ->
                val updated = cam.copy(sirenActive = targetState)
                repository.updateCamera(updated)
                lanController.triggerActiveDeterrenceSiren(updated, targetState)
            }
            _hudState.update {
                it.copy(
                    snackbarMessage = if (targetState) "সব ক্যামেরায় জরুরি সাইরেন অ্যালার্ম চালু হয়েছে!" else "সব ক্যামেরার সাইরেন বন্ধ করা হয়েছে"
                )
            }
        }
    }

    fun toggleHumanDetect(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.humanDetectEnabled
            repository.updateCamera(camera.copy(humanDetectEnabled = next))
            _hudState.update {
                it.copy(snackbarMessage = "হিউম্যান ডিটেকশন ${if (next) "চালু" else "বন্ধ"} হয়েছে")
            }
        }
    }

    fun toggleSmartTracking(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.smartTrackingEnabled
            repository.updateCamera(camera.copy(smartTrackingEnabled = next))
            _hudState.update {
                it.copy(snackbarMessage = "স্মার্ট ট্র্যাকিং ${if (next) "চালু" else "বন্ধ"} হয়েছে")
            }
        }
    }

    fun toggleClick2Track(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.click2TrackEnabled
            repository.updateCamera(camera.copy(click2TrackEnabled = next))
            _hudState.update {
                it.copy(snackbarMessage = "Click2Track মাল্টি-লেন্স লিংকেজ ${if (next) "চালু" else "বন্ধ"} হয়েছে")
            }
        }
    }

    fun toggleAutoCruise(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.autoCruiseEnabled
            repository.updateCamera(camera.copy(autoCruiseEnabled = next))
            _hudState.update {
                it.copy(snackbarMessage = "৩৬০° অটো ক্রুজ ${if (next) "চালু" else "বন্ধ"} হয়েছে")
            }
        }
    }

    fun togglePrivacyMask(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.privacyMaskEnabled
            repository.updateCamera(camera.copy(privacyMaskEnabled = next))
            _hudState.update {
                it.copy(snackbarMessage = "প্রাইভেসি মোড (Lens Hide) ${if (next) "চালু" else "বন্ধ"} হয়েছে")
            }
        }
    }

    fun toggleFlip180(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.imageFlip180
            val updated = camera.copy(imageFlip180 = next)
            repository.updateCamera(updated)
            lanController.toggleImageFlip180(updated, next)
            _hudState.update {
                it.copy(snackbarMessage = "১৮০° ইমেজ রোটেশন ${if (next) "চালু" else "বন্ধ"} হয়েছে")
            }
        }
    }

    fun rebootCameraOverLan(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val res = lanController.rebootCameraOnLan(camera)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Reboot -> /cgi-bin/magicBox.cgi?action=reboot (${res.summaryMessage})",
                    snackbarMessage = "${camera.name}-এ LAN রিস্টার্ট সিগন্যাল পাঠানো হয়েছে"
                )
            }
        }
    }

    fun deleteVaultEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }

    fun clearAllVaultEvents() {
        viewModelScope.launch {
            repository.clearEvents()
            _hudState.update { it.copy(snackbarMessage = "মেসেজ ও অ্যালবাম লগ পরিষ্কার করা হয়েছে") }
        }
    }
}
