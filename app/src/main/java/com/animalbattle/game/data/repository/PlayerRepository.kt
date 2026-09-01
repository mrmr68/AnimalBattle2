package com.animalbattle.game.data.repository

import com.animalbattle.game.data.datastore.PlayerDataStore
import com.animalbattle.game.domain.model.BattleRecord
import com.animalbattle.game.domain.model.GameSettings
import com.animalbattle.game.domain.model.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface PlayerRepository {
    val playerFlow: Flow<Player>
    suspend fun updatePlayer(player: Player)
    suspend fun updateSettings(settings: GameSettings)
    suspend fun addCoins(amount: Int)
    suspend fun removeCoins(amount: Int)
    suspend fun addTrophies(amount: Int)
    suspend fun addBattleRecord(record: BattleRecord)
    suspend fun selectAnimal(animalId: String)
    suspend fun unlockAnimal(animalId: String, cost: Int)
    suspend fun upgradeAnimal(animalId: String, cost: Int)
    suspend fun completeMapLevel(level: Int)
}

class PlayerRepositoryImpl(
    private val dataStore: PlayerDataStore
) : PlayerRepository {

    override val playerFlow: Flow<Player> = dataStore.playerFlow

    override suspend fun updatePlayer(player: Player) {
        dataStore.updatePlayer(player)
    }

    override suspend fun updateSettings(settings: GameSettings) {
        dataStore.updateSettings(settings)
    }

    override suspend fun addCoins(amount: Int) {
        val player = dataStore.playerFlow.first()
        dataStore.updatePlayer(player.copy(coins = player.coins + amount))
    }

    override suspend fun removeCoins(amount: Int) {
        val player = dataStore.playerFlow.first()
        if (player.coins >= amount) {
            dataStore.updatePlayer(player.copy(coins = player.coins - amount))
        }
    }

    override suspend fun addTrophies(amount: Int) {
        val player = dataStore.playerFlow.first()
        val newTrophies = player.trophies + amount
        dataStore.updatePlayer(
            player.copy(
                trophies = newTrophies,
                level = (newTrophies / Player.XP_PER_LEVEL) + 1
            )
        )
    }

    override suspend fun addBattleRecord(record: BattleRecord) {
        val player = dataStore.playerFlow.first()
        val updatedBattles = player.recentBattles + record
        dataStore.updatePlayer(player.copy(recentBattles = updatedBattles))
    }

    override suspend fun selectAnimal(animalId: String) {
        val player = dataStore.playerFlow.first()
        if (animalId in player.unlockedAnimals) {
            dataStore.updatePlayer(player.copy(selectedAnimalId = animalId))
        }
    }

    override suspend fun unlockAnimal(animalId: String, cost: Int) {
        val player = dataStore.playerFlow.first()
        if (player.coins >= cost && animalId !in player.unlockedAnimals) {
            dataStore.updatePlayer(
                player.copy(
                    coins = player.coins - cost,
                    unlockedAnimals = player.unlockedAnimals + animalId
                )
            )
        }
    }

    override suspend fun upgradeAnimal(animalId: String, cost: Int) {
        val player = dataStore.playerFlow.first()
        if (player.coins >= cost) {
            val currentLevel = player.animalUpgrades[animalId] ?: 0
            dataStore.updatePlayer(
                player.copy(
                    coins = player.coins - cost,
                    animalUpgrades = player.animalUpgrades + (animalId to currentLevel + 1)
                )
            )
        }
    }

    override suspend fun completeMapLevel(level: Int) {
        val player = dataStore.playerFlow.first()
        if (level !in player.completedLevels) {
            dataStore.updatePlayer(
                player.copy(
                    completedLevels = player.completedLevels + level,
                    currentMapLevel = maxOf(player.currentMapLevel, level + 1)
                )
            )
        }
    }
}
