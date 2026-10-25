package de.tum.informatics.www1.artemis.native_app.core.ui.media

import android.app.DownloadManager
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.common.artemis_context.*
import de.tum.informatics.www1.artemis.native_app.core.ui.AttachmentHandler
import de.tum.informatics.www1.artemis.native_app.core.ui.LocalArtemisContextProvider
import de.tum.informatics.www1.artemis.native_app.core.ui.R
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
class AttachmentDownloadBoundaryTest {
    @get:Rule val compose = createComposeRule()
    @Test fun foreignOtherAttachmentDownloadReachesReceiverWithoutArtemisCredentials() = download(false)
    @Test fun redirectedOtherAttachmentDownloadStripsCredentialsAtForeignReceiver() = download(true)

    private fun download(redirect: Boolean) {
        val source = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val foreign = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val received = AtomicInteger()
        var foreignCookie: String? = null
        var foreignAuthorization: String? = null
        var sourceCookie: String? = null
        val origin = "http://127.0.0.1:${source.address.port}"
        val foreignUrl = "http://127.0.0.1:${foreign.address.port}/file.zip"
        foreign.createContext("/file.zip") { exchange ->
            foreignCookie = exchange.requestHeaders.getFirst("Cookie")
            foreignAuthorization = exchange.requestHeaders.getFirst("Authorization")
            exchange.sendResponseHeaders(200, 4)
            exchange.responseBody.use { it.write(byteArrayOf(1, 2, 3, 4)) }
            received.incrementAndGet()
        }
        source.createContext("/file.zip") { exchange ->
            sourceCookie = exchange.requestHeaders.getFirst("Cookie")
            exchange.responseHeaders.add("Location", foreignUrl)
            exchange.sendResponseHeaders(302, -1); exchange.close()
        }
        source.start(); foreign.start()
        val provider = object : ArtemisContextProvider {
            override val stateFlow = MutableStateFlow<ArtemisContext>(ArtemisContextImpl.LoggedIn(origin, "private-token", "alice"))
            override fun setCourseId(courseId: Long) {}
            override fun clearCourseId() {}
        }
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val exported = java.io.File(context.cacheDir, "exported-test.zip")
            val downloads = object : android.content.ContentProvider() {
                override fun onCreate() = true
                override fun getType(uri: android.net.Uri) = "application/zip"
                override fun insert(uri: android.net.Uri, values: android.content.ContentValues?) = uri.buildUpon().appendPath("1").build()
                override fun update(uri: android.net.Uri, values: android.content.ContentValues?, selection: String?, args: Array<out String>?) = 1
                override fun delete(uri: android.net.Uri, selection: String?, args: Array<out String>?) = 1
                override fun query(uri: android.net.Uri, projection: Array<out String>?, selection: String?, args: Array<out String>?, order: String?): android.database.Cursor? = null
                override fun openFile(uri: android.net.Uri, mode: String): android.os.ParcelFileDescriptor = android.os.ParcelFileDescriptor.open(exported, android.os.ParcelFileDescriptor.MODE_CREATE or android.os.ParcelFileDescriptor.MODE_READ_WRITE or android.os.ParcelFileDescriptor.MODE_TRUNCATE)
            }
            downloads.attachInfo(context, android.content.pm.ProviderInfo().apply { authority = "media" })
            org.robolectric.shadows.ShadowContentResolver.registerProviderInternal("media", downloads)
            val dismissed = java.util.concurrent.atomic.AtomicBoolean(false)
            compose.setContent { MaterialTheme {
                CompositionLocalProvider(LocalArtemisContextProvider provides provider) {
                    AttachmentHandler(if (redirect) "$origin/file.zip" else foreignUrl, { dismissed.set(true) })
                }
            } }
            compose.onNodeWithText(context.getString(R.string.download_file_attachment_dialog_positive)).performClick()
            compose.waitUntil(15_000) { received.get() == 1 }
            compose.waitUntil(15_000) { dismissed.get() }
            assertArrayEquals(byteArrayOf(1, 2, 3, 4), exported.readBytes())
            assertNull(foreignCookie)
            assertNull(foreignAuthorization)
            if (redirect) assertEquals("jwt=private-token", sourceCookie)
            assertEquals(0, shadowOf(context.getSystemService(DownloadManager::class.java)).requestCount)
        } finally { source.stop(0); foreign.stop(0) }
    }
}
