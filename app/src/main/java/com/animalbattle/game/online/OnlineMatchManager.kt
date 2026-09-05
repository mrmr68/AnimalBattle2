package com.animalbattle.game.online

import com.animalbattle.game.gameplay.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── Online Match Events ────────────────────────────────────

sealed class OnlineMatchEvent {
    data class StateChanged(val snapshot: MatchSnapshot) : OnlineMatchEvent()
    data class MatchEnded(val result: MatchResult) : OnlineMatchEvent()
    data class OpponentDisconnected(val reason: String) : OnlineMatchEvent()
    data class Reconnecting(val attempt: Int) : OnlineMatchEvent()
    data class Reconnected(val snapshot: MatchSnapshot) : OnlineMatchEvent()
    data class Error(val message: String) : OnlineMatchEvent()
}

// ── Online Match Manager ───────────────────────────────────

/**
 * Drives an online 1v1 match using [GameplayCore] and a
 * [MatchNetworkSession]. The gameplay core stays backend-agnostic;
 * this manager owns the sync loop, reconnection and result delivery.
 *
 * ══ Security ══
 * Correctness of answers and rewards come from the server via
 * [MatchNetworkSession.onResultReceived]. This client never sends
 * "I won + coins" — it sends raw inputs (answer index, movement choice).
 */
class OnlineMatchManager(
    private val core: GameplayCore,
    private val session: MatchNetworkSession
) {
    private val _phase = MutableStateFlow(core.phase)
    val phase: StateFlow<MatchPhase> = _phase

    private val _snapshot = MutableStateFlow(core.toSnapshot())
    val snapshot: StateFlow<MatchSnapshot> = _snapshot

    private val _events = MutableSharedFlow<OnlineMatchEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<OnlineMatchEvent> = _events

    private var syncJob: Job? = null
    private var syncTickMs = 250L
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)

    companion object {
        const val SYNC_INTERVAL_MS = 250L
        const val MAX_RECONNECT_ATTEMPTS = 3
        const val RECONNECT_DELAY_MS = 2_000L
    }

    init {
        session.onSnapshotReceived { snapshot ->
            core.applyRemoteSnapshot(snapshot)
            publishState()
        }
        session.onResultReceived { result ->
            core.applyRemoteResult(result)
            _phase.value = core.phase
            _events.tryEmit(OnlineMatchEvent.MatchEnded(result))
            stopSync()
        }
        session.onDisconnect { reason ->
            _events.tryEmit(OnlineMatchEvent.OpponentDisconnected(reason))
            handleDisconnect(reason)
        }
    }

    // ── Lifecycle ──────────────────────────────────────────

    fun connect(matchId: String): Boolean {
        val connected = session.connect(matchId)
        if (connected) {
            _phase.value = MatchPhase.CONNECTING
        }
        return connected
    }

    fun startMatch(opponentId: String, opponentAnimalId: String, opponentName: String) {
        core.startMatch(opponentId, opponentAnimalId, opponentName)
        core.beginRace()
        _phase.value = core.phase
        startSync()
        publishState()
    }

    /** Local player answers — correctness is NOT claimed client-side. */
    fun submitAnswer(answerIndex: Int) {
        core.playerSubmitAnswer(answerIndex)
        session.send(core.toSnapshot())
        publishState()
    }

    fun performMovement(movementType: Int, boost: Boolean = false) {
        core.performMovement(movementType, boost)
        session.send(core.toSnapshot())
        publishState()
    }

    // ── Sync Loop ──────────────────────────────────────────

    private fun startSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (core.phase == MatchPhase.RUNNING || core.phase == MatchPhase.STARTING) {
                session.send(core.toSnapshot())
                delay(syncTickMs)
            }
        }
    }

    private fun stopSync() {
        syncJob?.cancel()
        syncJob = null
    }

    private fun publishState() {
        _snapshot.value = core.toSnapshot()
        _phase.value = core.phase
        _events.tryEmit(OnlineMatchEvent.StateChanged(core.toSnapshot()))
    }

    // ── Disconnect / Reconnect ─────────────────────────────

    private fun handleDisconnect(reason: String) {
        core.disconnect()
        _phase.value = core.phase

        var attempt = 0
        scope.launch {
            while (attempt < MAX_RECONNECT_ATTEMPTS) {
                attempt++
                _events.tryEmit(OnlineMatchEvent.Reconnecting(attempt))
                delay(RECONNECT_DELAY_MS * attempt)
                val reconnected = session.connect(core.config.matchId)
                if (reconnected) {
                    // Ask for a fresh snapshot by sending ours (server replies
                    // with authoritative one via onSnapshotReceived)
                    session.send(core.toSnapshot())
                    _phase.value = MatchPhase.RECONNECTING
                    _events.tryEmit(OnlineMatchEvent.Reconnected(core.toSnapshot()))
                    return@launch
                }
            }
            // Give up after attempts → forfeit (server will confirm)
            core.finishEarly(MatchEndReason.FORFEIT, forfeiterId = core.localPlayerId)
            _phase.value = core.phase
            _events.tryEmit(OnlineMatchEvent.Error("Connection lost. Match forfeited."))
        }
    }

    fun abandon() {
        core.finishEarly(MatchEndReason.FORFEIT, core.localPlayerId)
        session.close()
        stopSync()
        _phase.value = core.phase
    }

    fun cleanup() {
        stopSync()
        session.close()
    }
}