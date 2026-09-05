package com.animalbattle.game.gameplay

import com.animalbattle.game.domain.model.Question
import kotlin.math.abs
import kotlin.random.Random

/**
 * Core gameplay engine. Pure and deterministic where possible.
 *
 * One gameplay system shared by:
 *  - Local player (human on this device)
 *  - AI opponent (SmartOpponentAI drives it)
 *  - Online player (network inputs produce the same state transitions)
 *
 * The core NEVER talks to the backend directly. It consumes/emits
 * [MatchSnapshot] values so a network layer can be swapped in cleanly.
 *
 * ══ Security note ══
 * In local/AI mode the caller supplies correctness of the answer
 * ([playerSubmitAnswer]). In online mode, correctness is judged by the
 * server and delivered via [judgeRound], so the client can never claim
 * rewards it didn't earn.
 */
class GameplayCore(
    val config: MatchConfig,
    val localPlayerId: String
) {

    // ── Mutable state (single source of truth for an active match) ──
    var phase: MatchPhase = MatchPhase.IDLE
        private set

    var race = RaceState(trackLength = config.trackLength)
        private set

    var player = RacePlayerState(playerId = localPlayerId)
        private set

    var opponent = RacePlayerState(playerId = "opponent")
        private set

    /** The question currently on screen (mirror of the round). */
    var currentQuestion: Question? = null
        private set

    var questionRound: QuestionRoundState? = null
        private set

    var timer = TimerState(roundTimeMs = config.roundTimeMs)
        private set

    var movement = MovementState()
        private set

    var obstacles: List<ObstacleState> = emptyList()
        private set

    var stats = MatchStatistics()
        private set

    private var version = 0
    private val rng = Random(config.seed)

    // ── Lifecycle ──────────────────────────────────────────

    fun startMatch(opponentId: String, opponentAnimalId: String, opponentName: String = "Rival") {
        phase = MatchPhase.STARTING
        version++
        opponent = opponent.copy(playerId = opponentId, animalId = opponentAnimalId)
        obstacles = generateObstacles()
        race = RaceState(trackLength = config.trackLength)
        player = player.copy(progress = 0f, position = 0f, hp = 100)
        opponent = opponent.copy(progress = 0f, position = 0f, hp = 100)
    }

    fun beginRace() {
        phase = MatchPhase.RUNNING
        version++
        beginRound(roundIndex = 0)
    }

    private fun generateObstacles(): List<ObstacleState> {
        return (0 until config.obstacleCount).map { i ->
            val position = config.trackLength * (0.18f + 0.15f * i) + rng.nextFloat() * 30f
            ObstacleState(
                id = "obs_$i",
                position = position,
                lane = rng.nextInt(3),
                type = ObstacleType.entries[rng.nextInt(ObstacleType.entries.size)]
            )
        }
    }

    // ── Rounds / Questions ─────────────────────────────────

    private fun beginRound(roundIndex: Int) {
        version++
        questionRound = QuestionRoundState(
            roundIndex = roundIndex,
            startedAtMs = System.currentTimeMillis()
        )
        timer = TimerState(
            roundTimeMs = config.roundTimeMs,
            timeRemainingMs = config.roundTimeMs,
            isRunning = true,
            lastTickAtMs = System.currentTimeMillis()
        )
    }

    /** Present a question to both participants (local or simulated). */
    fun presentQuestion(question: Question) {
        currentQuestion = question
        val round = questionRound ?: return
        version++
        questionRound = round.copy(
            questionId = question.id,
            question = question.toSnapshot()
        )
    }

    private fun Question.toSnapshot() = QuestionSnapshot(
        id = id,
        questionText = questionText,
        options = options
    )

    /**
     * The local player submits an answer.
     * In local/AI mode the core knows the correct index from the Question;
     * in online mode the server judges instead (see [judgeRound]).
     */
    fun playerSubmitAnswer(answerIndex: Int): Boolean {
        val round = questionRound ?: return false
        if (round.question == null || round.playerAnswered) return false
        version++
        questionRound = round.copy(
            playerAnswered = true,
            playerAnswerIndex = answerIndex
        )
        return true
    }

    /** AI opponent submits an answer (local AI mode). */
    fun opponentSubmitAnswer(answerIndex: Int) {
        val round = questionRound ?: return
        if (round.question == null || round.opponentAnswered) return
        version++
        questionRound = round.copy(
            opponentAnswered = true,
            opponentAnswerIndex = answerIndex
        )
    }

    /**
     * Judge the round locally (single-player / AI).
     * Correctness values are computed from the Question by the caller.
     */
    fun judgeRound(
        playerCorrect: Boolean,
        opponentCorrect: Boolean,
        playerTimeMs: Long,
        opponentTimeMs: Long
    ): RoundResult {
        val round = questionRound ?: return RoundResult.TIE
        version++

        val result: RoundResult = when {
            playerCorrect && opponentCorrect -> {
                if (playerTimeMs < opponentTimeMs) RoundResult.PLAYER_WIN else RoundResult.OPPONENT_WIN
            }
            playerCorrect -> RoundResult.PLAYER_WIN
            opponentCorrect -> RoundResult.OPPONENT_WIN
            else -> RoundResult.TIE
        }

        if (result == RoundResult.PLAYER_WIN) {
            stats = stats.copy(playerPerfectAnswers = stats.playerPerfectAnswers + 1)
        }
        if (result == RoundResult.OPPONENT_WIN) {
            stats = stats.copy(opponentPerfectAnswers = stats.opponentPerfectAnswers + 1)
        }

        questionRound = round.copy(roundResult = result)
        timer = timer.copy(isRunning = false)
        applyRoundResult(result)
        return result
    }

    private fun applyRoundResult(result: RoundResult) {
        version++
        val gain = 0.08f // progress units per won round
        when (result) {
            RoundResult.PLAYER_WIN -> {
                player = player.copy(progress = (player.progress + gain).coerceAtMost(1f))
            }
            RoundResult.OPPONENT_WIN -> {
                opponent = opponent.copy(progress = (opponent.progress + gain).coerceAtMost(1f))
            }
            else -> {}
        }
        checkFinish()
    }

    // ── Movement (1 / 2 / 3) ───────────────────────────────

    fun performMovement(movementType: Int, boost: Boolean = false): Boolean {
        if (phase != MatchPhase.RUNNING) return false
        version++

        val speed = when (movementType) {
            1 -> 0.04f
            2 -> 0.07f
            3 -> 0.11f
            else -> return false
        } * (if (boost) 1.5f else 1f)

        movement = movement.copy(
            lastMovementType = movementType,
            lastMovementAtMs = System.currentTimeMillis(),
            movement1Used = movement.movement1Used + if (movementType == 1) 1 else 0,
            movement2Used = movement.movement2Used + if (movementType == 2) 1 else 0,
            movement3Used = movement.movement3Used + if (movementType == 3) 1 else 0
        )

        player = player.copy(
            progress = (player.progress + speed).coerceAtMost(1f),
            position = player.progress * config.trackLength,
            isBoosting = boost,
            speed = speed
        )

        // Obstacle collision
        val hit = obstacles.find { o ->
            !o.isCleared && abs(o.position - player.position) < 30f && o.lane == player.lane
        }
        if (hit != null) {
            when {
                movementType == 3 && hit.isJumpable -> {
                    obstacles = obstacles.map { if (it.id == hit.id) it.copy(isCleared = true) else it }
                }
                hit.type == ObstacleType.ROCK || hit.type == ObstacleType.TRAP -> {
                    player = player.copy(hp = (player.hp - 10).coerceAtLeast(0))
                }
                !hit.isJumpable -> {
                    player = player.copy(isStunned = true)
                }
            }
        }

        checkFinish()
        return true
    }

    /** Advance one match frame; returns true when the round timed out. */
    fun tickTimer(): Boolean {
        if (!timer.isRunning) return false
        val now = System.currentTimeMillis()
        val remaining = (timer.timeRemainingMs - (now - timer.lastTickAtMs))
            .coerceIn(0L, timer.timeRemainingMs)
        timer = timer.copy(timeRemainingMs = remaining, lastTickAtMs = now)

        if (remaining <= 0L) {
            timer = timer.copy(isRunning = false)
            onRoundTimeout()
            return true
        }
        return false
    }

    private fun onRoundTimeout() {
        val round = questionRound ?: return
        version++
        // Player failed to answer in time → opponent advances
        questionRound = round.copy(roundResult = RoundResult.PLAYER_TIMEOUT)
        opponent = opponent.copy(progress = (opponent.progress + 0.06f).coerceAtMost(1f))
        checkFinish()
    }

    private fun checkFinish() {
        if (phase == MatchPhase.RUNNING && (player.progress >= 1f || opponent.progress >= 1f)) {
            phase = MatchPhase.FINISHED
            version++
            val winner = if (player.progress >= opponent.progress) player.playerId else opponent.playerId
            race = race.copy(finished = true, winnerId = winner)
        }
    }

    // ── Pause / Resume / Abandon ───────────────────────────

    fun pause() {
        if (phase == MatchPhase.RUNNING) phase = MatchPhase.PAUSED
        version++
    }

    fun resume() {
        if (phase == MatchPhase.PAUSED) phase = MatchPhase.RUNNING
        version++
    }

    fun disconnect() {
        if (phase == MatchPhase.RUNNING || phase == MatchPhase.PAUSED) {
            phase = MatchPhase.DISCONNECTED
        }
        version++
    }

    /** Terminate the match early (disconnect/forfeit/error). */
    fun finishEarly(reason: MatchEndReason, forfeiterId: String? = null) {
        phase = MatchPhase.FINISHED
        version++
        race = race.copy(
            finished = true,
            winnerId = if (forfeiterId == localPlayerId) opponent.playerId else localPlayerId
        )
    }

    // ── Snapshot / Replay ──────────────────────────────────

    fun toSnapshot(): MatchSnapshot = MatchSnapshot(
        matchId = config.matchId,
        phase = phase,
        version = version,
        race = race,
        player = player,
        opponent = opponent,
        questionRound = questionRound,
        timer = timer,
        movement = movement,
        obstacles = obstacles,
        sentAtMs = System.currentTimeMillis()
    )

    /** Apply authoritative server snapshot (online mode only). */
    fun applyRemoteSnapshot(snapshot: MatchSnapshot) {
        if (snapshot.version <= version) return
        opponent = snapshot.opponent
        timer = snapshot.timer
        race = race.copy(
            progressOpponent = snapshot.race.progressOpponent,
            finished = snapshot.race.finished,
            winnerId = snapshot.race.winnerId
        )
        version = snapshot.version
    }

    fun applyRemoteResult(result: MatchResult) {
        phase = MatchPhase.FINISHED
        version++
        race = race.copy(finished = true, winnerId = result.winnerId)
    }

    fun reset() {
        phase = MatchPhase.IDLE
        version = 0
        race = RaceState(trackLength = config.trackLength)
        currentQuestion = null
        player = RacePlayerState(playerId = localPlayerId)
        opponent = RacePlayerState(playerId = "opponent")
        questionRound = null
        timer = TimerState(roundTimeMs = config.roundTimeMs)
        movement = MovementState()
        obstacles = emptyList()
        stats = MatchStatistics()
    }
}

// ── Network Abstraction ────────────────────────────────────

/**
 * Thin network boundary between gameplay and backend.
 * The gameplay core never imports HTTP/WebSocket code — it talks to this
 * interface, and an implementation is injected at the app boundary.
 */
interface MatchNetworkSession {
    val isConnected: Boolean
    fun connect(matchId: String): Boolean
    fun send(snapshot: MatchSnapshot)
    fun onSnapshotReceived(callback: (MatchSnapshot) -> Unit)
    fun onResultReceived(callback: (MatchResult) -> Unit)
    fun onDisconnect(callback: (String) -> Unit)
    fun close()
}

/** Mock session for a real-prototype feel without a live server. */
class MockMatchNetworkSession : MatchNetworkSession {
    override var isConnected: Boolean = false
        private set

    private var snapshotCallback: ((MatchSnapshot) -> Unit)? = null
    private var resultCallback: ((MatchResult) -> Unit)? = null
    private var disconnectCallback: ((String) -> Unit)? = null
    private var lastSnapshot: MatchSnapshot? = null

    override fun connect(matchId: String): Boolean {
        isConnected = true
        return true
    }

    override fun send(snapshot: MatchSnapshot) {
        if (!isConnected) return
        lastSnapshot = snapshot
        // Simulate round-trip echo of opponent state
    }

    override fun onSnapshotReceived(callback: (MatchSnapshot) -> Unit) {
        snapshotCallback = callback
    }

    override fun onResultReceived(callback: (MatchResult) -> Unit) {
        resultCallback = callback
    }

    override fun onDisconnect(callback: (String) -> Unit) {
        disconnectCallback = callback
    }

    override fun close() {
        isConnected = false
    }

    /** Test hook — simulate an inbound snapshot. */
    fun injectSnapshot(snapshot: MatchSnapshot) {
        snapshotCallback?.invoke(snapshot)
    }
}