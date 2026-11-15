package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ImouCameraEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name and verify 3-lens and 2-lens RTSP URLs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Imou LAN", appName)

        val camera = ImouCameraEntity(
            name = "Main Gate 3-Lens",
            modelName = "Imou Cruiser Triple 3-Lens",
            ipAddress = "192.168.1.108",
            rtspPort = 554,
            username = "admin",
            safetyCode = "L28A9F4C",
            lensCount = 3,
            lens1Channel = 1,
            lens2Channel = 2,
            lens3Channel = 3,
            ptzControlChannel = 3,
            preferredSubtype = 0
        )
        assertEquals(3, camera.getLensDescriptors().size)
        assertEquals(
            "rtsp://admin:L28A9F4C@192.168.1.108:554/cam/realmonitor?channel=1&subtype=0",
            camera.buildRtspUrlForChannel(1)
        )
        assertEquals(
            "rtsp://admin:L28A9F4C@192.168.1.108:554/cam/realmonitor?channel=2&subtype=0",
            camera.buildRtspUrlForChannel(2)
        )
        assertEquals(
            "rtsp://admin:L28A9F4C@192.168.1.108:554/cam/realmonitor?channel=3&subtype=0",
            camera.buildRtspUrlForChannel(3)
        )
        assertTrue(camera.buildMaskedRtspUrl(2).contains("••••••••"))
    }
}
