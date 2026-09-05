package com.animalbattle.game.data.save

import android.content.Context
import android.content.SharedPreferences
import com.animalbattle.game.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

// ── Save Version ───────────────────────────────────────────

const val SAVE_VERSION = 1

// ── Save Data Containers ───────────────────────────────────

data class PlayerSaveData(
    // Core
    val name: String = "Player",
    val totalXP: Int = 0,
    val coins: Int = 100,
    val trophies: Int = 0,

    // Animal
    val selectedAnimalId: String = "lion",
    val unlockedAnimals: List<String> = listOf("lion"),
    val animalUpgrades: Map<String, Int> = emptyMap(),
    val selectedSkins: Map<String, String> = emptyMap(), // animalId -> skinId

    // Progression
    val currentMapLevel: Int = 1,
    val completedStages: List<String> = emptyList(),
    val stageStars: Map<String, Int> = emptyMap(), // stageId -> stars (0-3)

    // Daily
    val dailyLoginStreak: Int = 0,
    val lastLoginDate: Long = 0L,
    val luckyWheelSpinsToday: Int = 0,
    val lastSpinDate: Long = 0L,

    // Boosts
    val activeBoosts: List<ActiveBoost> = emptyList(),

    // Chests
    val unlockedChests: List<String> = emptyList(), // chestIds that are available
    val openedChests: List<String> = emptyList(),

    // Statistics
    val statistics: PlayerStatistics = PlayerStatistics(),

    // Settings
    val settings: GameSettings = GameSettings(),

    // Metadata
    val saveVersion: Int = SAVE_VERSION,
    val lastSaved: Long = System.currentTimeMillis()
)

// ── Save Manager ───────────────────────────────────────────

class SaveManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "animal_battle_save",
        Context.MODE_PRIVATE
    )

    /**
     * Save player data to SharedPreferences.
     */
    fun save(data: PlayerSaveData) {
        val json = serializeSaveData(data)
        prefs.edit()
            .putString("save_data", json.toString())
            .putInt("save_version", SAVE_VERSION)
            .apply()
    }

    /**
     * Load player data from SharedPreferences.
     */
    fun load(): PlayerSaveData {
        val jsonStr = prefs.getString("save_data", null) ?: return PlayerSaveData()
        return try {
            val json = JSONObject(jsonStr)
            deserializeSaveData(json)
        } catch (e: Exception) {
            // Fallback to defaults on parse error
            PlayerSaveData()
        }
    }

    /**
     * Check if save exists.
     */
    fun hasSave(): Boolean = prefs.contains("save_data")

    /**
     * Clear all save data.
     */
    fun clearSave() {
        prefs.edit().clear().apply()
    }

    /**
     * Get save metadata without full deserialization.
     */
    fun getSaveInfo(): SaveInfo {
        return SaveInfo(
            version = prefs.getInt("save_version", 0),
            exists = prefs.contains("save_data"),
            lastSaved = prefs.getLong("last_saved", 0L)
        )
    }

    // ── Serialization ──────────────────────────────────────

    private fun serializeSaveData(data: PlayerSaveData): JSONObject {
        return JSONObject().apply {
            put("version", SAVE_VERSION)
            put("lastSaved", data.lastSaved)

            // Core
            put("name", data.name)
            put("totalXP", data.totalXP)
            put("coins", data.coins)
            put("trophies", data.trophies)

            // Animal
            put("selectedAnimalId", data.selectedAnimalId)
            put("unlockedAnimals", JSONArray(data.unlockedAnimals))
            put("animalUpgrades", serializeMap(data.animalUpgrades))
            put("selectedSkins", serializeMap(data.selectedSkins))

            // Progression
            put("currentMapLevel", data.currentMapLevel)
            put("completedStages", JSONArray(data.completedStages))
            put("stageStars", serializeMap(data.stageStars.mapValues { it.value }))

            // Daily
            put("dailyLoginStreak", data.dailyLoginStreak)
            put("lastLoginDate", data.lastLoginDate)
            put("luckyWheelSpinsToday", data.luckyWheelSpinsToday)
            put("lastSpinDate", data.lastSpinDate)

            // Boosts
            put("activeBoosts", serializeBoosts(data.activeBoosts))

            // Chests
            put("unlockedChests", JSONArray(data.unlockedChests))
            put("openedChests", JSONArray(data.openedChests))

            // Statistics
            put("statistics", serializeStatistics(data.statistics))

            // Settings
            put("settings", serializeSettings(data.settings))
        }
    }

    // ── Deserialization ────────────────────────────────────

    private fun deserializeSaveData(json: JSONObject): PlayerSaveData {
        return PlayerSaveData(
            name = json.optString("name", "Player"),
            totalXP = json.optInt("totalXP", 0),
            coins = json.optInt("coins", 100),
            trophies = json.optInt("trophies", 0),

            selectedAnimalId = json.optString("selectedAnimalId", "lion"),
            unlockedAnimals = jsonArrayToList(json.optJSONArray("unlockedAnimals") ?: JSONArray().apply { put("lion") }),
            animalUpgrades = deserializeMap(json.optJSONObject("animalUpgrades")),
            selectedSkins = deserializeMap(json.optJSONObject("selectedSkins")),

            currentMapLevel = json.optInt("currentMapLevel", 1),
            completedStages = jsonArrayToList(json.optJSONArray("completedStages")),
            stageStars = deserializeMap(json.optJSONObject("stageStars")).mapValues { it.value },

            dailyLoginStreak = json.optInt("dailyLoginStreak", 0),
            lastLoginDate = json.optLong("lastLoginDate", 0L),
            luckyWheelSpinsToday = json.optInt("luckyWheelSpinsToday", 0),
            lastSpinDate = json.optLong("lastSpinDate", 0L),

            activeBoosts = deserializeBoosts(json.optJSONArray("activeBoosts")),
            unlockedChests = jsonArrayToList(json.optJSONArray("unlockedChests")),
            openedChests = jsonArrayToList(json.optJSONArray("openedChests")),

            statistics = deserializeStatistics(json.optJSONObject("statistics")),
            settings = deserializeSettings(json.optJSONObject("settings")),

            saveVersion = json.optInt("version", SAVE_VERSION),
            lastSaved = json.optLong("lastSaved", System.currentTimeMillis())
        )
    }

    // ── Helper Serializers ─────────────────────────────────

    private fun serializeMap(map: Map<String, Any>): JSONObject {
        return JSONObject().apply {
            map.forEach { (key, value) -> put(key, value) }
        }
    }

    private fun deserializeMap(json: JSONObject?): Map<String, Int> {
        if (json == null) return emptyMap()
        val result = mutableMapOf<String, Int>()
        json.keys().forEach { key -> result[key] = json.optInt(key, 0) }
        return result
    }

    private fun jsonArrayToList(jsonArray: JSONArray): List<String> {
        return (0 until jsonArray.length()).map { jsonArray.getString(it) }
    }

    private fun serializeBoosts(boosts: List<ActiveBoost>): JSONArray {
        return JSONArray().apply {
            boosts.forEach { boost ->
                put(JSONObject().apply {
                    put("type", boost.type.name)
                    put("expiresAt", boost.expiresAt)
                    put("remainingMs", boost.remainingMs)
                })
            }
        }
    }

    private fun deserializeBoosts(jsonArray: JSONArray?): List<ActiveBoost> {
        if (jsonArray == null) return emptyList()
        return (0 until jsonArray.length()).mapNotNull { i ->
            val obj = jsonArray.optJSONObject(i) ?: return@mapNotNull null
            val typeName = obj.optString("type", "")
            val type = try { BoostType.valueOf(typeName) } catch (_: Exception) { null } ?: return@mapNotNull null
            ActiveBoost(
                type = type,
                expiresAt = obj.optLong("expiresAt", 0L),
                remainingMs = obj.optLong("remainingMs", 0L)
            )
        }.filter { it.isActive }
    }

    private fun serializeStatistics(stats: PlayerStatistics): JSONObject {
        return JSONObject().apply {
            put("totalBattles", stats.totalBattles)
            put("wins", stats.wins)
            put("losses", stats.losses)
            put("winStreak", stats.winStreak)
            put("bestWinStreak", stats.bestWinStreak)
            put("totalCoinsEarned", stats.totalCoinsEarned)
            put("totalXPEarned", stats.totalXPEarned)
            put("totalTrophiesEarned", stats.totalTrophiesEarned)
            put("perfectAnswers", stats.perfectAnswers)
            put("maxCombo", stats.maxCombo)
            put("stagesCompleted", stats.stagesCompleted)
            put("chestsOpened", stats.chestsOpened)
            put("skinsUnlocked", stats.skinsUnlocked)
            put("animalsUnlocked", stats.animalsUnlocked)
            put("playTimeMinutes", stats.playTimeMinutes)
            put("lastUpdated", stats.lastUpdated)
        }
    }

    private fun deserializeStatistics(json: JSONObject?): PlayerStatistics {
        if (json == null) return PlayerStatistics()
        return PlayerStatistics(
            totalBattles = json.optInt("totalBattles", 0),
            wins = json.optInt("wins", 0),
            losses = json.optInt("losses", 0),
            winStreak = json.optInt("winStreak", 0),
            bestWinStreak = json.optInt("bestWinStreak", 0),
            totalCoinsEarned = json.optInt("totalCoinsEarned", 0),
            totalXPEarned = json.optInt("totalXPEarned", 0),
            totalTrophiesEarned = json.optInt("totalTrophiesEarned", 0),
            perfectAnswers = json.optInt("perfectAnswers", 0),
            maxCombo = json.optInt("maxCombo", 0),
            stagesCompleted = json.optInt("stagesCompleted", 0),
            chestsOpened = json.optInt("chestsOpened", 0),
            skinsUnlocked = json.optInt("skinsUnlocked", 0),
            animalsUnlocked = json.optInt("animalsUnlocked", 0),
            playTimeMinutes = json.optInt("playTimeMinutes", 0),
            lastUpdated = json.optLong("lastUpdated", System.currentTimeMillis())
        )
    }

    private fun serializeSettings(settings: GameSettings): JSONObject {
        return JSONObject().apply {
            put("language", settings.language)
            put("soundEnabled", settings.soundEnabled)
            put("musicEnabled", settings.musicEnabled)
            put("notificationsEnabled", settings.notificationsEnabled)
        }
    }

    private fun deserializeSettings(json: JSONObject?): GameSettings {
        if (json == null) return GameSettings()
        return GameSettings(
            language = json.optString("language", "en"),
            soundEnabled = json.optBoolean("soundEnabled", true),
            musicEnabled = json.optBoolean("musicEnabled", true),
            notificationsEnabled = json.optBoolean("notificationsEnabled", true)
        )
    }
}

// ── Save Info ──────────────────────────────────────────────

data class SaveInfo(
    val version: Int,
    val exists: Boolean,
    val lastSaved: Long
)
