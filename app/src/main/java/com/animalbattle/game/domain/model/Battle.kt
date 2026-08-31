package com.animalbattle.game.domain.model

data class BattleState(
    val playerAnimal: Animal,
    val opponentAnimal: Animal,
    val playerHp: Int,
    val playerMaxHp: Int,
    val playerPower: Int,
    val playerXp: Int,
    val playerLevel: Int,
    val opponentHp: Int,
    val opponentMaxHp: Int,
    val opponentPower: Int,
    val opponentXp: Int,
    val opponentLevel: Int,
    val isPlayerTurn: Boolean = true,
    val battlePhase: BattlePhase = BattlePhase.STARTING,
    val currentQuestion: Question? = null,
    val timerSeconds: Int = 15,
    val playerUsedFiftyFifty: Boolean = false,
    val playerUsedSkip: Boolean = false,
    val selectedAbilityIndex: Int? = null,
    val battleResult: BattleResult? = null
) {
    companion object {
        const val INITIAL_PLAYER_POWER = 3
        const val INITIAL_OPPONENT_POWER = 4
        const val CORRECT_ANSWER_POWER_BONUS = 2
        const val POWER_ATTACK_COST = 1
        const val QUESTION_TIME_SECONDS = 15
    }
}

enum class BattlePhase {
    STARTING,
    PLAYER_TURN,
    OPPONENT_TURN,
    QUESTION,
    ATTACKING,
    USING_ABILITY,
    BATTLE_OVER
}

enum class BattleResult {
    VICTORY,
    DEFEAT
}
