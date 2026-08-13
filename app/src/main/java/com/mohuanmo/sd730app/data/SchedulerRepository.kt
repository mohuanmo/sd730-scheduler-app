package com.mohuanmo.sd730app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SchedulerRepository {

    suspend fun checkEnvironment(): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        val hasRoot = ShellExecutor.hasRoot()
        val hasBinary = if (hasRoot) ShellExecutor.hasBinary() else false
        hasRoot to hasBinary
    }

    // ========== Status & Info ==========
    suspend fun getStatus(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--status")

    suspend fun getStats(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--stats")

    suspend fun getPredictionStats(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--prediction-stats")

    suspend fun getModeLearningStats(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--mode-learning-stats")

    suspend fun getTpinStatus(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--tpin-status")

    suspend fun getThreads(packageName: String? = null): ShellExecutor.Result {
        val args = mutableListOf("--threads")
        if (!packageName.isNullOrBlank()) args.add(packageName)
        return ShellExecutor.execScheduler(*args.toTypedArray())
    }

    suspend fun getAffinity(packageName: String): ShellExecutor.Result =
        ShellExecutor.execScheduler("--affinity", packageName)

    // ========== Mode Control ==========
    suspend fun setMode(mode: String): ShellExecutor.Result =
        ShellExecutor.execScheduler("--mode", mode)

    suspend fun setSceneMode(mode: String): ShellExecutor.Result =
        ShellExecutor.execScheduler("--scene", mode)

    // ========== Toggle Engines ==========
    suspend fun enablePrediction(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--prediction-enable")

    suspend fun disablePrediction(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--prediction-disable")

    suspend fun enableTpin(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--tpin-enable")

    suspend fun disableTpin(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--tpin-disable")

    // ========== Reset Operations ==========
    suspend fun resetLearning(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--reset-learning")

    suspend fun resetModeLearning(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--reset-mode-learning")

    suspend fun resetPrediction(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--reset-prediction")

    suspend fun resetTpin(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--tpin-reset")

    suspend fun resetSelfm(): ShellExecutor.Result =
        ShellExecutor.execScheduler("--selfm-reset")

    companion object {
        val MODES = listOf("powersave", "balanced", "performance", "ultra")
        val MODE_LABELS = mapOf(
            "powersave" to "省电",
            "balanced" to "均衡",
            "performance" to "性能",
            "ultra" to "极速"
        )
        val MODE_COLORS = mapOf(
            "powersave" to 0xFF4CAF50,
            "balanced" to 0xFF2196F3,
            "performance" to 0xFFFF9800,
            "ultra" to 0xFFF44336
        )
    }
}
