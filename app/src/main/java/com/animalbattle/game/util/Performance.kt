package com.animalbattle.game.util

import android.os.Debug
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

// ── FPS Monitor ────────────────────────────────────────────

class FPSMonitor {
    private var lastFrameTime = SystemClock.elapsedRealtime()
    private var frameCount = 0
    private var _currentFPS = 0f
    private var _averageFPS = 0f
    private var _minFPS = Float.MAX_VALUE
    private var _maxFPS = 0f
    private var _totalFrames = 0L
    private var _droppedFrames = 0L

    val currentFPS: Float get() = _currentFPS
    val averageFPS: Float get() = _averageFPS
    val minFPS: Float get() = if (_minFPS == Float.MAX_VALUE) 0f else _minFPS
    val maxFPS: Float get() = _maxFPS
    val totalFrames: Long get() = _totalFrames
    val droppedFrames: Long get() = _droppedFrames
    val frameDropPercentage: Float
        get() = if (_totalFrames > 0) _droppedFrames.toFloat() / _totalFrames else 0f

    fun onFrame() {
        val now = SystemClock.elapsedRealtime()
        val delta = now - lastFrameTime
        lastFrameTime = now
        frameCount++
        _totalFrames++

        // Track dropped frames (took longer than 20ms = below 50fps)
        if (delta > 20) {
            _droppedFrames++
        }

        // Calculate FPS every second
        if (frameCount >= 60) {
            _currentFPS = 1000f / (delta.toFloat()).coerceAtLeast(1f) * frameCount
            _averageFPS = (_averageFPS + _currentFPS) / 2
            _minFPS = minOf(_minFPS, _currentFPS)
            _maxFPS = maxOf(_maxFPS, _currentFPS)
            frameCount = 0
        }
    }

    fun reset() {
        frameCount = 0
        _currentFPS = 0f
        _averageFPS = 0f
        _minFPS = Float.MAX_VALUE
        _maxFPS = 0f
        _totalFrames = 0
        _droppedFrames = 0
    }
}

// ── Memory Tracker ─────────────────────────────────────────

class MemoryTracker {
    private var _usedMemoryMB = 0f
    private var _maxMemoryMB = 0f
    private var _availableMemoryMB = 0f

    val usedMemoryMB: Float get() = _usedMemoryMB
    val maxMemoryMB: Float get() = _maxMemoryMB
    val availableMemoryMB: Float get() = _availableMemoryMB
    val memoryUsagePercentage: Float
        get() = if (_maxMemoryMB > 0) _usedMemoryMB / _maxMemoryMB else 0f

    fun update() {
        val runtime = Runtime.getRuntime()
        _maxMemoryMB = runtime.maxMemory() / (1024f * 1024f)
        _usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f)
        _availableMemoryMB = (runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()) / (1024f * 1024f)
    }

    fun isLowMemory(): Boolean = memoryUsagePercentage > 0.85f
}

// ── Generic Object Pool ────────────────────────────────────

open class ObjectPool<T>(
    private val maxSize: Int = 100,
    private val factory: () -> T,
    private val reset: (T) -> Unit = {}
) {
    private val pool = mutableListOf<T>()
    private var activeCount = 0

    val size: Int get() = pool.size + activeCount
    val availableCount: Int get() = pool.size

    fun acquire(): T {
        return if (pool.isNotEmpty()) {
            activeCount++
            pool.removeLast()
        } else {
            activeCount++
            factory()
        }
    }

    fun release(item: T) {
        if (activeCount > 0) activeCount--
        if (pool.size < maxSize) {
            reset(item)
            pool.add(item)
        }
    }

    fun releaseAll(items: Iterable<T>) {
        items.forEach { release(it) }
    }

    fun clear() {
        pool.clear()
        activeCount = 0
    }

    fun prewarm(count: Int) {
        repeat(count) {
            if (pool.size < maxSize) {
                pool.add(factory())
            }
        }
    }
}

// ── Frame-Aware Composable ─────────────────────────────────

@Composable
fun rememberFPSMonitor(): FPSMonitor {
    val monitor = remember { FPSMonitor() }
    LaunchedEffect(Unit) {
        while (true) {
            monitor.onFrame()
            delay(16) // ~60fps
        }
    }
    return monitor
}

@Composable
fun rememberMemoryTracker(): MemoryTracker {
    val tracker = remember { MemoryTracker() }
    LaunchedEffect(Unit) {
        while (true) {
            tracker.update()
            delay(1000) // Update every second
        }
    }
    return tracker
}

// ── Performance Stats Display ──────────────────────────────

@Composable
fun rememberPerformanceStats(): PerformanceStats {
    val fpsMonitor = rememberFPSMonitor()
    val memoryTracker = rememberMemoryTracker()

    return remember {
        PerformanceStats(fpsMonitor, memoryTracker)
    }
}

class PerformanceStats(
    val fps: FPSMonitor,
    val memory: MemoryTracker
) {
    val isPerformant: Boolean
        get() = fps.currentFPS >= 55f && !memory.isLowMemory()

    val statusText: String
        get() {
            val fpsText = "${fps.currentFPS.toInt()} FPS"
            val memText = "${memory.usedMemoryMB.toInt()}MB"
            return "$fpsText | $memText"
        }
}
