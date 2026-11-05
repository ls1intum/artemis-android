package de.tum.informatics.www1.artemis.native_app.core.ui.markdown.link_resolving

import android.app.Application
import android.content.Intent
import android.provider.Browser
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.sun.net.httpserver.HttpServer
import de.tum.informatics.www1.artemis.native_app.core.common.test.UnitTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Category(UnitTest::class)
class ReviewMarkdownLinkResolverTest {
    private val context: Application = ApplicationProvider.getApplicationContext()

    @Test fun `foreign PDF opens a credential-free browser intent without a native PDF request or redirected hop`() {
        val requests = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange -> requests.incrementAndGet(); exchange.responseHeaders.add("Location", "https://foreign.example/collect"); exchange.sendResponseHeaders(302, -1); exchange.close() }; start()
        }
        try {
            val url = "http://127.0.0.1:${server.address.port}/feedback.pdf"
            val resolver = ReviewMarkdownLinkResolver("https://artemis.example", { fail("Foreign resources do not use protected handoff") }, { fail("Valid external PDF") })
            resolver.resolve(View(context), url)
            val intent = shadowOf(context).nextStartedActivity
            assertEquals(Intent.ACTION_VIEW, intent.action)
            assertEquals(url, intent.dataString)
            assertFalse(intent.hasExtra(Browser.EXTRA_HEADERS))
            assertFalse(intent.extras.toString().contains("jwt="))
            assertEquals(0, requests.get())
        } finally { server.stop(0) }
    }

    @Test fun `protected relative PDF requires an explicit browser sign-in handoff and receives no app authentication`() {
        var pending: String? = null
        val resolver = ReviewMarkdownLinkResolver("https://artemis.example", { pending = it }, { fail("Valid protected file") })
        resolver.resolve(View(context), "/api/core/files/report.pdf")
        assertNull(shadowOf(context).nextStartedActivity)
        assertEquals("https://artemis.example/api/core/files/report.pdf", pending)
        openReviewBrowser(context, pending!!)
        val intent = shadowOf(context).nextStartedActivity
        assertEquals(pending, intent.dataString)
        assertFalse(intent.hasExtra(Browser.EXTRA_HEADERS))
        assertFalse(intent.extras.toString().contains("jwt="))
    }

    @Test fun `unsupported link schemes are explicit and never enter a native credential-bearing reader`() {
        var unsupported = 0
        val resolver = ReviewMarkdownLinkResolver("https://artemis.example", { fail("No protected request") }, { unsupported++ })
        resolver.resolve(View(context), "file:///private/account.pdf")
        resolver.resolve(View(context), "https://user:password@foreign.example/file.pdf")
        assertEquals(2, unsupported)
        assertNull(shadowOf(context).nextStartedActivity)
    }
}
