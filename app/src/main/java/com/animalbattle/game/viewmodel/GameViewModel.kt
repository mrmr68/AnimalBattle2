package com.animalbattle.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.animalbattle.game.data.datastore.PlayerDataStore
import com.animalbattle.game.data.repository.PlayerRepository
import com.animalbattle.game.data.repository.PlayerRepositoryImpl
import com.animalbattle.game.domain.model.Animal
import com.animalbattle.game.domain.model.AnimalData
import com.animalbattle.game.domain.model.BattlePhase
import com.animalbattle.game.domain.model.BattleRecord
import com.animalbattle.game.domain.model.BattleResult
import com.animalbattle.game.domain.model.BattleState
import com.animalbattle.game.domain.model.DailyLoginReward
import com.animalbattle.game.domain.model.GameConfig
import com.animalbattle.game.domain.model.GameSettings
import com.animalbattle.game.domain.model.LeaderboardEntry
import com.animalbattle.game.domain.model.LevelStatus
import com.animalbattle.game.domain.model.MapLevel
import com.animalbattle.game.domain.model.Player
import com.animalbattle.game.domain.model.Question
import com.animalbattle.game.domain.model.QuestionData
import com.animalbattle.game.domain.model.Reward
import com.animalbattle.game.domain.model.RewardType
import com.animalbattle.game.domain.model.ShopItem
import com.animalbattle.game.domain.model.WheelSegment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlayerRepository
    private val _player = MutableStateFlow(Player())
    val player: StateFlow<Player> = _player.asStateFlow()

    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState.asStateFlow()

    private val _dailyLoginRewards = MutableStateFlow<List<DailyLoginReward>>(emptyList())
    val dailyLoginRewards: StateFlow<List<DailyLoginReward>> = _dailyLoginRewards.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    private val _mapLevels = MutableStateFlow<List<MapLevel>>(emptyList())
    val mapLevels: StateFlow<List<MapLevel>> = _mapLevels.asStateFlow()

    private val _shopItems = MutableStateFlow<List<ShopItem>>(emptyList())
    val shopItems: StateFlow<List<ShopItem>> = _shopItems.asStateFlow()

    private val _wheelSegments = MutableStateFlow<List<WheelSegment>>(emptyList())
    val wheelSegments: StateFlow<List<WheelSegment>> = _wheelSegments.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _wheelResult = MutableStateFlow<Reward?>(null)
    val wheelResult: StateFlow<Reward?> = _wheelResult.asStateFlow()

    private val _showDailyLogin = MutableStateFlow(false)
    val showDailyLogin: StateFlow<Boolean> = _showDailyLogin.asStateFlow()

    private val _showLevelUp = MutableStateFlow(false)
    val showLevelUp: StateFlow<Boolean> = _showLevelUp.asStateFlow()

    private val _newLevel = MutableStateFlow(1)
    val newLevel: StateFlow<Int> = _newLevel.asStateFlow()

    init {
        val dataStore = PlayerDataStore(application)
        repository = PlayerRepositoryImpl(dataStore)
        observePlayer()
        initializeGame()
    }

    private fun observePlayer() {
        viewModelScope.launch {
            repository.playerFlow.collect { player ->
                _player.value = player
            }
        }
    }

    private fun initializeGame() {
        viewModelScope.launch {
            val player = repository.playerFlow.first()
            _shopItems.value = GameConfig.SHOP_ITEMS
            _wheelSegments.value = GameConfig.WHEEL_SEGMENTS
            _mapLevels.value = getMapLevelsForPlayer(player)
            _leaderboard.value = generateMockLeaderboard(player)
            checkDailyLogin(player)
        }
    }

    fun updatePlayerName(name: String) {
        viewModelScope.launch {
            val current = _player.value
            repository.updatePlayer(current.copy(name = name))
        }
    }

    fun selectAnimal(animalId: String) {
        viewModelScope.launch {
            repository.selectAnimal(animalId)
        }
    }

    fun unlockAnimal(animalId: String) {
        viewModelScope.launch {
            val animal = AnimalData.getAnimalById(animalId) ?: return@launch
            val current = _player.value
            if (current.coins >= animal.unlockCost) {
                repository.unlockAnimal(animalId, animal.unlockCost)
            }
        }
    }

    fun upgradeAnimal(animalId: String) {
        viewModelScope.launch {
            val animal = AnimalData.getAnimalById(animalId) ?: return@launch
            val current = _player.value
            val currentLevel = current.animalUpgrades[animalId] ?: 0
            val cost = animal.upgradeCost * (currentLevel + 1)
            if (current.coins >= cost) {
                repository.upgradeAnimal(animalId, cost)
            }
        }
    }

    fun buyShopItem(item: ShopItem) {
        viewModelScope.launch {
            val current = _player.value
            if (current.coins >= item.price) {
                repository.updatePlayer(current.copy(coins = current.coins - item.price))
                when (item.category) {
                    com.animalbattle.game.domain.model.ShopCategory.COIN_PACK -> {
                        val bonus = when (item.id) {
                            "coin_pack_small" -> 50
                            "coin_pack_medium" -> 200
                            "coin_pack_large" -> 500
                            else -> 50
                        }
                        repository.addCoins(bonus)
                    }
                    else -> { /* Handle other categories */ }
                }
            }
        }
    }

    fun spinWheel() {
        viewModelScope.launch {
            val current = _player.value
            if (current.coins < GameConfig.LUCKY_WHEEL_SPIN_COST) return@launch
            if (current.luckyWheelSpinsToday >= GameConfig.MAX_DAILY_SPINS) return@launch

            repository.updatePlayer(
                current.copy(
                    coins = current.coins - GameConfig.LUCKY_WHEEL_SPIN_COST,
                    luckyWheelSpinsToday = current.luckyWheelSpinsToday + 1,
                    lastSpinDate = System.currentTimeMillis()
                )
            )

            _isSpinning.value = true
            delay(3000)
            _isSpinning.value = false

            val reward = getRandomWheelReward()
            _wheelResult.value = reward
            applyReward(reward)
        }
    }

    private fun getRandomWheelReward(): Reward {
        val random = Random.nextFloat()
        var cumulative = 0f
        for (segment in GameConfig.WHEEL_SEGMENTS) {
            cumulative += segment.probability
            if (random <= cumulative) {
                return segment.reward
            }
        }
        return GameConfig.WHEEL_SEGMENTS.first().reward
    }

    private fun applyReward(reward: Reward) {
        viewModelScope.launch {
            when (reward.type) {
                RewardType.COINS -> repository.addCoins(reward.amount)
                RewardType.TROPHIES -> repository.addTrophies(reward.amount)
                RewardType.XP -> {
                    val current = _player.value
                    repository.updatePlayer(current.copy(xp = current.xp + reward.amount))
                }
                else -> { /* Handle other reward types */ }
            }
        }
    }

    fun clearWheelResult() {
        _wheelResult.value = null
    }

    // Battle Functions
    fun startBattle() {
        viewModelScope.launch {
            val current = _player.value
            val playerAnimal = AnimalData.getAnimalById(current.selectedAnimalId)
                ?: AnimalData.getAllAnimals().first()
            val opponentAnimal = AnimalData.getAllAnimals().random()

            val playerLevel = current.animalUpgrades[current.selectedAnimalId] ?: 1
            val opponentLevel = Random.nextInt(1, 5)

            _battleState.value = BattleState(
                playerAnimal = playerAnimal,
                opponentAnimal = opponentAnimal,
                playerHp = playerAnimal.baseHp + (playerLevel * 10),
                playerMaxHp = playerAnimal.baseHp + (playerLevel * 10),
                playerPower = GameConfig.INITIAL_PLAYER_POWER,
                playerXp = 0,
                playerLevel = playerLevel,
                opponentHp = opponentAnimal.baseHp + (opponentLevel * 10),
                opponentMaxHp = opponentAnimal.baseHp + (opponentLevel * 10),
                opponentPower = GameConfig.INITIAL_OPPONENT_POWER,
                opponentXp = 0,
                opponentLevel = opponentLevel,
                isPlayerTurn = true,
                battlePhase = BattlePhase.PLAYER_TURN
            )
        }
    }

    fun increasePower() {
        val current = _battleState.value ?: return
        if (!current.isPlayerTurn) return

        val question = QuestionData.getRandomQuestion()
        _battleState.value = current.copy(
            battlePhase = BattlePhase.QUESTION,
            currentQuestion = question
        )
    }

    fun answerQuestion(selectedIndex: Int) {
        val current = _battleState.value ?: return
        val question = current.currentQuestion ?: return

        if (selectedIndex == question.correctOptionIndex) {
            _battleState.value = current.copy(
                playerPower = current.playerPower + GameConfig.CORRECT_ANSWER_POWER_BONUS,
                battlePhase = BattlePhase.PLAYER_TURN,
                currentQuestion = null
            )
        } else {
            endPlayerTurn()
        }
    }

    fun timeUp() {
        endPlayerTurn()
    }

    fun useFiftyFifty() {
        val current = _battleState.value ?: return
        if (current.playerUsedFiftyFifty) return

        val current2 = _player.value
        if (current2.coins < GameConfig.FIFTY_FIFTY_COST) return

        viewModelScope.launch {
            repository.removeCoins(GameConfig.FIFTY_FIFTY_COST)
        }

        _battleState.value = current.copy(playerUsedFiftyFifty = true)
    }

    fun useSkip() {
        val current = _battleState.value ?: return
        if (current.playerUsedSkip) return

        val current2 = _player.value
        if (current2.coins < GameConfig.SKIP_COST) return

        viewModelScope.launch {
            repository.removeCoins(GameConfig.SKIP_COST)
        }

        _battleState.value = current.copy(
            playerUsedSkip = true,
            battlePhase = BattlePhase.PLAYER_TURN,
            currentQuestion = null
        )
    }

    fun attack() {
        val current = _battleState.value ?: return
        if (!current.isPlayerTurn || current.playerPower < GameConfig.POWER_ATTACK_COST) return

        val newOpponentHp = (current.opponentHp - current.playerPower).coerceAtLeast(0)
        val newPlayerPower = current.playerPower - GameConfig.POWER_ATTACK_COST

        _battleState.value = current.copy(
            opponentHp = newOpponentHp,
            playerPower = newPlayerPower,
            battlePhase = BattlePhase.ATTACKING
        )

        if (newOpponentHp <= 0) {
            endBattle(true)
        } else {
            viewModelScope.launch {
                delay(800)
                endPlayerTurn()
            }
        }
    }

    fun useAbility(abilityIndex: Int) {
        val current = _battleState.value ?: return
        if (!current.isPlayerTurn) return

        val ability = current.playerAnimal.abilities.getOrNull(abilityIndex) ?: return
        if (current.playerXp < ability.xpCost) return

        val newOpponentHp = (current.opponentHp - ability.damage).coerceAtLeast(0)
        val newXp = current.playerXp - ability.xpCost

        _battleState.value = current.copy(
            opponentHp = newOpponentHp,
            playerXp = newXp,
            battlePhase = BattlePhase.USING_ABILITY
        )

        if (newOpponentHp <= 0) {
            endBattle(true)
        } else {
            viewModelScope.launch {
                delay(800)
                endPlayerTurn()
            }
        }
    }

    private fun endPlayerTurn() {
        val current = _battleState.value ?: return
        _battleState.value = current.copy(
            isPlayerTurn = false,
            battlePhase = BattlePhase.OPPONENT_TURN,
            currentQuestion = null
        )
        viewModelScope.launch {
            delay(1500)
            opponentTurn()
        }
    }

    private fun opponentTurn() {
        val current = _battleState.value ?: return

        val newPlayerHp = (current.playerHp - current.opponentPower).coerceAtLeast(0)
        val newOpponentPower = current.opponentPower + 1
        val newXp = current.playerXp + 1

        _battleState.value = current.copy(
            playerHp = newPlayerHp,
            opponentPower = newOpponentPower,
            playerXp = newXp
        )

        if (newPlayerHp <= 0) {
            endBattle(false)
        } else {
            viewModelScope.launch {
                delay(800)
                _battleState.value = _battleState.value?.copy(
                    isPlayerTurn = true,
                    battlePhase = BattlePhase.PLAYER_TURN
                )
            }
        }
    }

    private fun endBattle(playerWon: Boolean) {
        viewModelScope.launch {
            val current = _player.value
            val battle = _battleState.value ?: return@launch

            if (playerWon) {
                repository.addCoins(GameConfig.BATTLE_WIN_COINS)
                repository.addTrophies(GameConfig.BATTLE_WIN_TROPHIES)

                val previousLevel = current.calculateLevel()
                val updatedPlayer = repository.playerFlow.first()
                val newLevel = updatedPlayer.calculateLevel()

                if (newLevel > previousLevel) {
                    _newLevel.value = newLevel
                    _showLevelUp.value = true
                }
            }

            val record = BattleRecord(
                id = System.currentTimeMillis().toString(),
                opponentName = GameConfig.OPPONENT_NAMES.random(),
                opponentAnimalId = battle.opponentAnimal.id,
                playerAnimalId = battle.playerAnimal.id,
                won = playerWon,
                rewardCoins = if (playerWon) GameConfig.BATTLE_WIN_COINS else 0,
                rewardTrophies = if (playerWon) GameConfig.BATTLE_WIN_TROPHIES else 0,
                timestamp = System.currentTimeMillis()
            )
            repository.addBattleRecord(record)

            _battleState.value = battle.copy(
                battleResult = if (playerWon) BattleResult.VICTORY else BattleResult.DEFEAT,
                battlePhase = BattlePhase.BATTLE_OVER
            )
        }
    }

    fun endBattleAndReturn() {
        _battleState.value = null
    }

    // Daily Login
    private fun checkDailyLogin(player: Player) {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val daysSinceLastLogin = (now - player.lastLoginDate) / dayMs

        if (daysSinceLastLogin >= 1) {
            val newStreak = if (daysSinceLastLogin <= 1) {
                (player.dailyLoginStreak % 7) + 1
            } else {
                1
            }

            _dailyLoginRewards.value = GameConfig.DAILY_LOGIN_REWARDS.mapIndexed { index, reward ->
                DailyLoginReward(
                    day = index + 1,
                    reward = reward,
                    isClaimed = index < (newStreak - 1)
                )
            }

            if (newStreak > player.dailyLoginStreak || daysSinceLastLogin > 1) {
                _showDailyLogin.value = true
                viewModelScope.launch {
                    repository.updatePlayer(
                        player.copy(
                            dailyLoginStreak = newStreak,
                            lastLoginDate = now
                        )
                    )
                }
            }
        } else {
            _dailyLoginRewards.value = GameConfig.DAILY_LOGIN_REWARDS.mapIndexed { index, reward ->
                DailyLoginReward(
                    day = index + 1,
                    reward = reward,
                    isClaimed = index < player.dailyLoginStreak
                )
            }
        }
    }

    fun claimDailyReward(day: Int) {
        viewModelScope.launch {
            val current = _player.value
            val reward = GameConfig.DAILY_LOGIN_REWARDS.getOrNull(day - 1) ?: return@launch
            if (day <= current.dailyLoginStreak) return@launch

            applyReward(reward)
            repository.updatePlayer(
                current.copy(
                    dailyLoginStreak = day,
                    lastLoginDate = System.currentTimeMillis()
                )
            )

            _dailyLoginRewards.value = _dailyLoginRewards.value.map {
                if (it.day == day) it.copy(isClaimed = true) else it
            }
        }
    }

    fun dismissDailyLogin() {
        _showDailyLogin.value = false
    }

    fun dismissLevelUp() {
        _showLevelUp.value = false
    }

    // Map
    private fun getMapLevelsForPlayer(player: Player): List<MapLevel> {
        return GameConfig.getMapLevels().map { level ->
            when {
                level.level in player.completedLevels -> level.copy(status = LevelStatus.COMPLETED)
                level.level <= player.currentMapLevel -> level.copy(status = LevelStatus.AVAILABLE)
                else -> level
            }
        }
    }

    fun completeMapLevel(level: Int) {
        viewModelScope.launch {
            repository.completeMapLevel(level)
            val updatedPlayer = repository.playerFlow.first()
            _mapLevels.value = getMapLevelsForPlayer(updatedPlayer)
        }
    }

    // Settings
    fun updateSettings(settings: GameSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    // Leaderboard
    private fun generateMockLeaderboard(player: Player): List<LeaderboardEntry> {
        val entries = mutableListOf<LeaderboardEntry>()
        val names = listOf("Shadow", "Blaze", "Storm", "Thunder", "Frost", "Phoenix", "Dragon", "Titan", "Viper", "Spike")

        val avatars = listOf("🦁", "🐯", "🦅", "🐺", "🐻", "🐍", "🐘", "🦏", "🐆", "🐊")

        names.forEachIndexed { index, name ->
            entries.add(
                LeaderboardEntry(
                    rank = index + 1,
                    playerName = name,
                    trophies = Random.nextInt(50, 200),
                    avatarEmoji = avatars.getOrElse(index) { "👤" }
                )
            )
        }

        entries.add(
            LeaderboardEntry(
                rank = entries.size + 1,
                playerName = player.name,
                trophies = player.trophies,
                avatarEmoji = "⭐",
                isPlayer = true
            )
        )

        return entries.sortedByDescending { it.trophies }.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
    }
}
