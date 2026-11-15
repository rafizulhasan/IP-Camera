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
    fun `read app_name from context and verify Imou RTSP URL generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Imou LAN", appName)

        val camera = ImouCameraEntity(
            name = "Main Gate",
            modelName = "Imou Cruiser SE+",
            ipAddress = "192.168.1.108",
            rtspPort = 554,
            username = "admin",
            safetyCode = "L28A9F4C",
            channel = 1,
            preferredSubtype = 0
        )
        assertEquals(
            "rtsp://admin:L28A9F4C@192.168.1.108:554/cam/realmonitor?channel=1&subtype=0",
            camera.buildRtspUrl()
        )
        assertTrue(camera.buildMaskedRtspUrl().contains("••••••••"))
    }
}
