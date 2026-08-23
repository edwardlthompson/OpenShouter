package org.openshouter.ui.tts

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import java.util.Locale
import org.openshouter.domain.TtsEngineChoice
import org.openshouter.domain.TtsLangCatalog
import org.openshouter.domain.TtsLocaleMenu
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsSourceCatalog
import org.openshouter.domain.TtsSourceOffer
import org.openshouter.domain.TtsVoice
import org.openshouter.domain.TtsVoiceCandidate
import org.openshouter.ui.menu.MenuDropdown
import org.openshouter.ui.menu.MenuLink

@Composable
fun TtsVoicePicker(
    playback: TtsPlaybackPolicy,
    onPlayback: (TtsPlaybackPolicy) -> Unit,
    languages: List<String>,
    voices: List<TtsVoiceCandidate> = emptyList(),
    engines: List<TtsEngineChoice>,
    downloads: List<TtsSourceOffer>,
    onOpenUrl: (String) -> Unit,
    onOpenApp: (String) -> Unit = {},
) {
    val voice = playback.voice
    val installedTags = remember(languages, voices) {
        (languages + voices.map { it.languageTag }).filter { it.isNotBlank() }.distinct()
    }
    val tags = remember(installedTags, voices, voice.minQuality) {
        TtsLangCatalog.filterTags(TtsLangCatalog.merge(installedTags), voices, voice.minQuality)
    }
    val installedPkgs = remember(engines) { engines.map { it.packageName }.toSet() }
    val ui = Locale.getDefault()
    val qualities = listOf(
        TtsVoice.QUALITY_VERY_HIGH to stringResource(R.string.tts_quality_very_high),
        TtsVoice.QUALITY_HIGH to stringResource(R.string.tts_quality_high),
        TtsVoice.QUALITY_NORMAL to stringResource(R.string.tts_quality_normal),
        TtsVoice.QUALITY_AUTO to stringResource(R.string.tts_quality_auto),
    )
    Text(stringResource(R.string.tts_quality_help), style = MaterialTheme.typography.bodySmall)
    MenuDropdown(
        label = stringResource(R.string.tts_quality),
        text = qualities.firstOrNull { it.first == voice.minQuality }?.second ?: qualities.first().second,
        options = qualities.map { it.first.toString() to it.second },
        onSelect = { raw ->
            val quality = raw.toIntOrNull() ?: TtsVoice.QUALITY_VERY_HIGH
            val keep = TtsLangCatalog.meets(voice.languageTag, voices, quality)
            onPlayback(
                playback.copy(
                    voice = voice.copy(
                        minQuality = quality,
                        voiceName = "",
                        languageTag = if (keep) voice.languageTag else "",
                    ).clamp(),
                ).clamp(),
            )
        },
    )
    TtsLocaleMenus(playback, onPlayback, tags, installedTags, installedPkgs, voices)
    if (installedTags.isEmpty() && tags.isEmpty()) {
        OutlinedTextField(
            value = voice.languageTag,
            onValueChange = { tag ->
                onPlayback(playback.copy(voice = voice.copy(languageTag = tag).clamp()).clamp())
            },
            label = { Text(stringResource(R.string.tts_language)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
    VoicePackLinks(voice.languageTag, installedTags, installedPkgs, ui, onOpenUrl, onOpenApp)
    Text(stringResource(R.string.tts_source_help), style = MaterialTheme.typography.bodySmall)
    val system = stringResource(R.string.tts_source_system)
    val engineOptions = listOf("" to system) + engines.map { it.packageName to sourceLabel(it.packageName, it.label) }
    MenuDropdown(
        label = stringResource(R.string.tts_source),
        text = engineOptions.firstOrNull { it.first == voice.engine }?.second ?: system,
        options = engineOptions,
        onSelect = { pkg ->
            onPlayback(playback.copy(voice = voice.copy(engine = pkg).clamp()).clamp())
        },
    )
    downloads.forEach { offer ->
        MenuLink(
            label = stringResource(R.string.tts_source_get, sourceLabel(offer.packageName, offer.packageName)),
            supporting = stringResource(
                if (offer.foss) R.string.tts_source_fdroid else R.string.tts_source_play,
            ),
            onClick = { onOpenUrl(offer.downloadUrl) },
            showDivider = true,
        )
    }
    NudgeRow(stringResource(R.string.tts_pitch), formatPitch(voice.pitch)) { sign ->
        onPlayback(playback.copy(voice = voice.copy(pitch = voice.pitch + sign * 0.1f).clamp()).clamp())
    }
}

@Composable
private fun VoicePackLinks(
    tag: String,
    installedTags: List<String>,
    installedPkgs: Set<String>,
    ui: Locale,
    onOpenUrl: (String) -> Unit,
    onOpenApp: (String) -> Unit,
) {
    if (tag.isBlank() || TtsLangCatalog.packInstalled(installedTags, tag)) return
    val lang = TtsLocaleMenu.displayLanguage(TtsLocaleMenu.languageCode(tag), ui)
    TtsLangCatalog.enginesFor(tag).forEach { pkg ->
        val offer = TtsSourceCatalog.known(pkg) ?: return@forEach
        val haveApp = pkg in installedPkgs
        MenuLink(
            label = stringResource(R.string.tts_voice_get, sourceLabel(pkg, pkg), lang),
            supporting = stringResource(
                if (haveApp) R.string.tts_voice_open_app else R.string.tts_source_fdroid,
            ),
            onClick = { if (haveApp) onOpenApp(pkg) else onOpenUrl(offer.downloadUrl) },
            showDivider = true,
        )
    }
}

@Composable
private fun sourceLabel(packageName: String, fallback: String): String {
    val res = when (packageName) {
        TtsSourceCatalog.GOOGLE -> R.string.tts_source_google
        TtsSourceCatalog.RHVOICE -> R.string.tts_source_rhvoice
        TtsSourceCatalog.SHERPA -> R.string.tts_source_sherpa
        else -> 0
    }
    return if (res != 0) stringResource(res) else fallback
}
