package com.animalbattle.game

import com.animalbattle.game.data.remote.ApiClientConfig
import com.animalbattle.game.data.remote.GameApiClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GameApiClient].
 *
 * These tests verify guard conditions (blank URL, zero player id) and
 * the offline-safe fallback paths without requiring a live network.
 */
class GameApiClientTest {

    private lateinit var client: GameApiClient
    private var originalBaseUrl: String = ""

    @Before
    fun setUp() {
        client = GameApiClient()
        originalBaseUrl = ApiClientConfig.baseUrl
    }

    @org.junit.After
    fun tearDown() {
        ApiClientConfig.baseUrl = originalBaseUrl
    }

    // ── Health ──────────────────────────────────────────────

    @Test
    fun `checkHealth returns false when baseUrl is blank`() = runTest {
        ApiClientConfig.baseUrl = ""
        assertFalse(client.checkHealth())
    }

    // ── Register ────────────────────────────────────────────

    @Test
    fun `registerPlayer returns null when baseUrl is blank`() = runTest {
        ApiClientConfig.baseUrl = ""
        assertNull(client.registerPlayer("device-001", "TestPlayer"))
    }

    // ── Submit Battle ───────────────────────────────────────

    @Test
    fun `submitBattle returns null when baseUrl is blank`() = runTest {
        ApiClientConfig.baseUrl = ""
        val result = client.submitBattle(
            remotePlayerId = 1L,
            clientBattleId = "b1",
            playerAnimalId = "lion",
            opponentName = "Shadow",
            opponentAnimalId = "tiger",
            won = true,
            rewardCoins = 25,
            rewardTrophies = 1
        )
        assertNull(result)
    }

    @Test
    fun `submitBattle returns null when remotePlayerId is zero`() = runTest {
        ApiClientConfig.baseUrl = "http://localhost:3000"
        val result = client.submitBattle(
            remotePlayerId = 0L,
            clientBattleId = "b1",
            playerAnimalId = "lion",
            opponentName = "Shadow",
            opponentAnimalId = "tiger",
            won = true,
            rewardCoins = 25,
            rewardTrophies = 1
        )
        assertNull(result)
    }

    @Test
    fun `submitBattle returns null when remotePlayerId is negative`() = runTest {
        ApiClientConfig.baseUrl = "http://localhost:3000"
        val result = client.submitBattle(
            remotePlayerId = -1L,
            clientBattleId = "b1",
            playerAnimalId = "lion",
            opponentName = "Shadow",
            opponentAnimalId = "tiger",
            won = true,
            rewardCoins = 25,
            rewardTrophies = 1
        )
        assertNull(result)
    }

    // ── Leaderboard ─────────────────────────────────────────

    @Test
    fun `fetchWeeklyLeaderboard returns null when baseUrl is blank`() = runTest {
        ApiClientConfig.baseUrl = ""
        assertNull(client.fetchWeeklyLeaderboard())
    }

    @Test
    fun `fetchWeeklyLeaderboard returns null when unreachable`() = runTest {
        // Use a port that almost certainly has no server
        ApiClientConfig.baseUrl = "http://192.0.2.1:19999"
        assertNull(client.fetchWeeklyLeaderboard(limit = 5))
    }

    // ── Config ──────────────────────────────────────────────

    @Test
    fun `timeout is positive`() {
        assertTrue(ApiClientConfig.TIMEOUT_MS > 0)
    }

    @Test
    fun `timeout is reasonable for mobile`() {
        assertTrue(ApiClientConfig.TIMEOUT_MS <= 10_000)
    }
}
