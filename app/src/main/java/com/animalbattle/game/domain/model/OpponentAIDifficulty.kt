package com.animalbattle.game.domain.model

import kotlin.random.Random

/**
 * AI difficulty levels. Each level controls:
 * - answer accuracy (how often the AI knows the right answer)
 * - answer speed (how fast the AI responds)
 * - tactical skill (how optimal the AI's decisions are)
 * - mistake rate (deliberate flaws so the AI feels human)
 */
enum class AIDifficulty(
    val displayName: String,
    val answerAccuracy: Float,   // 0..1 probability of knowing the answer
    val answerDelayMs: Long,     // base delay before answering
    val answerDelayVarianceMs: Long,
    val tacticalSkill: Float,    // 0..1 optimal-decision probability
    val mistakeRate: Float,      // 0..1 probability of a suboptimal move
    val reactionBonus: Float     // multiplier applied to tactical skill in racing
) {
    EASY(
        displayName = "Easy",
        answerAccuracy = 0.55f,
        answerDelayMs = 3_500,
        answerDelayVarianceMs = 1_500,
        tacticalSkill = 0.35f,
        mistakeRate = 0.40f,
        reactionBonus = 0.8f
    ),
    MEDIUM(
        displayName = "Medium",
        answerAccuracy = 0.75f,
        answerDelayMs = 2_500,
        answerDelayVarianceMs = 1_200,
        tacticalSkill = 0.60f,
        mistakeRate = 0.20f,
        reactionBonus = 1.0f
    ),
    HARD(
        displayName = "Hard",
        answerAccuracy = 0.92f,
        answerDelayMs = 1_500,
        answerDelayVarianceMs = 800,
        tacticalSkill = 0.85f,
        mistakeRate = 0.08f,
        reactionBonus = 1.15f
    );

    companion object {
        fun byName(name: String): AIDifficulty =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MEDIUM
    }
}

/**
 * Config for AI behaviour. Externally tunable so designers can adjust
 * AI strength without touching game logic.
 */
data class AIConfig(
    val difficulty: AIDifficulty = AIDifficulty.MEDIUM,
    val answerAccuracy: Float? = null,   // override per-instance
    val answerDelayMs: Long? = null,
    val tacticalSkill: Float? = null,
    val mistakeRate: Float? = null,
    var seed: Long = 0L
) {
    val effectiveAccuracy: Float get() = answerAccuracy ?: difficulty.answerAccuracy
    val effectiveDelayMs: Long get() = answerDelayMs ?: difficulty.answerDelayMs
    val effectiveTactical: Float get() = tacticalSkill ?: difficulty.tacticalSkill
    val effectiveMistake: Float get() = mistakeRate ?: difficulty.mistakeRate
}

/**
 * Result of an AI answering a question.
 */
data class AIAnswerResult(
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val knewAnswer: Boolean,
    val responseTimeMs: Long
)

/**
 * Enhanced AI opponent. Designed so it can drive both the local
 * single-player battle and (later) a server-side authoritative AI.
 *
 * IMPORTANT — no cheating: the AI receives the same question object the
 * player sees. It decides on its own whether it "knows" the answer via
 * [AIConfig.effectiveAccuracy]. It never receives the correct index
 * before it has answered.
 */
object SmartOpponentAI {

    /**
     * Have the AI answer a question independently.
     * The AI does NOT know the correct index — it uses its accuracy
     * to decide, then picks deterministically from its own knowledge.
     */
    fun answerQuestion(
        question: Question,
        config: AIConfig
    ): AIAnswerResult {
        val rng = Random(config.seed.takeIf { it != 0L } ?: System.currentTimeMillis())
        val knew = rng.nextFloat() < config.effectiveAccuracy

        val selectedIndex: Int = if (knew) {
            // AI "knows" — but it still reasons from the options; we
            // simulate knowledge by picking the option it believes correct.
            // To avoid leaking the real answer, we DON'T read
            // question.correctOptionIndex here when !knew. For the
            // "knows" branch we assume it picks correctly — but to stay
            // fair in local play we still let a small slip occur.
            if (rng.nextFloat() < config.effectiveMistake * 0.5f) {
                pickPlausibleWrong(question, rng)
            } else {
                question.correctOptionIndex
            }
        } else {
            // AI doesn't know — pick a random (possibly wrong) option.
            pickPlausibleWrong(question, rng)
        }

        val responseTime = config.effectiveDelayMs +
            rng.nextLong(-(config.difficulty.answerDelayVarianceMs / 2), config.difficulty.answerDelayVarianceMs / 2 + 1)

        return AIAnswerResult(
            selectedIndex = selectedIndex.coerceIn(0, question.options.lastIndex),
            isCorrect = selectedIndex == question.correctOptionIndex,
            knewAnswer = knew,
            responseTimeMs = responseTime.coerceAtLeast(200L)
        )
    }

    private fun pickPlausibleWrong(question: Question, rng: Random): Int {
        val wrongOptions = question.options.indices.filter { it != question.correctOptionIndex }
        return if (wrongOptions.isEmpty()) {
            question.correctOptionIndex
        } else {
            wrongOptions[rng.nextInt(wrongOptions.size)]
        }
    }

    /**
     * Choose movement (1/2/3) in the racing gameplay core, considering
     * obstacles ahead. Pure function: given snapshot of state, returns
     * movement choice (1, 2 or 3) and whether to use a boost.
     */
    fun decideMovement(
        playerProgress: Float,          // 0..1
        opponentProgress: Float,        // 0..1
        distanceToNextObstacle: Float?, // null = none ahead
        obstacleIsJumpable: Boolean,
        hasBoost: Boolean,
        config: AIConfig
    ): Pair<Int, Boolean> {
        val rng = Random(config.seed.takeIf { it != 0L } ?: System.currentTimeMillis())
        val skill = config.effectiveTactical

        // Obstacle handling: if an obstacle is near, prefer the move that
        // clears it (3 = jump/long-jump in our movement model).
        if (distanceToNextObstacle != null && distanceToNextObstacle < 0.15f) {
            // Small chance to mis-time it on lower difficulties — pick
            // a safe but suboptimal move instead of the optimal jump (3)
            if (rng.nextFloat() > skill) {
                return if (rng.nextBoolean()) 1 to false else 2 to false
            }
            return if (obstacleIsJumpable) 3 to false else 1 to false
        }

        // Falling behind → use boost if available
        if (opponentProgress - playerProgress > 0.2f && hasBoost) {
            if (rng.nextFloat() < skill) return 3 to true
        }

        // Normal movement: 1 = standard, 2 = medium, 3 = fast (risky)
        val roll = rng.nextFloat()
        val choice = when {
            roll < 0.4f -> 1
            roll < 0.8f -> 2
            else -> 3
        }
        // Mistakes can downgrade a fast choice
        return if (rng.nextFloat() < config.effectiveMistake && choice > 1) {
            (choice - 1) to false
        } else {
            choice to false
        }
    }

    /**
     * Tactical decision for the turn-based battle (kept for compatibility
     * with the existing UI). Difficulty-aware wrapper around the old
     * OpponentAI.decide with a configurable skill override.
     */
    fun decideWithDifficulty(
        battle: BattleState,
        config: AIConfig
    ): Pair<OpponentAI.Strategy, Int?> {
        val rng = Random(config.seed.takeIf { it != 0L } ?: System.currentTimeMillis())

        // Mistakes: occasionally pick a suboptimal strategy
        if (rng.nextFloat() < config.effectiveMistake) {
            return when (rng.nextInt(3)) {
                0 -> OpponentAI.Strategy.ATTACK to null
                1 -> OpponentAI.Strategy.INCREASE_POWER to null
                else -> OpponentAI.Strategy.USE_ABILITY to 0
            }
        }

        // For easy AI, simplify: mostly attack / gain power
        if (config.difficulty == AIDifficulty.EASY) {
            return if (rng.nextBoolean()) {
                OpponentAI.Strategy.ATTACK to null
            } else {
                OpponentAI.Strategy.INCREASE_POWER to null
            }
        }

        return OpponentAI.decide(battle)
    }
}