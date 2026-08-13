package com.mohuanmo.sd730app.ui.screen

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GpuScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Mode Resolution
    var effectiveMode by remember { mutableStateOf("—") }
    var manualMode by remember { mutableStateOf("—") }
    var sceneBaseMode by remember { mutableStateOf("—") }
    var learnedOverride by remember { mutableStateOf("—") }

    // GPU Adaptive Scheduler
    var adaptiveEnabled by remember { mutableStateOf("—") }
    var writeFirstInterval by remember { mutableStateOf("—") }
    var ultraRelaxFloor by remember { mutableStateOf("—") }
    var importantSceneThreshold by remember { mutableStateOf("—") }
    var relaxHysteresis by remember { mutableStateOf("—") }
    var tierGates by remember { mutableStateOf("—") }

    // Live GPU Nodes
    var curFreq by remember { mutableStateOf("—") }
    var minFreq by remember { mutableStateOf("—") }
    var maxFreq by remember { mutableStateOf("—") }
    var governor by remember { mutableStateOf("—") }
    var pwrlevel by remember { mutableStateOf("—") }
    var oppTable by remember { mutableStateOf("—") }

    // Status
    var ultraLock by remember { mutableStateOf("—") }
    var appTier by remember { mutableStateOf("—") }
    var foreground by remember { mutableStateOf("—") }
    var watchdog by remember { mutableStateOf("—") }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            val result = repository.getGpuStatus()
            isLoading = false

            output = result.stdout

            result.stdout.lineSequence().forEach { line ->
                when {
                    // Mode Resolution
                    line.startsWith("Effective mode (.last_mode):") ->
                        effectiveMode = line.substringAfter(":").trim()
                    line.startsWith("Manual mode (current_mode):") ->
                        manualMode = line.substringAfter(":").trim()
                    line.startsWith("Scene base mode:") ->
                        sceneBaseMode = line.substringAfter(":").trim()
                    line.startsWith("Learned per-app override:") ->
                        learnedOverride = line.substringAfter(":").trim()

                    // GPU Adaptive Scheduler
                    line.startsWith("Enabled:") && !line.contains("GPU") && !line.contains("Thread") ->
                        adaptiveEnabled = line.substringAfter(":").trim()
                    line.startsWith("Write-first interval:") ->
                        writeFirstInterval = line.substringAfter(":").trim()
                    line.startsWith("Ultra relax floor:") ->
                        ultraRelaxFloor = line.substringAfter(":").trim()
                    line.startsWith("Important-scene threshold:") ->
                        importantSceneThreshold = line.substringAfter(":").trim()
                    line.startsWith("Relax hysteresis:") ->
                        relaxHysteresis = line.substringAfter(":").trim()
                    line.startsWith("Tier gates:") ->
                        tierGates = line.substringAfter(":").trim()

                    // Live GPU Nodes
                    line.contains("cur_freq:") && !line.contains("min_freq") ->
                        curFreq = line.substringAfter("cur_freq:").trim()
                    line.contains("min_freq:") -> {
                        val afterMin = line.substringAfter("min_freq:").trim()
                        minFreq = afterMin.substringBefore("max_freq:").trim()
                        maxFreq = line.substringAfter("max_freq:").trim()
                    }
                    line.contains("governor:") ->
                        governor = line.substringAfter("governor:").trim()
                    line.contains("pwrlevel:") ->
                        pwrlevel = line.substringAfter("pwrlevel:").trim()
                    line.contains("OPP table:") ->
                        oppTable = line.substringAfter("OPP table:").trim()

                    // Status
                    line.startsWith("Ultra lock:") ->
                        ultraLock = line.substringAfter(":").trim()
                    line.startsWith("App tier:") ->
                        appTier = line.substringAfter(":").trim()
                    line.startsWith("Foreground:") ->
                        foreground = line.substringAfter(":").trim()
                    line.startsWith("Watchdog:") ->
                        watchdog = line.substringAfter(":").trim()
                }
            }

            if (!result.success) {
                error = result.stderr.ifBlank { "Exit: ${result.exitCode} (output may still be valid)" }
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.gpu_status),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { load() }, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // === Mode Resolution ===
            SectionTitle(stringResource(R.string.mode_resolution), Icons.Default.Tune)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                GpuInfoCard(stringResource(R.string.effective_mode), effectiveMode, Modifier.weight(1f))
                GpuInfoCard(stringResource(R.string.manual_mode), manualMode, Modifier.weight(1f))
                GpuInfoCard(stringResource(R.string.scene_base_mode), sceneBaseMode, Modifier.weight(1f))
                GpuInfoCard(stringResource(R.string.learned_override), learnedOverride, Modifier.weight(1f))
            }

            // === GPU Adaptive Scheduler ===
            SectionTitle(stringResource(R.string.gpu_adaptive_scheduler), Icons.Default.AutoGraph)
            GpuInfoCard("Enabled", adaptiveEnabled, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.write_first_interval), writeFirstInterval, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.ultra_relax_floor), ultraRelaxFloor, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.important_scene_threshold), importantSceneThreshold, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.relax_hysteresis), relaxHysteresis, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.tier_gates), tierGates, Modifier.fillMaxWidth())

            // === Live GPU Nodes ===
            SectionTitle(stringResource(R.string.live_gpu_nodes), Icons.Default.Speed)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                GpuInfoCard("cur_freq", curFreq, Modifier.weight(1f))
                GpuInfoCard("governor", governor, Modifier.weight(1f))
                GpuInfoCard("min_freq", minFreq, Modifier.weight(1f))
                GpuInfoCard("max_freq", maxFreq, Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.pwrlevel), pwrlevel, Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
            GpuInfoCard(stringResource(R.string.opp_table), oppTable, Modifier.fillMaxWidth())

            // === Status ===
            SectionTitle("状态", Icons.Default.Visibility)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                GpuInfoCard(stringResource(R.string.ultra_lock), ultraLock, Modifier.weight(1f))
                GpuInfoCard(stringResource(R.string.app_tier), appTier, Modifier.weight(1f))
                GpuInfoCard(stringResource(R.string.foreground_app), foreground, Modifier.weight(1f))
                GpuInfoCard(stringResource(R.string.watchdog), watchdog, Modifier.weight(1f))
            }

            if (isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Raw output
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.command_output),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    SelectionContainer {
                        Text(
                            text = output.ifBlank { stringResource(R.string.empty) },
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun GpuInfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
