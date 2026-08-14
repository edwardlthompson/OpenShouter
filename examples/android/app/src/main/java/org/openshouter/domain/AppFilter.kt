package org.openshouter.domain

object AppFilter {
    fun allows(packageName: String, settings: AppSettings): Boolean {
        val listed = packageName in settings.listedPackages
        return when (settings.filterMode) {
            FilterMode.BLACKLIST -> !listed
            FilterMode.WHITELIST -> listed
        }
    }
}
