package com.animalbattle.game.online

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ── Matchmaking Events ─────────────────────────────────────

sealed class MatchmakingEvent {
    data class SearchingStarted(val startedAtMs: Long) : MatchmakingEvent()
    data class SearchCanceled(val reason: String) : MatchmakingEvent()
    data class OpponentFound(val opponentId: String, val opponentName: String, val opponentLevel: Int, val matchId: String) : MatchmakingEvent()
    data class SearchTimeout(val waitedMs: Long) : MatchmakingEvent()
    data class SearchError(val message: String) : MatchmakingEvent()
}

// ── Matchmaking Manager ────────────────────────────────────

/**
 * Matchmaking: quick match with skill-based filtering.
 *
 * Uses the same [MatchmakingBackend] abstraction as the rest of the
 * online layer. A mock backend drives the search locally; the real
 * backend would be the Node.js server with a WebSocket/queue.
 */
class MatchmakingManager(
    private val backend: MatchmakingBackend = MockMatchmakingBackend()
) {
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _searchProgressText = MutableStateFlow("")
    val searchProgressText: StateFlow<String> = _searchProgressText

    private val _events = MutableSharedFlow<MatchmakingEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<MatchmakingEvent> = _events

    private var searchJob: kotlinx.coroutines.Job? = null
    private var startedAtMs = 0L
    private val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default)

    companion object {
        const val DEFAULT_SEARCH_TIMEOUT_MS = 30_000L
        const val SKILL_WINDOW_TROPHIES = 100
    }

    fun startQuickMatch(
        playerTrophies: Int,
        timeoutMs: Long = DEFAULT_SEARCH_TIMEOUT_MS
    ) {
        if (_isSearching.value) return
        _isSearching.value = true
        startedAtMs = System.currentTimeMillis()
        _searchProgressText.value = "Finding opponent…"
        _events.tryEmit(MatchmakingEvent.SearchingStarted(startedAtMs))

        searchJob = scope.launch {
            val found = backend.findOpponent(playerTrophies, SKILL_WINDOW_TROPHIES)
            if (!found.isFound) {
                _isSearching.value = false
                _searchProgressText.value = ""
                _events.tryEmit(MatchmakingEvent.SearchTimeout(System.currentTimeMillis() - startedAtMs))
                return@launch
            }

            // Simulate natural search progression
            _searchProgressText.value = "Checking opponent skill…"

            // Wait for the "network" to finalize the pairing
            delay(800)

            _isSearching.value = false
            _searchProgressText.value = ""
            _events.tryEmit(
                MatchmakingEvent.OpponentFound(
                    opponentId = found.opponentId,
                    opponentName = found.opponentName,
                    opponentLevel = found.opponentLevel,
                    matchId = found.matchId
                )
            )
        }

        // Timeout watcher
        scope.launch {
            delay(timeoutMs)
            if (_isSearching.value) {
                cancelSearch("timeout")
                _events.tryEmit(MatchmakingEvent.SearchTimeout(System.currentTimeMillis() - startedAtMs))
            }
        }
    }

    fun cancelSearch(reason: String = "cancelled") {
        if (!_isSearching.value) return
        searchJob?.cancel()
        searchJob = null
        _isSearching.value = false
        _searchProgressText.value = ""
        _events.tryEmit(MatchmakingEvent.SearchCanceled(reason))
    }
}

// ── Backend Abstraction ────────────────────────────────────

data class OpponentCandidate(
    val opponentId: String,
    val opponentName: String,
    val opponentLevel: Int,
    val opponentTrophies: Int,
    val matchId: String,
    val isFound: Boolean = true
)

interface MatchmakingBackend {
    suspend fun findOpponent(playerTrophies: Int, skillWindow: Int): OpponentCandidate
}

/** Local mock — simulates a matchmaking server for prototyping. */
class MockMatchmakingBackend : MatchmakingBackend {
    override suspend fun findOpponent(playerTrophies: Int, skillWindow: Int): OpponentCandidate {
        // Simulate a few seconds of "server" search
        kotlinx.coroutines.delay(2_500)
        val names = listOf("ShadowRex", "BlazeWolf", "StormEagle", "FrostLion", "ThunderCobra")
        val opponents = (0 until names.size).map { i ->
            OpponentCandidate(
                opponentId = "rem_${i + 100}",
                opponentName = names[i],
                opponentLevel = 3 + i,
                opponentTrophies = (playerTrophies - 60 + i * 30).coerceAtLeast(0),
                matchId = MatchIdFactory.create()
            )
        }
        return opponents.filter {
            kotlin.math.abs(it.opponentTrophies - playerTrophies) <= skillWindow
        }.firstOrNull() ?: opponents.first()
    }
}

object MatchIdFactory {
    private val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    fun create(): String = (1..6).map { chars[kotlin.random.Random.nextInt(chars.length)] }.joinToString("")
}