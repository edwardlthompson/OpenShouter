package org.openshouter.domain

object RingerSilent {
    fun active(ringerNormal: Boolean, dndActive: Boolean): Boolean = !ringerNormal || dndActive
}
