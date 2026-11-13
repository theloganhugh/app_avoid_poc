package com.reelblocker.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ViewIdProvider.
 * Verifies JSON parsing, fallback lists, and default configurations.
 */
class ViewIdProviderTest {

    private val provider = ViewIdProvider(remoteConfigProvider = { null })

    @Test
    fun parseJsonArray_validJson_returnsListOfStrings() {
        val json = """["view_id_1", "view_id_2", "view_id_3"]"""
        val result = provider.parseJsonArray(json)

        assertEquals(3, result.size)
        assertEquals("view_id_1", result[0])
        assertEquals("view_id_2", result[1])
        assertEquals("view_id_3", result[2])
    }

    @Test
    fun parseJsonArray_emptyString_returnsEmptyList() {
        val result = provider.parseJsonArray("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseJsonArray_blankString_returnsEmptyList() {
        val result = provider.parseJsonArray("    ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseJsonArray_malformedJson_returnsEmptyListGracefully() {
        val malformed = """[not valid json]"""
        val result = provider.parseJsonArray(malformed)
        assertTrue(result.isEmpty())
    }

    @Test
    fun getYouTubeShortsViewIds_fallback_returnsDefaultIds() {
        val ids = provider.getYouTubeShortsViewIds()
        assertEquals(ViewIdProvider.DEFAULT_YOUTUBE_SHORTS_IDS, ids)
        assertTrue(ids.contains("com.google.android.youtube:id/shorts_player_container"))
    }

    @Test
    fun getInstagramReelsViewIds_fallback_returnsDefaultIds() {
        val ids = provider.getInstagramReelsViewIds()
        assertEquals(ViewIdProvider.DEFAULT_INSTAGRAM_REELS_IDS, ids)
        assertTrue(ids.contains("com.instagram.android:id/clips_viewer_view_pager"))
    }

    @Test
    fun getBlockCooldownMs_fallback_returnsDefaultCooldown() {
        val cooldown = provider.getBlockCooldownMs()
        assertEquals(ViewIdProvider.DEFAULT_COOLDOWN_MS, cooldown)
    }

    @Test
    fun isKillSwitchActive_fallback_returnsFalse() {
        val isActive = provider.isKillSwitchActive()
        assertFalse(isActive)
    }
}
