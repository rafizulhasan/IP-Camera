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
    LIVE_MATRIX,
    PTZ_CONSOLE,
    LAN_SCANNER,
    LOCAL_VAULT,
    OFFLINE_GUIDE
}

data class ImouHudUiState(
    val activeTab: MainNavTab = MainNavTab.LIVE_MATRIX,
    val layoutMode: MatrixLayoutMode = MatrixLayoutMode.QUAD_2X2,
    val selectedCameraId: Long = 0L,
    val ptzSpeed: Int = 5,
    val recordingCameraIds: Set<Long> = emptySet(),
    val mutedCameraIds: Set<Long> = emptySet(),
    val networkInfo: LocalNetworkInfo = LocalNetworkInfo("192.168.1.50", "192.168.1", "wlan0", true),
    val probeResults: Map<Long, RtspProbeResult> = emptyMap(),
    val isScanningSubnet: Boolean = false,
    val scanProgressPercent: Int = 0,
    val currentScanIp: String = "",
    val discoveredDevices: List<DiscoveredLanDevice> = emptyList(),
    val lastCommandFeedback: String = "Dahua CGI / ONVIF • লোকাল LAN কন্ট্রোল প্রস্তুত",
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
                    it.copy(
                        selectedCameraId = initialList.first().id,
                        mutedCameraIds = initialList.drop(1).map { c -> c.id }.toSet()
                    )
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

    fun setMatrixLayoutMode(mode: MatrixLayoutMode) {
        _hudState.update { it.copy(layoutMode = mode) }
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
            val currentCameras = cameras.value
            currentCameras.forEach { cam ->
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
                        "LAN স্ক্যান সম্পন্ন: ${found.size}টি IP ক্যামেরা/ডিভাইস পাওয়া গেছে!"
                    } else {
                        "সাবনেট $cleanPrefix.x স্ক্যান সম্পন্ন (পোর্ট 554/80/37777 চেক করা হয়েছে)"
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
                val updated = cam.copy(ipAddress = "$cleanPrefix.$lastOctet")
                repository.updateCamera(updated)
            }
            _hudState.update {
                it.copy(snackbarMessage = "সব ক্যামেরা $cleanPrefix.x সাবনেটে আপডেট করা হয়েছে")
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
                    snackbarMessage = "'${camera.name}' (${camera.ipAddress}) সংরক্ষিত হয়েছে"
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

    fun movePtz(camera: ImouCameraEntity, direction: PtzDirection) {
        val speed = _hudState.value.ptzSpeed
        val scale = speed / 5f
        val newPan = (camera.panAngle + direction.panDelta * scale).coerceIn(-180f, 180f)
        val newTilt = (camera.tiltAngle + direction.tiltDelta * scale).coerceIn(-30f, 90f)
        val newZoom = (camera.zoomLevel + direction.zoomDelta).coerceIn(1.0f, 8.0f)

        viewModelScope.launch {
            val updated = camera.copy(panAngle = newPan, tiltAngle = newTilt, zoomLevel = newZoom)
            repository.updateCamera(updated)

            val res = lanController.sendPtzMoveCommand(updated, direction, speed)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "${direction.labelBn} -> ${res.endpoint} (${res.summaryMessage})"
                )
            }
        }
    }

    fun centerCameraPtz(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val updated = camera.copy(panAngle = 0f, tiltAngle = 0f, zoomLevel = 1.0f)
            repository.updateCamera(updated)
            val res = lanController.sendPtzPresetCommand(updated, presetNumber = 1, setPreset = false)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "PTZ হোম পজিশন (0°, 0°) • ${res.summaryMessage}",
                    snackbarMessage = "${camera.name} সেন্টার পজিশনে রিসেট হয়েছে"
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
                it.copy(lastCommandFeedback = "অপটিক্যাল জুম ${"%.1f".format(clamped)}x • ${res.summaryMessage}")
            }
        }
    }

    fun applyPtzPreset(camera: ImouCameraEntity, preset: PtzPresetEntity) {
        viewModelScope.launch {
            val updated = camera.copy(
                panAngle = preset.panAngle,
                tiltAngle = preset.tiltAngle,
                zoomLevel = preset.zoomLevel
            )
            repository.updateCamera(updated)
            val res = lanController.sendPtzPresetCommand(updated, preset.presetNumber, setPreset = false)
            repository.logEvent(
                VaultEventEntity(
                    cameraId = camera.id,
                    cameraName = camera.name,
                    cameraIp = camera.ipAddress,
                    eventType = "PTZ_PRESET",
                    title = "PTZ প্রিসেট #${preset.presetNumber} (${preset.title})",
                    details = "PAN ${preset.panAngle.toInt()}° • TILT ${preset.tiltAngle.toInt()}° • ZOOM ${preset.zoomLevel}x (${res.summaryMessage})"
                )
            )
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "GotoPreset #${preset.presetNumber} '${preset.title}' • ${res.summaryMessage}",
                    snackbarMessage = "ক্যামেরা '${preset.title}' অ্যাঙ্গেলে ঘোরানো হয়েছে"
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
            val res = lanController.sendPtzPresetCommand(camera, presetNumber, setPreset = true)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "SetPreset #$presetNumber ($title) • ${res.summaryMessage}",
                    snackbarMessage = "প্রিসেট '$title' মেমোরিতে সংরক্ষিত হয়েছে"
                )
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
            val updated = camera.copy(preferredSubtype = nextSub)
            repository.updateCamera(updated)
            val label = if (nextSub == 0) "Main Stream (2K HD • subtype=0)" else "Sub Stream (SD Fast • subtype=1)"
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "RTSP স্ট্রিম সুইচ: $label",
                    snackbarMessage = "${camera.name}: $label সক্রিয়"
                )
            }
        }
    }

    fun toggleAudioMute(cameraId: Long) {
        _hudState.update { state ->
            val nextSet = if (state.mutedCameraIds.contains(cameraId)) {
                state.mutedCameraIds - cameraId
            } else {
                state.mutedCameraIds + cameraId
            }
            state.copy(mutedCameraIds = nextSet)
        }
    }

    fun captureSnapshotToVault(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val resLabel = if (camera.preferredSubtype == 0) "2560x1440 (2K HEVC)" else "640x480 (SD H.264)"
            repository.logEvent(
                VaultEventEntity(
                    cameraId = camera.id,
                    cameraName = camera.name,
                    cameraIp = camera.ipAddress,
                    eventType = "SNAPSHOT",
                    title = "হাই-রেজোলিউশন LAN স্ন্যাপশট ক্যাপচার",
                    details = "Endpoint: ${camera.buildSnapshotCgiUrl()} • Angle: P:${camera.panAngle.toInt()}° T:${camera.tiltAngle.toInt()}° • NV: ${camera.nightVisionMode}",
                    resolution = resLabel
                )
            )
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Snapshot captured -> /cgi-bin/snapshot.cgi?channel=${camera.channel}",
                    snackbarMessage = "স্ন্যাপশট ভল্টে সংরক্ষিত হয়েছে (${camera.name})"
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
                    "লোকাল RTSP স্ট্রিম রেকর্ডিং শুরু হয়েছে (${camera.name})"
                } else {
                    "রেকর্ডিং সম্পন্ন ও লোকাল ভল্টে সংরক্ষিত (${camera.name})"
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
                        title = "RTSP ডিরেক্ট স্ট্রিম ক্লিপ সংরক্ষিত",
                        details = "RTSP Source: ${camera.buildMaskedRtspUrl()} • Direct TCP Packet Dump",
                        resolution = if (camera.preferredSubtype == 0) "2K H.265 MP4" else "SD H.264 MP4"
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
                    lastCommandFeedback = "NightVision [$mode] -> ${res.endpoint} (${res.summaryMessage})"
                )
            }
        }
    }

    fun toggleSpotlight(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.spotlightActive
            val updated = camera.copy(spotlightActive = next)
            repository.updateCamera(updated)
            val res = lanController.toggleWhiteLightSpotlight(updated, next)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Spotlight ${if (next) "ON" else "OFF"} -> ${res.endpoint}",
                    snackbarMessage = "${camera.name}: ফ্লাডলাইট ${if (next) "চালু" else "বন্ধ"} করা হয়েছে"
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
                        title = "110dB অ্যাক্টিভ সাইরেন অ্যালার্ম ট্রিগার",
                        details = "Dahua CoaxialIO Siren Activated on ${camera.ipAddress} (${res.summaryMessage})",
                        resolution = "ALARM IO=1"
                    )
                )
            }
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "110dB Siren ${if (next) "ACTIVE" else "STOPPED"} -> ${res.endpoint}",
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
                    snackbarMessage = if (targetState) "সব ক্যামেরায় জরুরি পেরিমিটার সাইরেন চালু হয়েছে!" else "সব ক্যামেরার সাইরেন বন্ধ করা হয়েছে"
                )
            }
        }
    }

    fun togglePrivacyMask(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.privacyMaskEnabled
            repository.updateCamera(camera.copy(privacyMaskEnabled = next))
            _hudState.update {
                it.copy(
                    snackbarMessage = "${camera.name}: প্রাইভেসি শিল্ড ${if (next) "সক্রিয়" else "নিষ্ক্রিয়"}"
                )
            }
        }
    }

    fun toggleFlip180(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.imageFlip180
            val updated = camera.copy(imageFlip180 = next)
            repository.updateCamera(updated)
            val res = lanController.toggleImageFlip180(updated, next)
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Image Flip 180° = $next (${res.summaryMessage})",
                    snackbarMessage = "${camera.name}: ১৮০° ইমেজ রোটেশন ${if (next) "চালু" else "বন্ধ"}"
                )
            }
        }
    }

    fun toggleSmartTracking(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val next = !camera.smartTrackingEnabled
            repository.updateCamera(camera.copy(smartTrackingEnabled = next))
            _hudState.update {
                it.copy(
                    snackbarMessage = "${camera.name}: স্মার্ট হিউম্যান ট্র্যাকিং ${if (next) "চালু" else "বন্ধ"}"
                )
            }
        }
    }

    fun rebootCameraOverLan(camera: ImouCameraEntity) {
        viewModelScope.launch {
            val res = lanController.rebootCameraOnLan(camera)
            repository.logEvent(
                VaultEventEntity(
                    cameraId = camera.id,
                    cameraName = camera.name,
                    cameraIp = camera.ipAddress,
                    eventType = "CGI_COMMAND",
                    title = "LAN রিমোট রিবুট কমান্ড পাঠানো হয়েছে",
                    details = "GET /cgi-bin/magicBox.cgi?action=reboot (${res.summaryMessage})"
                )
            )
            _hudState.update {
                it.copy(
                    lastCommandFeedback = "Reboot -> /cgi-bin/magicBox.cgi?action=reboot (${res.summaryMessage})",
                    snackbarMessage = "${camera.name}-এ LAN রিবুট সিগন্যাল পাঠানো হয়েছে"
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
            _hudState.update { it.copy(snackbarMessage = "ভল্ট লগ পরিষ্কার করা হয়েছে") }
        }
    }
}
