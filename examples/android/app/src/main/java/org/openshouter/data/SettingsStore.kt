package org.openshouter.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

internal val Context.osDataStore by preferencesDataStore("openshouter")
