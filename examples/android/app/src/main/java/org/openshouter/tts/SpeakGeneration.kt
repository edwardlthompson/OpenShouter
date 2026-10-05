package org.openshouter.tts

/** Invalidates in-flight speak/delay/loop work when interrupt bumps the generation. */
object SpeakGeneration {
    fun bump(current: Long): Long = current + 1L

    fun isCurrent(current: Long, started: Long): Boolean = current == started
}
