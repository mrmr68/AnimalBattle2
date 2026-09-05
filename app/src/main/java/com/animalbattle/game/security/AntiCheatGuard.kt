package com.animalbattle.game.security

import com.animalbattle.game.gameplay.MatchConfig
import com.animalbattle.game.gameplay.MatchResult
import com.animalbattle.game.gameplay.MatchSnapshot

/**
 * Client-side anti-cheat guard.
 *
 * The fundamental rule: rewards are only awarded server-side. This guard
 * validates that the client's own inputs are plausible (timing, movement
 * caps) and flags attempts to claim forged results.
 *
 * The guard is NOT the source of truth in online mode — the server is.
 */
object AntiCheatGuard {

    data class Violation(
        val code: String,
        val message: String,
        val severity: Severity
    )

    enum class Severity {
        INFO,       // log only
        WARNING,    // ignore result for this frame
        BLOCK       // reject the input / mark suspicion
    }

    private const val MAX_MOVEMENT_PER_TICK = 0.15f          // progress units
    private const val MIN_MOVEMENT_INTERVAL_MS = 300L        // human floor
    private const val MAX_ANSWER_SPEED_MS = 800L             // below this is sus
    private const val MAX_RESPONSE_SECONDS = 20

    private val violations = mutableListOf<Violation>()

    fun validateMovement(
        previousProgress: Float,
        newProgress: Float,
        elapsedMs: Long,
        claimedMovementType: Int
    ): List<Violation> {
        val found = mutableListOf<Violation>()

        val gained = newProgress - previousProgress
        if (gained > MAX_MOVEMENT_PER_TICK) {
            found += Violation(
                "MOVEMENT_SPEED_HACK",
                "Movement gain ${"%.3f".format(gained)} exceeds cap",
                Severity.BLOCK
            )
        }
        if (elapsedMs in 1 until MIN_MOVEMENT_INTERVAL_MS) {
            found += Violation(
                "MOVEMENT_RATE_HACK",
                "Movement interval ${elapsedMs}ms below human floor",
                Severity.WARNING
            )
        }
        if (claimedMovementType !in 1..3) {
            found += Violation(
                "INVALID_MOVEMENT",
                "Movement type $claimedMovementType out of range",
                Severity.BLOCK
            )
        }
        return found.also { violations.addAll(it) }
    }

    fun validateAnswerSubmission(
        questionSentAtMs: Long,
        submittedAtMs: Long
    ): List<Violation> {
        val found = mutableListOf<Violation>()
        val elapsed = submittedAtMs - questionSentAtMs
        if (elapsed in 1 until MAX_ANSWER_SPEED_MS) {
            found += Violation(
                "INSTANT_ANSWER",
                "Answer in ${elapsed}ms is below human floor",
                Severity.WARNING
            )
        }
        return found.also { violations.addAll(it) }
    }

    fun validateTimerSnapshot(timer: com.animalbattle.game.gameplay.TimerState): List<Violation> {
        val found = mutableListOf<Violation>()
        if (timer.timeRemainingMs > timer.roundTimeMs + 5_000) {
            found += Violation(
                "TIMER_TAMPERING",
                "Remaining time exceeds round duration",
                Severity.BLOCK
            )
        }
        return found.also { violations.addAll(it) }
    }

    /**
     * Validate a claimed match result before sending it to the server.
     * This prevents the client from inventing wins/rewards.
     */
    fun validateClaimedResult(
        matchId: String,
        claimedWinnerId: String,
        localPlayerId: String,
        snapshot: MatchSnapshot
    ): List<Violation> {
        val found = mutableListOf<Violation>()

        // The local player may not claim victory if the core hasn't finished
        if (claimedWinnerId == localPlayerId && !snapshot.race.finished) {
            found += Violation(
                "FORGED_VICTORY",
                "Victory claimed before race finished",
                Severity.BLOCK
            )
        }
        // Winner must be either the local player or the opponent
        if (claimedWinnerId != localPlayerId && claimedWinnerId != snapshot.opponent.playerId) {
            found += Violation(
                "FORGED_WINNER",
                "Winner id $claimedWinnerId is not a participant",
                Severity.BLOCK
            )
        }
        return found.also { violations.addAll(it) }
    }

    fun validateRewardsRequest(
        matchId: String,
        requestedRewards: MatchRewardsClaim
    ): List<Violation> {
        val found = mutableListOf<Violation>()
        if (requestedRewards.winnerCoins > 1000 || requestedRewards.winnerTrophies > 100) {
            found += Violation(
                "REWARD_FORGERY",
                "Reward amounts out of server bounds",
                Severity.BLOCK
            )
        }
        return found.also { violations.addAll(it) }
    }

    fun hasBlockingViolations(checks: List<List<Violation>>): Boolean =
        checks.flatten().any { it.severity == Severity.BLOCK }

    fun drainViolations(): List<Violation> = violations.toList().also { violations.clear() }

    /** Convenience: combine multiple validations. */
    fun validateAll(blocks: List<() -> List<Violation>>): List<Violation> =
        blocks.flatMap { it() }
}

/** Client-proposed rewards — must be confirmed by the server. */
data class MatchRewardsClaim(
    val winnerCoins: Int,
    val winnerXp: Int,
    val winnerTrophies: Int,
    val loserCoins: Int,
    val loserXp: Int,
    val loserTrophies: Int
)

/**
 * Generation nonce — ties a match to a server-issued token so replays
 * of old results are rejected. Intended for the real backend.
 */
data class MatchNonce(
    val matchId: String,
    val issuedAtMs: Long,
    val expiresAtMs: Long,
    val signature: String = ""
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAtMs
    val isValidFormat: Boolean get() = matchId.isNotBlank() && signature.isNotBlank()
}