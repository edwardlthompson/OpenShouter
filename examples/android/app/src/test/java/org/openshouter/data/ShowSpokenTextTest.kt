package org.openshouter.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.foss.goldenpath.clearPreferenceDataStores
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [26])
class ShowSpokenTextTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun resetDataStore() {
        context.clearPreferenceDataStores()
    }

    @Test
    fun defaultsOffAndPersistsOn() = runBlocking {
        val repo = SettingsRepository(context)
        assertFalse(repo.snapshot().showSpokenText)
        repo.setShowSpokenText(true)
        assertTrue(repo.settings.first().showSpokenText)
        repo.setShowSpokenText(false)
        assertFalse(repo.settings.first().showSpokenText)
    }
}
