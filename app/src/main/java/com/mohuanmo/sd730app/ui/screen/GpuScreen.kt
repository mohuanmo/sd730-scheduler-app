package com.mohuanmo.sd730app.ui.screen

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import kotlinx.coroutines.launch

@Composable
fun GpuScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Parsed fields
    var curFreq by remember { mutableStateOf("—") }
    var minFreq by remember { mutableStateOf("—") }
    var maxFreq by remember { mutableStateOf("—") }
    var governor by remember { mutableStateOf("—") }
    var pwrLevel by remember { mutableStateOf("—") }
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
                    line.contains("cur_freq:") -> curFreq = line.substringAfter("cur_freq:").trim()
                    line.contains("min_freq:") -> minFreq = line.substringAfter("min_freq:").trim()
                    line.contains("max_freq:") -> maxFreq = line.substringAfter("max_freq:").trim()
                    line.contains("governor:") -> governor = line.substringAfter("governor:").trim()
                    line.contains("pwrlevel:") -> pwrLevel = line.substringAfter("pwrlevel:").trim()
                    line.startsWith("Ultra lock:") -> ultraLock = line.substringAfter(":").trim()
                    line.startsWith("App tier:") -> appTier = line.substringAfter(":").trim()
                    line.startsWith("Foreground:") -> foreground = line.substringAfter(":").trim()
                    line.startsWith("Watchdog:") -> watchdog = line.substringAfter(":").trim()
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

        // GPU Info Cards
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GpuInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Speed,
                    label = "当前频率",
                    value = curFreq
                )
                GpuInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Memory,
                    label = "频率范围",
                    value = "$minFreq / $maxFreq"
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GpuInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Visibility,
                    label = "Governor",
                    value = governor
                )
                GpuInfoCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Speed,
                    label = "Power Level",
                    value = pwrLevel
                )
            }
            GpuInfoCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Memory,
                label = "Ultra Lock",
                value = ultraLock
            )
            GpuInfoCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Visibility,
                label = "App Tier / Foreground",
                value = "$appTier | $foreground"
            )
            GpuInfoCard(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Memory,
                label = "Watchdog",
                value = watchdog
            )
        }

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
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
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
private fun GpuInfoCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
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
