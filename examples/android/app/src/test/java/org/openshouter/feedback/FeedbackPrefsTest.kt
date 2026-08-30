package org.openshouter.feedback

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.openshouter.crashcapture.PendingCrash
import org.openshouter.crashcapture.PendingCrashStore
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class FeedbackPrefsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun saveCrashesDefaultsOffAndClearDeletesQueue() {
        val prefs = FeedbackPrefs(context)
        assertFalse(prefs.saveCrashes())
        val store = PendingCrashStore(context)
        assertTrue(store.write(PendingCrash("boom", "stack")))
        prefs.setSaveCrashes(true)
        assertTrue(prefs.saveCrashes())
        prefs.setSaveCrashes(false)
        assertFalse(prefs.saveCrashes())
        assertTrue(store.read() == null)
    }
}
