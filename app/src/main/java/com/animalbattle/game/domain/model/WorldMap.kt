package com.animalbattle.game.domain.model

import com.animalbattle.game.R

// ── World Map ──────────────────────────────────────────────

data class WorldRegion(
    val id: String,
    val name: String,
    val nameResId: Int,
    val description: String,
    val environmentType: String, // Maps to EnvironmentType
    val stages: List<GameStage>,
    val requiredTrophies: Int,
    val isUnlocked: Boolean = false,
    val completionPercentage: Float = 0f
)

data class GameStage(
    val id: String,
    val regionId: String,
    val index: Int,
    val name: String,
    val nameResId: Int,
    val difficulty: StageDifficulty,
    val reward: StageReward,
    val isCompleted: Boolean = false,
    val isAvailable: Boolean = false,
    val stars: Int = 0, // 0-3 stars based on performance
    val bestTime: Long = 0L,
    val opponentAnimalId: String? = null
)

// ── World Map Configuration ────────────────────────────────

object WorldMapConfig {

    private val regions = listOf(
        createForestRegion(),
        createDesertRegion(),
        createArcticRegion(),
        createVolcanoRegion(),
        createIslandRegion(),
        createCastleRegion()
    )

    fun getRegions(): List<WorldRegion> = regions

    fun getRegion(id: String): WorldRegion? = regions.find { it.id == id }

    fun getStage(regionId: String, stageIndex: Int): GameStage? {
        return getRegion(regionId)?.stages?.getOrNull(stageIndex)
    }

    /**
     * Get next available stage for a player.
     */
    fun getNextAvailableStage(completedStages: List<String>): GameStage? {
        for (region in regions) {
            for (stage in region.stages) {
                if (stage.id !in completedStages) {
                    return stage
                }
            }
        }
        return null
    }

    /**
     * Calculate total completion percentage.
     */
    fun calculateCompletion(completedStages: List<String>): Float {
        val totalStages = regions.sumOf { it.stages.size }
        return if (totalStages > 0) completedStages.size.toFloat() / totalStages else 0f
    }

    /**
     * Get region completion status.
     */
    fun getRegionCompletion(regionId: String, completedStages: List<String>): Float {
        val region = getRegion(regionId) ?: return 0f
        val completed = region.stages.count { it.id in completedStages }
        return if (region.stages.isNotEmpty()) completed.toFloat() / region.stages.size else 0f
    }

    // ── Region Creators ────────────────────────────────────

    private fun createForestRegion() = WorldRegion(
        id = "forest",
        name = "Whispering Woods",
        nameResId = R.string.region_forest,
        description = "A mystical forest where ancient creatures dwell",
        environmentType = "FOREST",
        requiredTrophies = 0,
        stages = listOf(
            GameStage("forest_0", "forest", 0, "Forest Path", R.string.stage_forest_path, StageDifficulty.EASY, StageReward(30, 15, 0), opponentAnimalId = "wolf"),
            GameStage("forest_1", "forest", 1, "Mossy Glade", R.string.stage_mossy_glade, StageDifficulty.EASY, StageReward(35, 20, 1), opponentAnimalId = "bear"),
            GameStage("forest_2", "forest", 2, "Ancient Oak", R.string.stage_ancient_oak, StageDifficulty.MEDIUM, StageReward(50, 30, 1), opponentAnimalId = "gorilla"),
            GameStage("forest_3", "forest", 3, "Wolf Den", R.string.stage_wolf_den, StageDifficulty.MEDIUM, StageReward(60, 35, 2), opponentAnimalId = "wolf"),
            GameStage("forest_4", "forest", 4, "Forest Guardian", R.string.stage_forest_guardian, StageDifficulty.BOSS, StageReward(100, 60, 3), opponentAnimalId = "bear")
        )
    )

    private fun createDesertRegion() = WorldRegion(
        id = "desert",
        name = "Scorching Sands",
        nameResId = R.string.region_desert,
        description = "Endless dunes hiding ancient treasures",
        environmentType = "DESERT",
        requiredTrophies = 10,
        stages = listOf(
            GameStage("desert_0", "desert", 0, "Sand Dunes", R.string.stage_sand_dunes, StageDifficulty.EASY, StageReward(40, 20, 1), opponentAnimalId = "cheetah"),
            GameStage("desert_1", "desert", 1, "Oasis", R.string.stage_oasis, StageDifficulty.EASY, StageReward(45, 25, 1), opponentAnimalId = "cobra"),
            GameStage("desert_2", "desert", 2, "Ruins", R.string.stage_ruins, StageDifficulty.MEDIUM, StageReward(60, 35, 2), opponentAnimalId = "leopard"),
            GameStage("desert_3", "desert", 3, "Sandstorm", R.string.stage_sandstorm, StageDifficulty.HARD, StageReward(80, 45, 2), opponentAnimalId = "cheetah"),
            GameStage("desert_4", "desert", 4, "Desert King", R.string.stage_desert_king, StageDifficulty.BOSS, StageReward(120, 70, 4), opponentAnimalId = "lion")
        )
    )

    private fun createArcticRegion() = WorldRegion(
        id = "arctic",
        name = "Frozen Peaks",
        nameResId = R.string.region_arctic,
        description = "Icy mountains where only the strongest survive",
        environmentType = "ARCTIC",
        requiredTrophies = 25,
        stages = listOf(
            GameStage("arctic_0", "arctic", 0, "Snowfield", R.string.stage_snowfield, StageDifficulty.EASY, StageReward(50, 25, 1), opponentAnimalId = "wolf"),
            GameStage("arctic_1", "arctic", 1, "Ice Cave", R.string.stage_ice_cave, StageDifficulty.MEDIUM, StageReward(70, 40, 2), opponentAnimalId = "bear"),
            GameStage("arctic_2", "arctic", 2, "Frozen Lake", R.string.stage_frozen_lake, StageDifficulty.MEDIUM, StageReward(80, 45, 2), opponentAnimalId = "eagle"),
            GameStage("arctic_3", "arctic", 3, "Avalanche", R.string.stage_avalanche, StageDifficulty.HARD, StageReward(100, 55, 3), opponentAnimalId = "rhino"),
            GameStage("arctic_4", "arctic", 4, "Frost Titan", R.string.stage_frost_titan, StageDifficulty.BOSS, StageReward(150, 80, 5), opponentAnimalId = "elephant")
        )
    )

    private fun createVolcanoRegion() = WorldRegion(
        id = "volcano",
        name = "Molten Core",
        nameResId = R.string.region_volcano,
        description = "A volcanic wasteland of fire and fury",
        environmentType = "VOLCANO",
        requiredTrophies = 45,
        stages = listOf(
            GameStage("volcano_0", "volcano", 0, "Lava Field", R.string.stage_lava_field, StageDifficulty.MEDIUM, StageReward(70, 40, 2), opponentAnimalId = "crocodile"),
            GameStage("volcano_1", "volcano", 1, "Magma River", R.string.stage_magma_river, StageDifficulty.MEDIUM, StageReward(80, 45, 2), opponentAnimalId = "tiger"),
            GameStage("volcano_2", "volcano", 2, "Fire Gorge", R.string.stage_fire_gorge, StageDifficulty.HARD, StageReward(100, 55, 3), opponentAnimalId = "lion"),
            GameStage("volcano_3", "volcano", 3, "Eruption", R.string.stage_eruption, StageDifficulty.HARD, StageReward(120, 65, 3), opponentAnimalId = "bear"),
            GameStage("volcano_4", "volcano", 4, "Inferno Dragon", R.string.stage_inferno_dragon, StageDifficulty.BOSS, StageReward(180, 100, 6), opponentAnimalId = "eagle")
        )
    )

    private fun createIslandRegion() = WorldRegion(
        id = "island",
        name = "Mystery Island",
        nameResId = R.string.region_island,
        description = "A tropical paradise with hidden dangers",
        environmentType = "ISLAND",
        requiredTrophies = 70,
        stages = listOf(
            GameStage("island_0", "island", 0, "Beach", R.string.stage_beach, StageDifficulty.MEDIUM, StageReward(80, 45, 2), opponentAnimalId = "crocodile"),
            GameStage("island_1", "island", 1, "Jungle", R.string.stage_jungle, StageDifficulty.HARD, StageReward(100, 55, 3), opponentAnimalId = "gorilla"),
            GameStage("island_2", "island", 2, "Volcano", R.string.stage_island_volcano, StageDifficulty.HARD, StageReward(120, 65, 3), opponentAnimalId = "rhino"),
            GameStage("island_3", "island", 3, "Temple", R.string.stage_temple, StageDifficulty.HARD, StageReward(140, 75, 4), opponentAnimalId = "cobra"),
            GameStage("island_4", "island", 4, "Island Boss", R.string.stage_island_boss, StageDifficulty.BOSS, StageReward(200, 110, 7), opponentAnimalId = "tiger")
        )
    )

    private fun createCastleRegion() = WorldRegion(
        id = "castle",
        name = "Shadow Fortress",
        nameResId = R.string.region_castle,
        description = "The final stronghold of the ultimate champion",
        environmentType = "CASTLE",
        requiredTrophies = 100,
        stages = listOf(
            GameStage("castle_0", "castle", 0, "Castle Gates", R.string.stage_castle_gates, StageDifficulty.HARD, StageReward(100, 55, 3), opponentAnimalId = "lion"),
            GameStage("castle_1", "castle", 1, "Throne Room", R.string.stage_throne_room, StageDifficulty.HARD, StageReward(120, 65, 3), opponentAnimalId = "tiger"),
            GameStage("castle_2", "castle", 2, "Tower", R.string.stage_tower, StageDifficulty.HARD, StageReward(140, 75, 4), opponentAnimalId = "eagle"),
            GameStage("castle_3", "castle", 3, "Dungeon", R.string.stage_dungeon, StageDifficulty.BOSS, StageReward(180, 90, 5), opponentAnimalId = "cobra"),
            GameStage("castle_4", "castle", 4, "Final Boss", R.string.stage_final_boss, StageDifficulty.BOSS, StageReward(300, 150, 10), opponentAnimalId = "dragon")
        )
    )
}
