package com.animalbattle.game.util

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Error Handler ──────────────────────────────────────────

object ErrorHandler {
    private const val TAG = "AnimalBattle"
    private const val ERROR_LOG_FILE = "error_log.txt"
    private const val MAX_LOG_SIZE = 100 * 1024 // 100KB

    private var context: Context? = null
    private var onCrashCallback: ((String) -> Unit)? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext

        // Set up uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                logError("Uncaught Exception", throwable)
                saveErrorLog(throwable)
                onCrashCallback?.invoke(throwable.message ?: "Unknown error")
            } catch (_: Exception) {
                // Don't crash in crash handler
            }
            // Pass to default handler
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun setCrashCallback(callback: (String) -> Unit) {
        onCrashCallback = callback
    }

    // ── Error Logging ──────────────────────────────────────

    fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
        saveToLogFile("ERROR: $message\n${throwable?.stackTraceToString() ?: ""}")
    }

    fun logWarning(message: String) {
        Log.w(TAG, message)
        saveToLogFile("WARNING: $message")
    }

    fun logInfo(message: String) {
        Log.i(TAG, message)
    }

    // ── Error Log File ─────────────────────────────────────

    private fun saveToLogFile(entry: String) {
        try {
            val ctx = context ?: return
            val file = File(ctx.filesDir, ERROR_LOG_FILE)

            // Truncate if too large
            if (file.exists() && file.length() > MAX_LOG_SIZE) {
                file.writeText("")
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val logEntry = "[$timestamp] $entry\n"

            file.appendText(logEntry)
        } catch (_: Exception) {
            // Don't crash on log write failure
        }
    }

    private fun saveErrorLog(throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val deviceInfo = """
            |Device: ${Build.MANUFACTURER} ${Build.MODEL}
            |Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            |App Version: 1.0.0
            |
            |Stack Trace:
            |$stackTrace
        """.trimMargin()

        saveToLogFile("CRASH: $deviceInfo")
    }

    fun getErrorLog(): String {
        return try {
            val ctx = context ?: return "No context"
            val file = File(ctx.filesDir, ERROR_LOG_FILE)
            if (file.exists()) file.readText() else "No errors logged"
        } catch (_: Exception) {
            "Unable to read error log"
        }
    }

    fun clearErrorLog() {
        try {
            val ctx = context ?: return
            val file = File(ctx.filesDir, ERROR_LOG_FILE)
            if (file.exists()) file.delete()
        } catch (_: Exception) { /* ignore */ }
    }

    // ── Safe Execution ─────────────────────────────────────

    inline fun <T> safeRun(block: () -> T, fallback: T, tag: String = ""): T {
        return try {
            block()
        } catch (e: Exception) {
            logError("${tag.ifEmpty { "SafeRun" }}: ${e.message}", e)
            fallback
        }
    }

    inline fun safeRun(block: () -> Unit, tag: String = "") {
        try {
            block()
        } catch (e: Exception) {
            logError("${tag.ifEmpty { "SafeRun" }}: ${e.message}", e)
        }
    }
}

// ── Result Wrapper ─────────────────────────────────────────

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    fun <R> map(transform: (T) -> R): AppResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    companion object {
        inline fun <T> runCatching(block: () -> T): AppResult<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                ErrorHandler.logError("AppResult error: ${e.message}", e)
                Error(e.message ?: "Unknown error", e)
            }
        }
    }
}

// ── Validation Utils ───────────────────────────────────────

object ValidationUtils {
    fun isValidPlayerName(name: String): Boolean {
        return name.length in 1..20 && name.all { it.isLetterOrDigit() || it == ' ' }
    }

    fun isValidDeviceId(deviceId: String): Boolean {
        return deviceId.length in 1..128
    }

    fun sanitizeInput(input: String): String {
        return input.trim().take(128)
    }
}
