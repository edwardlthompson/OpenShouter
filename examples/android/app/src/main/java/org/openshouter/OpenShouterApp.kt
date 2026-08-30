package org.openshouter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.openshouter.crashcapture.CrashCapture

@HiltAndroidApp
class OpenShouterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashCapture.install(this)
    }
}
