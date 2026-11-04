package org.openshouter.tts

import org.junit.Assert.assertEquals
import org.junit.Test

class TtsBindPolicyTest {
    @Test
    fun successClearsRetryPath() {
        assertEquals(TtsBindPhase.Ready, TtsBindPolicy.afterInit(success = true, alreadyRetried = false))
        assertEquals(TtsBindPhase.Ready, TtsBindPolicy.afterInit(success = true, alreadyRetried = true))
    }

    @Test
    fun failureRetriesOnceThenShowsVisibleFailure() {
        assertEquals(TtsBindPhase.RetryOnce, TtsBindPolicy.afterInit(success = false, alreadyRetried = false))
        assertEquals(TtsBindPhase.FailedVisible, TtsBindPolicy.afterInit(success = false, alreadyRetried = true))
    }

    @Test
    fun throwableAlsoRetriesOnce() {
        assertEquals(TtsBindPhase.RetryOnce, TtsBindPolicy.afterThrowable(alreadyRetried = false))
        assertEquals(TtsBindPhase.FailedVisible, TtsBindPolicy.afterThrowable(alreadyRetried = true))
    }
}
