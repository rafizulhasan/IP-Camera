package com.example.network

import com.example.data.local.ImouCameraEntity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class LocalNetworkInfo(
    val deviceIp: String,
    val subnetPrefix: String,
    val interfaceName: String,
    val isPrivateLan: Boolean
)

data class DiscoveredLanDevice(
    val ipAddress: String,
    val rtspPortOpen: Boolean,
    val httpPortOpen: Boolean,
    val dahuaTcpPortOpen: Boolean,
    val latencyMs: Long,
    val serverBanner: String,
    val vendorHint: String,
    val suggestedRtspUrl: String
)

data class RtspProbeResult(
    val cameraId: Long,
    val ipAddress: String,
    val isReachable: Boolean,
    val rtspPortOpen: Boolean,
    val httpPortOpen: Boolean,
    val latencyMs: Long,
    val rtspStatusLine: String,
    val serverHeader: String,
    val checkedAt: Long = System.currentTimeMillis()
)

data class LanCommandResult(
    val success: Boolean,
    val latencyMs: Long,
    val httpCode: Int,
    val protocolUsed: String,
    val endpoint: String,
    val summaryMessage: String
)

enum class PtzDirection(val dahuaCode: String, val labelBn: String, val panDelta: Float, val tiltDelta: Float, val zoomDelta: Float = 0f) {
    UP("Up", "উপরে (Tilt Up)", 0f, 8f),
    DOWN("Down", "নিচে (Tilt Down)", 0f, -8f),
    LEFT("Left", "বামে (Pan Left)", -12f, 0f),
    RIGHT("Right", "ডানে (Pan Right)", 12f, 0f),
    LEFT_UP("LeftUp", "বামে-উপরে", -9f, 6f),
    RIGHT_UP("RightUp", "ডানে-উপরে", 9f, 6f),
    LEFT_DOWN("LeftDown", "বামে-নিচে", -9f, -6f),
    RIGHT_DOWN("RightDown", "ডানে-নিচে", 9f, -6f),
    ZOOM_IN("ZoomTele", "জুম ইন (+)", 0f, 0f, 0.25f),
    ZOOM_OUT("ZoomWide", "জুম আউট (-)", 0f, 0f, -0.25f)
}

class ImouLanController {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(1200, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .writeTimeout(1500, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build()

    fun detectLocalNetwork(): LocalNetworkInfo {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            var fallbackIp = "192.168.1.50"
            var fallbackInterface = "wlan0"

            for (netIf in interfaces) {
                if (!netIf.isUp || netIf.isLoopback) continue
                val name = netIf.name.lowercase()
                for (addr in netIf.inetAddresses) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val ip = addr.hostAddress ?: continue
                        val parts = ip.split(".")
                        if (parts.size == 4) {
                            val prefix = "${parts[0]}.${parts[1]}.${parts[2]}"
                            if (ip.startsWith("192.168.") || ip.startsWith("172.") || name.contains("wlan") || name.contains("eth")) {
                                return LocalNetworkInfo(
                                    deviceIp = ip,
                                    subnetPrefix = prefix,
                                    interfaceName = netIf.name,
                                    isPrivateLan = true
                                )
                            }
                            fallbackIp = ip
                            fallbackInterface = netIf.name
                        }
                    }
                }
            }
            val parts = fallbackIp.split(".")
            val prefix = if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}" else "192.168.1"
            LocalNetworkInfo(
                deviceIp = fallbackIp,
                subnetPrefix = prefix,
                interfaceName = fallbackInterface,
                isPrivateLan = fallbackIp.startsWith("192.168.") || fallbackIp.startsWith("10.")
            )
        } catch (e: Exception) {
            LocalNetworkInfo(
                deviceIp = "192.168.1.50",
                subnetPrefix = "192.168.1",
                interfaceName = "wlan0",
                isPrivateLan = true
            )
        }
    }

    suspend fun verifyCameraRtspHandshake(camera: ImouCameraEntity): RtspProbeResult = withContext(Dispatchers.IO) {
        val startNs = System.nanoTime()
        val ip = camera.ipAddress.trim()
        var rtspOpen = false
        var httpOpen = false
        var statusLine = "No response (Host offline on LAN)"
        var serverHeader = "Unknown"

        try {
            Socket().use { socket ->
                socket.soTimeout = 700
                socket.connect(InetSocketAddress(ip, camera.rtspPort), 650)
                rtspOpen = true
                val req = buildString {
                    append("OPTIONS rtsp://$ip:${camera.rtspPort}/cam/realmonitor?channel=${camera.channel}&subtype=${camera.preferredSubtype} RTSP/1.0\r\n")
                    append("CSeq: 1\r\n")
                    append("User-Agent: ImouLAN-Direct/1.0\r\n")
                    append("\r\n")
                }
                socket.getOutputStream().write(req.toByteArray(Charsets.UTF_8))
                socket.getOutputStream().flush()

                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val firstLine = reader.readLine()
                if (!firstLine.isNullOrBlank()) {
                    statusLine = firstLine.trim()
                } else {
                    statusLine = "RTSP Port ${camera.rtspPort} Open (Ready)"
                }
                var line: String?
                var linesRead = 0
                while (reader.readLine().also { line = it } != null && linesRead < 10) {
                    linesRead++
                    val l = line ?: break
                    if (l.isBlank()) break
                    if (l.startsWith("Server:", ignoreCase = true)) {
                        serverHeader = l.substringAfter(":").trim()
                    }
                }
            }
        } catch (e: Exception) {
            rtspOpen = false
        }

        try {
            Socket().use { httpSocket ->
                httpSocket.connect(InetSocketAddress(ip, camera.httpPort), 450)
                httpOpen = true
            }
        } catch (_: Exception) {
            httpOpen = false
        }

        val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
        RtspProbeResult(
            cameraId = camera.id,
            ipAddress = ip,
            isReachable = rtspOpen || httpOpen,
            rtspPortOpen = rtspOpen,
            httpPortOpen = httpOpen,
            latencyMs = elapsedMs,
            rtspStatusLine = if (rtspOpen) statusLine else if (httpOpen) "HTTP Port ${camera.httpPort} Open (RTSP ${camera.rtspPort} closed)" else "Unreachable on local subnet",
            serverHeader = serverHeader
        )
    }

    suspend fun scanLocalSubnet(
        subnetPrefix: String,
        onProgress: (Int, String) -> Unit
    ): List<DiscoveredLanDevice> = withContext(Dispatchers.IO) {
        val discovered = mutableListOf<DiscoveredLanDevice>()
        val wsDiscoveredIps = sendOnvifWsDiscoveryProbe()

        val semaphore = Semaphore(32)
        val totalHosts = 254
        var scannedCount = 0

        val results = coroutineScope {
            (1..totalHosts).map { hostLastOctet ->
                async {
                    semaphore.withPermit {
                        val ip = "$subnetPrefix.$hostLastOctet"
                        val start = System.nanoTime()
                        val rtspOpen = isTcpPortOpen(ip, 554, 180)
                        val dahuaOpen = if (!rtspOpen) isTcpPortOpen(ip, 37777, 140) else isTcpPortOpen(ip, 37777, 180)
                        val httpOpen = if (rtspOpen || dahuaOpen || wsDiscoveredIps.contains(ip)) {
                            isTcpPortOpen(ip, 80, 180)
                        } else {
                            false
                        }

                        scannedCount++
                        if (scannedCount % 12 == 0 || scannedCount == totalHosts) {
                            onProgress((scannedCount * 100) / totalHosts, ip)
                        }

                        if (rtspOpen || dahuaOpen || wsDiscoveredIps.contains(ip)) {
                            val latency = ((System.nanoTime() - start) / 1_000_000L).coerceAtLeast(2L)
                            val banner = if (rtspOpen) grabRtspBanner(ip, 554) else "Dahua/ONVIF Device"
                            val vendor = when {
                                dahuaOpen || banner.contains("Dahua", ignoreCase = true) || banner.contains("RtspServer", ignoreCase = true) ->
                                    "Imou / Dahua LAN Camera"
                                wsDiscoveredIps.contains(ip) -> "ONVIF IP Camera"
                                else -> "RTSP IP Camera"
                            }
                            DiscoveredLanDevice(
                                ipAddress = ip,
                                rtspPortOpen = rtspOpen,
                                httpPortOpen = httpOpen,
                                dahuaTcpPortOpen = dahuaOpen,
                                latencyMs = latency,
                                serverBanner = banner,
                                vendorHint = vendor,
                                suggestedRtspUrl = "rtsp://admin:<SAFETY_CODE>@$ip:554/cam/realmonitor?channel=1&subtype=0"
                            )
                        } else {
                            null
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }

        discovered.addAll(results)
        discovered.sortedBy { it.ipAddress }
    }

    private fun sendOnvifWsDiscoveryProbe(): Set<String> {
        val foundIps = mutableSetOf<String>()
        try {
            DatagramSocket().use { socket ->
                socket.soTimeout = 350
                socket.broadcast = true
                val probeUuid = UUID.randomUUID().toString()
                val soapProbe = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <e:Envelope xmlns:e="http://www.w3.org/2003/05/soap-envelope"
                        xmlns:w="http://schemas.xmlsoap.org/ws/2004/08/addressing"
                        xmlns:d="http://schemas.xmlsoap.org/ws/2005/04/discovery"
                        xmlns:dn="http://www.onvif.org/ver10/network/wsdl">
                        <e:Header>
                            <w:MessageID>uuid:$probeUuid</w:MessageID>
                            <w:To e:mustUnderstand="true">urn:schemas-xmlsoap-org:ws:2005:04:discovery</w:To>
                            <w:Action a:mustUnderstand="true" xmlns:a="http://www.w3.org/2003/05/soap-envelope">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>
                        </e:Header>
                        <e:Body>
                            <d:Probe>
                                <d:Types>dn:NetworkVideoTransmitter</d:Types>
                            </d:Probe>
                        </e:Body>
                    </e:Envelope>
                """.trimIndent()
                val bytes = soapProbe.toByteArray(Charsets.UTF_8)
                val group = InetAddress.getByName("239.255.255.250")
                val packet = DatagramPacket(bytes, bytes.size, group, 3702)
                socket.send(packet)

                val recvBuf = ByteArray(4096)
                val start = System.currentTimeMillis()
                while (System.currentTimeMillis() - start < 350) {
                    val recvPacket = DatagramPacket(recvBuf, recvBuf.size)
                    socket.receive(recvPacket)
                    val senderIp = recvPacket.address?.hostAddress
                    if (!senderIp.isNullOrBlank()) {
                        foundIps.add(senderIp)
                    }
                }
            }
        } catch (_: Exception) {
            // Normal timeout after listening for UDP multicast responses
        }
        return foundIps
    }

    private fun isTcpPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { s ->
                s.connect(InetSocketAddress(ip, port), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun grabRtspBanner(ip: String, port: Int): String {
        return try {
            Socket().use { s ->
                s.soTimeout = 300
                s.connect(InetSocketAddress(ip, port), 250)
                val req = "OPTIONS rtsp://$ip:$port/ RTSP/1.0\r\nCSeq: 1\r\n\r\n"
                s.getOutputStream().write(req.toByteArray(Charsets.UTF_8))
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val status = reader.readLine() ?: "RTSP/1.0 Ready"
                status.trim()
            }
        } catch (_: Exception) {
            "RTSP Port 554 Active"
        }
    }

    suspend fun sendPtzMoveCommand(
        camera: ImouCameraEntity,
        direction: PtzDirection,
        speed: Int = 5
    ): LanCommandResult = withContext(Dispatchers.IO) {
        val cgiPath = "/cgi-bin/ptz.cgi?action=start&channel=${camera.channel}&code=${direction.dahuaCode}&arg1=0&arg2=${speed.coerceIn(1, 8)}&arg3=0"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        val startResult = executeAuthenticatedCgiGet(camera, url, cgiPath)

        // Automatically send stop pulse after short movement burst for step accuracy
        val stopPath = "/cgi-bin/ptz.cgi?action=stop&channel=${camera.channel}&code=${direction.dahuaCode}&arg1=0&arg2=1&arg3=0"
        val stopUrl = "http://${camera.ipAddress.trim()}:${camera.httpPort}$stopPath"
        executeAuthenticatedCgiGet(camera, stopUrl, stopPath)

        startResult
    }

    suspend fun sendPtzPresetCommand(
        camera: ImouCameraEntity,
        presetNumber: Int,
        setPreset: Boolean = false
    ): LanCommandResult = withContext(Dispatchers.IO) {
        val code = if (setPreset) "SetPreset" else "GotoPreset"
        val cgiPath = "/cgi-bin/ptz.cgi?action=start&channel=${camera.channel}&code=$code&arg1=0&arg2=$presetNumber&arg3=0"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        executeAuthenticatedCgiGet(camera, url, cgiPath)
    }

    suspend fun setNightVisionMode(
        camera: ImouCameraEntity,
        mode: String
    ): LanCommandResult = withContext(Dispatchers.IO) {
        // Dahua/Imou DayNightColor config: 0 = Auto, 1 = Full Color, 2 = Black & White (IR)
        val modeValue = when (mode) {
            "FULL_COLOR" -> 1
            "IR_BW" -> 2
            else -> 0
        }
        val cgiPath = "/cgi-bin/configManager.cgi?action=setConfig&VideoInDayNight[0][0].Mode=$modeValue"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        executeAuthenticatedCgiGet(camera, url, cgiPath)
    }

    suspend fun toggleWhiteLightSpotlight(
        camera: ImouCameraEntity,
        enable: Boolean
    ): LanCommandResult = withContext(Dispatchers.IO) {
        val mode = if (enable) "Manual" else "Off"
        val cgiPath = "/cgi-bin/configManager.cgi?action=setConfig&Lighting[0][0].Mode=$mode"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        executeAuthenticatedCgiGet(camera, url, cgiPath)
    }

    suspend fun triggerActiveDeterrenceSiren(
        camera: ImouCameraEntity,
        enable: Boolean
    ): LanCommandResult = withContext(Dispatchers.IO) {
        val ioState = if (enable) 1 else 0
        val cgiPath = "/cgi-bin/coaxialControlIO.cgi?action=control&channel=${camera.channel}&info[0].Type=2&info[0].IO=$ioState"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        executeAuthenticatedCgiGet(camera, url, cgiPath)
    }

    suspend fun toggleImageFlip180(
        camera: ImouCameraEntity,
        flip: Boolean
    ): LanCommandResult = withContext(Dispatchers.IO) {
        val cgiPath = "/cgi-bin/configManager.cgi?action=setConfig&VideoInOptions[0].Flip=$flip&VideoInOptions[0].Mirror=$flip"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        executeAuthenticatedCgiGet(camera, url, cgiPath)
    }

    suspend fun rebootCameraOnLan(camera: ImouCameraEntity): LanCommandResult = withContext(Dispatchers.IO) {
        val cgiPath = "/cgi-bin/magicBox.cgi?action=reboot"
        val url = "http://${camera.ipAddress.trim()}:${camera.httpPort}$cgiPath"
        executeAuthenticatedCgiGet(camera, url, cgiPath)
    }

    private fun executeAuthenticatedCgiGet(
        camera: ImouCameraEntity,
        fullUrl: String,
        uriPath: String
    ): LanCommandResult {
        val startNs = System.nanoTime()
        return try {
            val initialReq = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", "ImouLAN-Controller/1.0")
                .build()

            httpClient.newCall(initialReq).execute().use { firstResp ->
                if (firstResp.isSuccessful) {
                    val ms = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
                    return LanCommandResult(
                        success = true,
                        latencyMs = ms,
                        httpCode = firstResp.code,
                        protocolUsed = "Dahua HTTP CGI",
                        endpoint = uriPath,
                        summaryMessage = "LAN CGI 200 OK (${ms}ms)"
                    )
                }

                if (firstResp.code == 401) {
                    val wwwAuth = firstResp.header("WWW-Authenticate").orEmpty()
                    val authHeader = if (wwwAuth.startsWith("Digest", ignoreCase = true)) {
                        buildDigestAuthHeader(
                            username = camera.username.trim(),
                            password = camera.safetyCode.trim(),
                            method = "GET",
                            uri = uriPath,
                            wwwAuthenticate = wwwAuth
                        )
                    } else {
                        Credentials.basic(camera.username.trim(), camera.safetyCode.trim())
                    }

                    val authedReq = Request.Builder()
                        .url(fullUrl)
                        .header("Authorization", authHeader)
                        .header("User-Agent", "ImouLAN-Controller/1.0")
                        .build()

                    httpClient.newCall(authedReq).execute().use { secondResp ->
                        val ms = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
                        return LanCommandResult(
                            success = secondResp.isSuccessful,
                            latencyMs = ms,
                            httpCode = secondResp.code,
                            protocolUsed = "Dahua Digest CGI",
                            endpoint = uriPath,
                            summaryMessage = if (secondResp.isSuccessful) {
                                "LAN Digest CGI 200 OK (${ms}ms)"
                            } else {
                                "HTTP ${secondResp.code} (Safety Code যাচাই করুন)"
                            }
                        )
                    }
                }

                val ms = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
                LanCommandResult(
                    success = false,
                    latencyMs = ms,
                    httpCode = firstResp.code,
                    protocolUsed = "Dahua HTTP CGI",
                    endpoint = uriPath,
                    summaryMessage = "HTTP ${firstResp.code} Response (${ms}ms)"
                )
            }
        } catch (e: Exception) {
            // Try ONVIF SOAP fallback note and return structured local telemetry result
            val ms = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            LanCommandResult(
                success = false,
                latencyMs = ms,
                httpCode = 0,
                protocolUsed = "LAN CGI / ONVIF Queue",
                endpoint = uriPath,
                summaryMessage = "লোকাল প্রোফাইল আপডেট হয়েছে • ক্যামেরা অফলাইন (${camera.ipAddress})"
            )
        }
    }

    private fun buildDigestAuthHeader(
        username: String,
        password: String,
        method: String,
        uri: String,
        wwwAuthenticate: String
    ): String {
        fun extractParam(name: String): String {
            val regex = Regex("""$name="([^"]*)"""", RegexOption.IGNORE_CASE)
            return regex.find(wwwAuthenticate)?.groupValues?.getOrNull(1).orEmpty()
        }

        val realm = extractParam("realm")
        val nonce = extractParam("nonce")
        val qop = extractParam("qop").split(",").map { it.trim() }.firstOrNull { it == "auth" } ?: ""
        val opaque = extractParam("opaque")
        val nc = "00000001"
        val cnonce = UUID.randomUUID().toString().replace("-", "").take(16)

        val ha1 = md5Hex("$username:$realm:$password")
        val ha2 = md5Hex("$method:$uri")
        val responseHash = if (qop.isNotEmpty()) {
            md5Hex("$ha1:$nonce:$nc:$cnonce:$qop:$ha2")
        } else {
            md5Hex("$ha1:$nonce:$ha2")
        }

        return buildString {
            append("Digest username=\"$username\", ")
            append("realm=\"$realm\", ")
            append("nonce=\"$nonce\", ")
            append("uri=\"$uri\", ")
            if (qop.isNotEmpty()) {
                append("qop=$qop, nc=$nc, cnonce=\"$cnonce\", ")
            }
            append("response=\"$responseHash\"")
            if (opaque.isNotEmpty()) {
                append(", opaque=\"$opaque\"")
            }
        }
    }

    private fun md5Hex(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
