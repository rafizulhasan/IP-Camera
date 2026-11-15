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
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.outlined.ControlCamera
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Radar
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
import com.example.ui.components.CameraEditorDialog
import com.example.ui.components.SavePtzPresetDialog
import com.example.ui.screens.DedicatedPtzScreen
import com.example.ui.screens.LanScannerScreen
import com.example.ui.screens.LiveMatrixScreen
import com.example.ui.screens.OfflineLanGuideScreen
import com.example.ui.screens.VaultAndLogsScreen
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanContainer
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
                ImouLanApp()
            }
        }
    }
}

@Composable
fun ImouLanApp(
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

    LaunchedEffect(hudState.snackbarMessage) {
        val msg = hudState.snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // BackHandler on secondary tabs returns to LIVE_MATRIX
    if (hudState.activeTab != MainNavTab.LIVE_MATRIX) {
        BackHandler {
            viewModel.selectTab(MainNavTab.LIVE_MATRIX)
        }
    }

    val navItems = listOf(
        Triple(MainNavTab.LIVE_MATRIX, stringResource(R.string.nav_monitor), Icons.Filled.GridView to Icons.Outlined.GridView),
        Triple(MainNavTab.PTZ_CONSOLE, stringResource(R.string.nav_ptz), Icons.Filled.ControlCamera to Icons.Outlined.ControlCamera),
        Triple(MainNavTab.LAN_SCANNER, stringResource(R.string.nav_scanner), Icons.Filled.Radar to Icons.Outlined.Radar),
        Triple(MainNavTab.LOCAL_VAULT, stringResource(R.string.nav_vault), Icons.Filled.FolderSpecial to Icons.Outlined.FolderSpecial),
        Triple(MainNavTab.OFFLINE_GUIDE, stringResource(R.string.nav_guide), Icons.Filled.HelpOutline to Icons.Outlined.HelpOutline)
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
                        actionColor = CyberCyan
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
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyanContainer,
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
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyanContainer,
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
                        MainNavTab.LIVE_MATRIX -> {
                            LiveMatrixScreen(
                                cameras = cameras,
                                selectedCamera = selectedCamera,
                                presetsForSelected = presetsForSelected,
                                hudState = hudState,
                                onSelectCamera = { viewModel.selectCamera(it) },
                                onToggleLayoutMode = { viewModel.toggleMatrixLayoutMode() },
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
                                onToggleStreamQuality = { viewModel.toggleStreamQuality(it) },
                                onToggleAudio = { viewModel.toggleAudioMute(it) },
                                onCaptureSnapshot = { viewModel.captureSnapshotToVault(it) },
                                onToggleRecording = { viewModel.toggleRecording(it) },
                                onProbeCamera = { viewModel.probeSingleCamera(it) },
                                onPinchZoom = { cam, z -> viewModel.setCameraZoom(cam, z) },
                                onPtzDirection = { cam, dir -> viewModel.movePtz(cam, dir) },
                                onCenterPtz = { viewModel.centerCameraPtz(it) },
                                onSpeedChange = { viewModel.setPtzSpeed(it) },
                                onSelectPreset = { cam, preset -> viewModel.applyPtzPreset(cam, preset) },
                                onAddPresetRequest = { cam -> presetSaveCamera = cam },
                                onDeletePreset = { viewModel.deletePreset(it) },
                                onNightVisionChange = { cam, mode -> viewModel.updateNightVisionMode(cam, mode) },
                                onToggleSpotlight = { viewModel.toggleSpotlight(it) },
                                onToggleSiren = { viewModel.toggleSiren(it) },
                                onToggleAllSirens = { viewModel.toggleAllCamerasSiren() },
                                onTogglePrivacyMask = { viewModel.togglePrivacyMask(it) },
                                onToggleFlip180 = { viewModel.toggleFlip180(it) },
                                onToggleSmartTracking = { viewModel.toggleSmartTracking(it) },
                                onRebootCamera = { viewModel.rebootCameraOverLan(it) }
                            )
                        }

                        MainNavTab.PTZ_CONSOLE -> {
                            DedicatedPtzScreen(
                                cameras = cameras,
                                selectedCamera = selectedCamera,
                                presetsForSelected = presetsForSelected,
                                hudState = hudState,
                                onSelectCamera = { viewModel.selectCamera(it) },
                                onToggleStreamQuality = { viewModel.toggleStreamQuality(it) },
                                onToggleAudio = { viewModel.toggleAudioMute(it) },
                                onCaptureSnapshot = { viewModel.captureSnapshotToVault(it) },
                                onToggleRecording = { viewModel.toggleRecording(it) },
                                onProbeCamera = { viewModel.probeSingleCamera(it) },
                                onPinchZoom = { cam, z -> viewModel.setCameraZoom(cam, z) },
                                onPtzDirection = { cam, dir -> viewModel.movePtz(cam, dir) },
                                onCenterPtz = { viewModel.centerCameraPtz(it) },
                                onSpeedChange = { viewModel.setPtzSpeed(it) },
                                onSelectPreset = { cam, preset -> viewModel.applyPtzPreset(cam, preset) },
                                onAddPresetRequest = { cam -> presetSaveCamera = cam },
                                onDeletePreset = { viewModel.deletePreset(it) },
                                onNightVisionChange = { cam, mode -> viewModel.updateNightVisionMode(cam, mode) },
                                onToggleSpotlight = { viewModel.toggleSpotlight(it) },
                                onToggleSiren = { viewModel.toggleSiren(it) },
                                onTogglePrivacyMask = { viewModel.togglePrivacyMask(it) },
                                onToggleFlip180 = { viewModel.toggleFlip180(it) },
                                onToggleSmartTracking = { viewModel.toggleSmartTracking(it) },
                                onRebootCamera = { viewModel.rebootCameraOverLan(it) }
                            )
                        }

                        MainNavTab.LAN_SCANNER -> {
                            LanScannerScreen(
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

                        MainNavTab.LOCAL_VAULT -> {
                            VaultAndLogsScreen(
                                events = vaultEvents,
                                onDeleteEvent = { viewModel.deleteVaultEvent(it) },
                                onClearAllEvents = { viewModel.clearAllVaultEvents() }
                            )
                        }

                        MainNavTab.OFFLINE_GUIDE -> {
                            OfflineLanGuideScreen(cameras = cameras)
                        }
                    }
                }
            }
        }
    }

    if (showCameraEditor) {
        CameraEditorDialog(
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
