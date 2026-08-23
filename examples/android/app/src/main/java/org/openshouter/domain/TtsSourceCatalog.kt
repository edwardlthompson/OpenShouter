package org.openshouter.domain

data class TtsEngineChoice(
    val packageName: String,
    val label: String,
)

data class TtsSourceOffer(
    val packageName: String,
    val downloadUrl: String,
    val foss: Boolean,
)

object TtsSourceCatalog {
    const val GOOGLE = "com.google.android.tts"
    const val RHVOICE = "com.github.olga_yakovleva.rhvoice.android"
    const val SHERPA = "org.woheller69.ttsengine"

    val OFFERS = listOf(
        TtsSourceOffer(GOOGLE, "https://play.google.com/store/apps/details?id=$GOOGLE", false),
        TtsSourceOffer(RHVOICE, "https://f-droid.org/packages/$RHVOICE/", true),
        TtsSourceOffer(SHERPA, "https://f-droid.org/packages/$SHERPA/", true),
    )

    fun missing(installed: Set<String>): List<TtsSourceOffer> =
        OFFERS.filter { it.packageName !in installed }

    fun known(packageName: String): TtsSourceOffer? =
        OFFERS.firstOrNull { it.packageName == packageName }
}
