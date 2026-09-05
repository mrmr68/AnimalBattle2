package com.animalbattle.game.gameplay

import kotlin.random.Random

// ═══════════════════════════════════════════════════════════
//  Match Lifecycle
// ═══════════════════════════════════════════════════════════

enum class MatchPhase {
    IDLE,
    SEARCHING,      // matchmaking in progress
    FOUND,          // opponent found, awaiting ready
    CONNECTING,     // establishing network session
    SYNCHRONIZING,  // exchanging initial state
    STARTING,       // countdown
    RUNNING,        // race/battle in progress
    PAUSED,         // player paused
    DISCONNECTED,   // network dropped
    RECONNECTING,   // attempting to rejoin
    FINISHED,       // result ready
    CANCELLED       // match terminated
}

// ═══════════════════════════════════════════════════════════
//  Player Identity
// ═══════════════════════════════════════════════════════════

data class PlayerIdentity(
    val playerId: String,          // stable ID (device ID or account ID)
    val displayName: String,
    val avatarEmoji: String = "🦁",
    val level: Int = 1,
    val playerType: PlayerType = PlayerType.LOCAL
)

enum class PlayerType {
    LOCAL,      // the human on this device
    AI,         // simulated opponent
    REMOTE      // real human on another device
}

// ═══════════════════════════════════════════════════════════
//  Modular Game State Snapshot
// ═══════════════════════════════════════════════════════════

/** Player state within a match. */
data class RacePlayerState(
    val playerId: String,
    val animalId: String,
    val progress: Float = 0f,        // 0..1 along the track
    val position: Float = 0f,        // x coordinate in world units
    val lane: Int = 0,
    val speed: Float = 0f,
    val isBoosting: Boolean = false,
    val isJumping: Boolean = false,
    val isStunned: Boolean = false,
    val checkpointsPassed: Int = 0,
    val hp: Int = 100,
    val maxHp: Int = 100
)

/** Race state — common to local, AI and online games. */
data class RaceState(
    val trackLength: Float = 1000f,
    val progressPlayer: Float = 0f,
    val progressOpponent: Float = 0f,
    val finished: Boolean = false,
    val winnerId: String? = null,
    val finishSnapshotVersion: Int = 0
)

/** Question state — one question per round. */
data class QuestionRoundState(
    val roundIndex: Int = 0,
    val questionId: String? = null,
    val question: QuestionSnapshot? = null,
    val playerAnswered: Boolean = false,
    val playerAnswerIndex: Int? = null,
    val opponentAnswered: Boolean = false,
    val opponentAnswerIndex: Int? = null,
    val isPerfect: Boolean = false,
    val roundResult: RoundResult? = null,
    val startedAtMs: Long = 0L
) {
    val bothAnswered: Boolean get() = playerAnswered && opponentAnswered
    val isComplete: Boolean get() = roundResult != null
}

/** Lightweight serializable question (no answer leakage to network). */
data class QuestionSnapshot(
    val id: String,
    val questionText: String,
    val options: List<String>
    // NOTE: correctOptionIndex is intentionally NOT included.
    // The server/authoritative layer is responsible for judging answers.
)

enum class RoundResult {
    PLAYER_WIN,
    OPPONENT_WIN,
    TIE,
    PLAYER_TIMEOUT,
    OPPONENT_TIMEOUT
}

/** Movement state — tracks the three movement types. */
data class MovementState(
    val movement1Used: Int = 0,
    val movement2Used: Int = 0,
    val movement3Used: Int = 0,
    val lastMovementType: Int = 0,   // 1, 2 or 3
    val lastMovementAtMs: Long = 0L
)

/** Timer state. */
data class TimerState(
    val roundTimeMs: Long = 15_000,
    val timeRemainingMs: Long = 15_000,
    val isRunning: Boolean = false,
    val lastTickAtMs: Long = 0L
) {
    val progress: Float get() = (timeRemainingMs.toFloat() / roundTimeMs).coerceIn(0f, 1f)
    val isExpired: Boolean get() = timeRemainingMs <= 0
}

/** Obstacle state — one obstacle per segment. */
data class ObstacleState(
    val id: String,
    val position: Float,             // track position 0..trackLength
    val lane: Int,
    val type: ObstacleType,
    val isCleared: Boolean = false
) {
    val isJumpable: Boolean get() = type == ObstacleType.HURDLE || type == ObstacleType.DITCH
}

enum class ObstacleType {
    HURDLE,     // small — jump with movement 3
    DITCH,      // gap — jump with movement 3
    ROCK,       // requires slowing (movement 1) or timing
    SLOW_ZONE,  // reduces speed, unavoidable
    TRAP        // damages HP
}

// ═══════════════════════════════════════════════════════════
//  Match Result
// ═══════════════════════════════════════════════════════════

data class MatchResult(
    val matchId: String,
    val winnerId: String?,
    val loserId: String?,
    val isDraw: Boolean = false,
    val reason: MatchEndReason = MatchEndReason.FINISH,
    val rewards: MatchRewards = MatchRewards(),
    val stats: MatchStatistics = MatchStatistics(),
    val endedAtMs: Long = System.currentTimeMillis()
)

enum class MatchEndReason {
    FINISH,         // crossed the finish line
    DISCONNECT,     // opponent left
    TIMEOUT,        // match timed out
    FORFEIT,        // player abandoned
    ERROR
}

data class MatchRewards(
    val winnerCoins: Int = 0,
    val winnerXp: Int = 0,
    val winnerTrophies: Int = 0,
    val loserCoins: Int = 0,
    val loserXp: Int = 0,
    val loserTrophies: Int = 0
)

data class MatchStatistics(
    val playerPerfectAnswers: Int = 0,
    val playerCombos: Int = 0,
    val playerMaxCombo: Int = 0,
    val opponentPerfectAnswers: Int = 0,
    val playerFinishTimeMs: Long = 0L,
    val opponentFinishTimeMs: Long = 0L
)

// ═══════════════════════════════════════════════════════════
//  Match Definition
// ═══════════════════════════════════════════════════════════

data class MatchConfig(
    val matchId: String = generateMatchId(),
    val trackLength: Float = 1000f,
    val roundTimeMs: Long = 15_000,
    val roundsToWin: Int = 3,
    val obstacleCount: Int = 5,
    val allowBoosts: Boolean = true,
    val allowCombos: Boolean = true,
    private val _seed: Long = Random.nextLong()
) {
    val seed: Long get() = _seed

    companion object {
        fun generateMatchId(): String {
            val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        }
    }
}

/** Complete match snapshot transmitted over the wire. */
data class MatchSnapshot(
    val matchId: String,
    val phase: MatchPhase,
    val version: Int,
    val race: RaceState,
    val player: RacePlayerState,
    val opponent: RacePlayerState,
    val questionRound: QuestionRoundState?,
    val timer: TimerState,
    val movement: MovementState,
    val obstacles: List<ObstacleState>,
    val sentAtMs: Long
)