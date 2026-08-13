package com.mohuanmo.sd730app.ui.screen

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import com.mohuanmo.sd730app.data.ShellExecutor
import com.mohuanmo.sd730app.ui.theme.BalancedBlue
import com.mohuanmo.sd730app.ui.theme.PerformanceOrange
import com.mohuanmo.sd730app.ui.theme.PowersaveGreen
import com.mohuanmo.sd730app.ui.theme.UltraRed
import kotlinx.coroutines.launch

@Composable
fun StatusScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Parsed fields
    var currentMode by remember { mutableStateOf("—") }
    var sceneOverride by remember { mutableStateOf("—") }
    var temperature by remember { mutableStateOf("—") }
    var battery by remember { mutableStateOf("—") }
    var foregroundApp by remember { mutableStateOf("—") }
    var gpuLoad by remember { mutableStateOf("—") }
    var prediction by remember { mutableStateOf("—") }
    var tpinEnabled by remember { mutableStateOf("—") }

    fun load() {
        scope.launch {
            isLoading = true
            errorMsg = null
            val result = repository.getStatus()
            isLoading = false
            if (result.success) {
                statusText = result.stdout
                // Parse key fields
                result.stdout.lineSequence().forEach { line ->
                    when {
                        line.startsWith("Mode:") -> currentMode = line.substringAfter(":").trim()
                        line.startsWith("Scene Override:") -> sceneOverride = line.substringAfter(":").trim()
                        line.startsWith("Temperature:") -> temperature = line.substringAfter(":").trim()
                        line.startsWith("Battery:") -> battery = line.substringAfter(":").trim()
                        line.startsWith("Foreground App:") -> foregroundApp = line.substringAfter(":").trim()
                        line.startsWith("GPU Load:") -> gpuLoad = line.substringAfter(":").trim()
                        line.startsWith("Prediction Engine:") -> prediction = line.substringAfter(":").trim()
                        line.startsWith("Enabled:") && tpinEnabled == "—" -> tpinEnabled = line.substringAfter(":").trim()
                    }
                }
            } else {
                errorMsg = result.stderr.ifBlank { "Exit code: ${result.exitCode}" }
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.current_mode),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { load() }, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
            }
        }

        Spacer(Modifier.height(12.dp))

        // Mode Card
        val modeColor = when (currentMode.lowercase()) {
            "powersave" -> PowersaveGreen
            "balanced" -> BalancedBlue
            "performance" -> PerformanceOrange
            "ultra" -> UltraRed
            else -> MaterialTheme.colorScheme.primary
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = modeColor.copy(alpha = 0.15f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = currentMode.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineMedium,
                    color = modeColor
                )
                Text(
                    text = SchedulerRepository.MODE_LABELS[currentMode.lowercase()] ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Quick info grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            InfoChip(Icons.Default.Thermostat, stringResource(R.string.temperature), temperature)
            InfoChip(Icons.Default.BatteryFull, stringResource(R.string.battery), battery)
            InfoChip(Icons.Default.Apps, stringResource(R.string.foreground_app), foregroundApp)
            InfoChip(Icons.Default.Speed, stringResource(R.string.gpu_load), gpuLoad)
            InfoChip(Icons.Default.Memory, stringResource(R.string.scene_override), sceneOverride)
            InfoChip(Icons.Default.Memory, stringResource(R.string.prediction), prediction)
            InfoChip(Icons.Default.Memory, stringResource(R.string.thread_pin), tpinEnabled)
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }

        if (errorMsg != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
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
                Text(
                    text = statusText.ifBlank { stringResource(R.string.empty) },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.widthIn(min = 140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall)
                Text(text = value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
