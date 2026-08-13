package com.mohuanmo.sd730app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object ShellExecutor {
    private const val TAG = "ShellExecutor"
    private const val TIMEOUT_SECONDS = 15L
    private const val BINARY = "sd730-scheduler"

    data class Result(
        val success: Boolean,
        val stdout: String,
        val stderr: String,
        val exitCode: Int
    )

    suspend fun hasRoot(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su -c id")
            val exit = process.waitFor(3, TimeUnit.SECONDS)
            if (!exit) {
                process.destroyForcibly()
                return@withContext false
            }
            process.exitValue() == 0
        } catch (e: Exception) {
            Log.e(TAG, "Root check failed", e)
            false
        }
    }

    suspend fun hasBinary(): Boolean = withContext(Dispatchers.IO) {
        val result = exec("command -v $BINARY || echo '/system/bin/$BINARY'")
        result.success && result.stdout.trim().isNotEmpty()
    }

    suspend fun exec(command: String): Result = withContext(Dispatchers.IO) {
        val fullCommand = "su -c '$command'"
        Log.d(TAG, "Executing: $fullCommand")
        try {
            val process = Runtime.getRuntime().exec(fullCommand)
            val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return@withContext Result(
                    success = false,
                    stdout = "",
                    stderr = "Command timed out after ${TIMEOUT_SECONDS}s",
                    exitCode = -1
                )
            }
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            Result(
                success = process.exitValue() == 0,
                stdout = stdout,
                stderr = stderr,
                exitCode = process.exitValue()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Execution failed", e)
            Result(
                success = false,
                stdout = "",
                stderr = e.message ?: "Unknown error",
                exitCode = -1
            )
        }
    }

    suspend fun execScheduler(vararg args: String): Result {
        val cmd = "$BINARY ${args.joinToString(" ")}"
        return exec(cmd)
    }
}
