package com.animalbattle.game.data.remote

import com.animalbattle.game.domain.model.LeaderboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal HTTP client for the Animal Battle backend (Node.js + PostgreSQL).
 *
 * Uses org.json (part of the Android SDK) so no extra serialization
 * dependency is needed. When the backend is unreachable the app
 * transparently falls back to the local mock leaderboard so the game
 * remains fully playable offline.
 */
object ApiClientConfig {
    /** Base URL of the deployed backend. Blank = remote sync disabled (offline mock only). */
    var baseUrl: String = com.animalbattle.game.BuildConfig.API_BASE_URL

    /** Network calls fail fast so UI never blocks on a dead backend. */
    const val TIMEOUT_MS: Int = 3_000
}

/**
 * Thin REST client. Only endpoints the game consumes today are implemented;
 * add more as backend features ship.
 */
class GameApiClient {

    /** Weekly leaderboard; null when the backend is unreachable or disabled. */
    suspend fun fetchWeeklyLeaderboard(limit: Int = 100): List<LeaderboardEntry>? =
        withContext(Dispatchers.IO) {
            if (ApiClientConfig.baseUrl.isBlank()) return@withContext null
            try {
                val url = "${ApiClientConfig.baseUrl}/api/v1/leaderboard/weekly?limit=$limit"
                val body = httpCall(url, method = "GET") ?: return@withContext null
                val array = JSONArray(body)
                (0 until array.length()).map { i ->
                    val entry = array.getJSONObject(i)
                    LeaderboardEntry(
                        rank = entry.optInt("rank", i + 1),
                        playerName = entry.optString("player_name", "Player"),
                        trophies = entry.optInt("weekly_trophies", 0),
                        avatarEmoji = "⭐"
                    )
                }
            } catch (_: Exception) {
                null // offline fallback handled by caller
            }
        }

    /** Register/refresh this device's profile; returns remote player id or null. */
    suspend fun registerPlayer(deviceId: String, name: String): Long? =
        withContext(Dispatchers.IO) {
            if (ApiClientConfig.baseUrl.isBlank()) return@withContext null
            try {
                val url = "${ApiClientConfig.baseUrl}/api/v1/players"
                val payload = JSONObject().apply {
                    put("deviceId", deviceId)
                    put("name", name)
                }.toString()
                val body = httpCall(url, method = "POST", payload = payload)
                    ?: return@withContext null
                JSONObject(body).optLong("id", -1L).takeIf { it > 0 }
            } catch (_: Exception) {
                null
            }
        }

    /**
     * Submit a finished battle result to the backend.
     * Returns the server response (battleId, duplicate flag, updated player)
     * or null when the backend is unreachable.
     */
    suspend fun submitBattle(
        remotePlayerId: Long,
        clientBattleId: String,
        playerAnimalId: String,
        opponentName: String,
        opponentAnimalId: String,
        won: Boolean,
        rewardCoins: Int,
        rewardTrophies: Int
    ): JSONObject? = withContext(Dispatchers.IO) {
        if (ApiClientConfig.baseUrl.isBlank() || remotePlayerId <= 0) return@withContext null
        try {
            val url = "${ApiClientConfig.baseUrl}/api/v1/battles"
            val payload = JSONObject().apply {
                put("playerAnimalId", playerAnimalId)
                put("opponentName", opponentName)
                put("opponentAnimalId", opponentAnimalId)
                put("won", won)
                put("rewardCoins", rewardCoins)
                put("rewardTrophies", rewardTrophies)
                put("clientBattleId", clientBattleId)
            }.toString()
            val body = httpCall(
                url, method = "POST", payload = payload,
                extraHeaders = mapOf("X-Player-Id" to remotePlayerId.toString())
            ) ?: return@withContext null
            JSONObject(body)
        } catch (_: Exception) {
            null
        }
    }

    private fun httpCall(url: String, method: String, payload: String? = null, extraHeaders: Map<String, String> = emptyMap()): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = ApiClientConfig.TIMEOUT_MS
                readTimeout = ApiClientConfig.TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                for ((key, value) in extraHeaders) {
                    setRequestProperty(key, value)
                }
                if (payload != null) {
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.use { it.write(payload.toByteArray()) }
                }
            }
            val code = connection.responseCode
            if (code in 200..299) connection.inputStream.bufferedReader().readText() else null
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }
}
