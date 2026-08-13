package com.mohuanmo.sd730app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun execute(name: String, block: suspend () -> com.mohuanmo.sd730app.data.ShellExecutor.Result) {
        scope.launch {
            isLoading = true
            val result = block()
            isLoading = false
            snackbarMessage = if (result.success) {
                "$name 成功"
            } else {
                "$name 失败: ${result.stderr.ifBlank { "code ${result.exitCode}" }}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        // Reset group
        Text(
            text = "重置操作",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.reset_learning),
            icon = Icons.Default.DeleteForever,
            onClick = { execute(stringResource(R.string.reset_learning)) { repository.resetLearning() } }
        )
        ActionCard(
            title = stringResource(R.string.reset_mode_learning),
            icon = Icons.Default.DeleteForever,
            onClick = { execute(stringResource(R.string.reset_mode_learning)) { repository.resetModeLearning() } }
        )
        ActionCard(
            title = stringResource(R.string.reset_prediction),
            icon = Icons.Default.DeleteForever,
            onClick = { execute(stringResource(R.string.reset_prediction)) { repository.resetPrediction() } }
        )
        ActionCard(
            title = stringResource(R.string.tpin_reset),
            icon = Icons.Default.LockReset,
            onClick = { execute(stringResource(R.string.tpin_reset)) { repository.resetTpin() } }
        )
        ActionCard(
            title = stringResource(R.string.selfm_reset),
            icon = Icons.Default.LockReset,
            onClick = { execute(stringResource(R.string.selfm_reset)) { repository.resetSelfm() } }
        )

        Spacer(Modifier.height(24.dp))

        // Toggle group
        Text(
            text = "引擎开关",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))

        ActionCard(
            title = stringResource(R.string.prediction_enable),
            icon = Icons.Default.ToggleOn,
            onClick = { execute(stringResource(R.string.prediction_enable)) { repository.enablePrediction() } }
        )
        ActionCard(
            title = stringResource(R.string.prediction_disable),
            icon = Icons.Default.ToggleOff,
            onClick = { execute(stringResource(R.string.prediction_disable)) { repository.disablePrediction() } }
        )
        ActionCard(
            title = stringResource(R.string.tpin_enable),
            icon = Icons.Default.ToggleOn,
            onClick = { execute(stringResource(R.string.tpin_enable)) { repository.enableTpin() } }
        )
        ActionCard(
            title = stringResource(R.string.tpin_disable),
            icon = Icons.Default.ToggleOff,
            onClick = { execute(stringResource(R.string.tpin_disable)) { repository.disableTpin() } }
        )
    }

    // Snackbar
    snackbarMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2500)
            snackbarMessage = null
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        snackbarMessage?.let { msg ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("确定")
                    }
                }
            ) { Text(msg) }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(icon, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = "执行",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
