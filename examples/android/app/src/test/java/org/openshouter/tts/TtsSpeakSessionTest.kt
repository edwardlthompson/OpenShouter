package org.openshouter.tts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Regression for GitHub #63: MediaSession.setCallback NPE when TtsController is
 * constructed on Dispatchers.Default (no Looper).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class TtsSpeakSessionTest {
    @Test
    fun constructsOffMainThreadWithoutNpe() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val error = AtomicReference<Throwable>()
        val done = CountDownLatch(1)
        Thread({
            try {
                TtsSpeakSession(context).release()
            } catch (t: Throwable) {
                error.set(t)
            } finally {
                done.countDown()
            }
        }, "tts-speak-session-off-main").start()
        assertTrue("worker finished", done.await(5, TimeUnit.SECONDS))
        assertNull("MediaSession must accept main-looper callback off-thread", error.get())
    }
}
