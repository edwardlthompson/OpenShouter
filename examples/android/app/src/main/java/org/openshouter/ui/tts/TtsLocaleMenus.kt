package org.openshouter.ui.tts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.foss.goldenpath.R
import java.util.Locale
import org.openshouter.domain.TtsLangCatalog
import org.openshouter.domain.TtsLocaleMenu
import org.openshouter.domain.TtsPlaybackPolicy
import org.openshouter.domain.TtsVoice
import org.openshouter.domain.TtsVoiceCandidate
import org.openshouter.ui.menu.MenuDropdown

@Composable
fun TtsLocaleMenus(
    playback: TtsPlaybackPolicy,
    onPlayback: (TtsPlaybackPolicy) -> Unit,
    tags: List<String>,
    installedTags: List<String>,
    installedPkgs: Set<String>,
    voices: List<TtsVoiceCandidate> = emptyList(),
) {
    val ui = Locale.getDefault()
    val voice = playback.voice
    val selectedLang = TtsLocaleMenu.languageCode(voice.languageTag)
    val langOptions = TtsLocaleMenu.languages(tags, ui)
    val accentOptions = TtsLocaleMenu.accents(tags, selectedLang, ui)
    val defaultLang = stringResource(R.string.tts_language_default)
    MenuDropdown(
        label = stringResource(R.string.tts_language_menu),
        text = if (selectedLang.isEmpty()) defaultLang else TtsLocaleMenu.displayLanguage(selectedLang, ui),
        options = listOf("" to defaultLang) + langOptions.map { it to TtsLocaleMenu.displayLanguage(it, ui) },
        onSelect = { code ->
            val next = if (code.isEmpty()) "" else TtsLocaleMenu.accents(tags, code, ui).firstOrNull().orEmpty()
            onPlayback(playback.copy(voice = applyLocale(voice, next, installedTags, installedPkgs)).clamp())
        },
    )
    if (selectedLang.isNotEmpty() && accentOptions.isNotEmpty()) {
        MenuDropdown(
            label = stringResource(R.string.tts_accent),
            text = TtsLocaleMenu.displayAccent(voice.languageTag, ui).ifBlank { voice.languageTag },
            options = accentOptions.map { it to TtsLocaleMenu.displayAccent(it, ui).ifBlank { it } },
            onSelect = { tag ->
                onPlayback(playback.copy(voice = applyLocale(voice, tag, installedTags, installedPkgs)).clamp())
            },
        )
    }
    val named = TtsLocaleMenu.voicesFor(voices, voice.languageTag)
        .filter { voice.minQuality <= 0 || it.quality >= voice.minQuality }
    if (named.isNotEmpty()) {
        val auto = stringResource(R.string.tts_voice_auto)
        MenuDropdown(
            label = stringResource(R.string.tts_voice_pick),
            text = named.firstOrNull { it.name == voice.voiceName }?.let { TtsLocaleMenu.shortName(it.name) } ?: auto,
            options = listOf("" to auto) + named.map { it.name to TtsLocaleMenu.shortName(it.name) },
            onSelect = { name ->
                onPlayback(playback.copy(voice = voice.copy(voiceName = name).clamp()).clamp())
            },
        )
    }
}

private fun applyLocale(
    voice: TtsVoice,
    tag: String,
    installedTags: List<String>,
    installedPkgs: Set<String>,
) = voice.copy(
    languageTag = tag,
    voiceName = "",
    engine = TtsLangCatalog.keepOrPrefer(voice.engine, tag, installedTags, installedPkgs, voice.minQuality),
).clamp()
