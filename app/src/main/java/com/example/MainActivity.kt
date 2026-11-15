package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.ImouCameraEntity
import com.example.ui.ImouLanViewModel
import com.example.ui.MainNavTab
import com.example.ui.components.ImouCameraEditorDialog
import com.example.ui.components.SavePtzPresetDialog
import com.example.ui.screens.ImouHomeScreen
import com.example.ui.screens.ImouLanCenterScreen
import com.example.ui.screens.ImouMessagesAndAlbumScreen
import com.example.ui.screens.ImouMultiLensLiveScreen
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.ImouOrange
import com.example.ui.theme.ImouOrangeContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextPureWhite
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ImouSmartLifeLanApp()
            }
        }
    }
}

@Composable
fun ImouSmartLifeLanApp(
    viewModel: ImouLanViewModel = viewModel()
) {
    val cameras by viewModel.cameras.collectAsStateWithLifecycle()
    val allPresets by viewModel.allPresets.collectAsStateWithLifecycle()
    val vaultEvents by viewModel.vaultEvents.collectAsStateWithLifecycle()
    val hudState by viewModel.hudState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showCameraEditor by remember { mutableStateOf(false) }
    var editingCamera by remember { mutableStateOf<ImouCameraEntity?>(null) }
    var prefillIpForEditor by remember { mutableStateOf<String?>(null) }
    var presetSaveCamera by remember { mutableStateOf<ImouCameraEntity?>(null) }

    val selectedCamera = remember(cameras, hudState.selectedCameraId) {
        cameras.find { it.id == hudState.selectedCameraId } ?: cameras.firstOrNull()
    }

    val presetsForSelected = remember(allPresets, selectedCamera) {
        if (selectedCamera == null) emptyList()
        else allPresets.filter { it.cameraId == selectedCamera.id }
    }

    val eventsForSelected = remember(vaultEvents, selectedCamera) {
        if (selectedCamera == null) emptyList()
        else vaultEvents.filter { it.cameraId == selectedCamera.id }
    }

    LaunchedEffect(hudState.snackbarMessage) {
        val msg = hudState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // BackHandler on secondary tabs returns to IMOU_HOME
    if (hudState.activeTab != MainNavTab.IMOU_HOME) {
        BackHandler {
            viewModel.selectTab(MainNavTab.IMOU_HOME)
        }
    }

    val navItems = listOf(
        Triple(MainNavTab.IMOU_HOME, stringResource(R.string.nav_home), Icons.Filled.Home to Icons.Outlined.Home),
        Triple(MainNavTab.MULTI_LENS_LIVE, stringResource(R.string.nav_live), Icons.Filled.Videocam to Icons.Outlined.Videocam),
        Triple(MainNavTab.MESSAGES_ALBUM, stringResource(R.string.nav_messages), Icons.Filled.Notifications to Icons.Outlined.Notifications),
        Triple(MainNavTab.LAN_SETTINGS, stringResource(R.string.nav_lan_hub), Icons.Filled.Radar to Icons.Outlined.Radar)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = PitchBlack,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = CarbonCard,
                        contentColor = TextPureWhite,
                        actionColor = ImouOrange
                    )
                }
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        contentColor = TextPureWhite
                    ) {
                        navItems.forEach { (tab, label, icons) ->
                            val selected = hudState.activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ImouOrange,
                                    selectedTextColor = ImouOrange,
                                    indicatorColor = ImouOrangeContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PitchBlack)
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        contentColor = TextPureWhite,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        navItems.forEach { (tab, label, icons) ->
                            val selected = hudState.activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) icons.first else icons.second,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ImouOrange,
                                    selectedTextColor = ImouOrange,
                                    indicatorColor = ImouOrangeContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_rail_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (hudState.activeTab) {
                        MainNavTab.IMOU_HOME -> {
                            ImouHomeScreen(
                                cameras = cameras,
                                networkInfo = hudState.networkInfo,
                                probeResults = hudState.probeResults,
                                onOpenCameraLiveView = { camId -> viewModel.openCameraLiveView(camId) },
                                onAddCameraClick = {
                                    editingCamera = null
                                    prefillIpForEditor = null
                                    showCameraEditor = true
                                },
                                onEditCameraClick = { cam ->
                                    editingCamera = cam
                                    prefillIpForEditor = null
                                    showCameraEditor = true
                                },
                                onOpenLanScannerClick = { viewModel.selectTab(MainNavTab.LAN_SETTINGS) },
                                onToggleCameraSiren = { viewModel.toggleSiren(it) },
                                onToggleCameraSpotlight = { viewModel.toggleSpotlight(it) },
                                onToggleCameraPrivacy = { viewModel.togglePrivacyMask(it) },
                                onToggleAllSirens = { viewModel.toggleAllCamerasSiren() }
                            )
                        }

                        MainNavTab.MULTI_LENS_LIVE -> {
                            ImouMultiLensLiveScreen(
                                cameras = cameras,
                                selectedCamera = selectedCamera,
                                presetsForSelected = presetsForSelected,
                                eventsForSelected = eventsForSelected,
                                hudState = hudState,
                                onBackToHome = { viewModel.selectTab(MainNavTab.IMOU_HOME) },
                                onSelectCamera = { viewModel.selectCamera(it) },
                                onEditCameraSettings = { cam ->
                                    editingCamera = cam
                                    prefillIpForEditor = null
                                    showCameraEditor = true
                                },
                                onProbeCamera = { viewModel.probeSingleCamera(it) },
                                onClick2TrackTap = { cam, lensIdx, nx, ny ->
                                    viewModel.performClick2Track(cam, lensIdx, nx, ny)
                                },
                                onCaptureSnapshot = { viewModel.captureSnapshotToVault(it) },
                                onToggleRecording = { viewModel.toggleRecording(it) },
                                onToggleAudio = { viewModel.toggleAudioMute(it) },
                                onToggleTalk = { viewModel.toggleTwoWayTalk(it) },
                                onToggleStreamQuality = { viewModel.toggleStreamQuality(it) },
                                onToggleSpotlight = { viewModel.toggleSpotlight(it) },
                                onToggleSiren = { viewModel.toggleSiren(it) },
                                onPtzDirection = { cam, dir -> viewModel.movePtz(cam, dir) },
                                onCenterPtz = { viewModel.centerCameraPtz(it) },
                                onSpeedChange = { viewModel.setPtzSpeed(it) },
                                onZoomChange = { cam, z -> viewModel.setCameraZoom(cam, z) },
                                onSelectPreset = { cam, preset -> viewModel.applyPtzPreset(cam, preset) },
                                onAddPresetRequest = { cam -> presetSaveCamera = cam },
                                onDeletePreset = { viewModel.deletePreset(it) },
                                onNightVisionChange = { cam, mode -> viewModel.updateNightVisionMode(cam, mode) },
                                onToggleHumanDetect = { viewModel.toggleHumanDetect(it) },
                                onToggleSmartTracking = { viewModel.toggleSmartTracking(it) },
                                onToggleClick2Track = { viewModel.toggleClick2Track(it) },
                                onToggleAutoCruise = { viewModel.toggleAutoCruise(it) },
                                onTogglePrivacyMask = { viewModel.togglePrivacyMask(it) },
                                onToggleFlip180 = { viewModel.toggleFlip180(it) },
                                onRebootCamera = { viewModel.rebootCameraOverLan(it) }
                            )
                        }

                        MainNavTab.MESSAGES_ALBUM -> {
                            ImouMessagesAndAlbumScreen(
                                events = vaultEvents,
                                onDeleteEvent = { viewModel.deleteVaultEvent(it) },
                                onClearAllEvents = { viewModel.clearAllVaultEvents() }
                            )
                        }

                        MainNavTab.LAN_SETTINGS -> {
                            ImouLanCenterScreen(
                                networkInfo = hudState.networkInfo,
                                isScanning = hudState.isScanningSubnet,
                                scanProgress = hudState.scanProgressPercent,
                                currentScanIp = hudState.currentScanIp,
                                discoveredDevices = hudState.discoveredDevices,
                                cameras = cameras,
                                probeResults = hudState.probeResults,
                                onStartSubnetScan = { viewModel.startSubnetDiscovery(it) },
                                onProbeAllCameras = { viewModel.probeAllCamerasOnLan() },
                                onSyncSubnetForAllCameras = { viewModel.syncAllCamerasToSubnet(it) },
                                onAddDiscoveredCamera = { ip ->
                                    editingCamera = null
                                    prefillIpForEditor = ip
                                    showCameraEditor = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCameraEditor) {
        ImouCameraEditorDialog(
            initialCamera = editingCamera,
            defaultSubnetPrefix = hudState.networkInfo.subnetPrefix,
            prefillIp = prefillIpForEditor,
            onDismiss = { showCameraEditor = false },
            onSave = { cam ->
                viewModel.saveOrUpdateCamera(cam)
                showCameraEditor = false
            },
            onDelete = if (editingCamera != null) {
                { cam ->
                    viewModel.deleteCamera(cam)
                    showCameraEditor = false
                }
            } else null
        )
    }

    val targetPresetCamera = presetSaveCamera
    if (targetPresetCamera != null) {
        val nextNum = (presetsForSelected.maxOfOrNull { it.presetNumber } ?: 0) + 1
        SavePtzPresetDialog(
            camera = targetPresetCamera,
            nextPresetNumber = nextNum,
            onDismiss = { presetSaveCamera = null },
            onConfirmSave = { title, num ->
                viewModel.saveCurrentPositionAsPreset(targetPresetCamera, title, num)
                presetSaveCamera = null
            }
        )
    }
}
