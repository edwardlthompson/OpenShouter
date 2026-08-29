package org.openshouter.silence

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OemSilenceHintsTest {
    @Test
    fun colorOsFamilyNeedsTheSilentFile() {
        assertTrue(OemSilenceHints.needsSilentFile("OnePlus"))
        assertTrue(OemSilenceHints.needsSilentFile("OPPO"))
        assertTrue(OemSilenceHints.needsSilentFile("realme"))
        assertTrue(OemSilenceHints.needsSilentFile("oplus"))
        assertFalse(OemSilenceHints.needsSilentFile("Google"))
        assertFalse(OemSilenceHints.needsSilentFile(""))
        assertFalse(
            OemSilenceHints.needsSilentFile(
                "OnePlus",
                "23.2-20260409-NIGHTLY-waffle lineage_waffle-userdebug",
            ),
        )
        assertTrue(OemSilenceHints.isAospCustomRom("lineage_dodge-userdebug"))
        assertFalse(OemSilenceHints.isAospCustomRom(""))
    }
}
