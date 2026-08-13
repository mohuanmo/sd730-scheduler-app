package com.mohuanmo.sd730app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ThreadsScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var pkgInput by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isMonitoring by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun fetch() {
        scope.launch {
            isLoading = true
            error = null
            val result = repository.getThreads(pkgInput.takeIf { it.isNotBlank() })
            isLoading = false
            if (result.success) {
                output = result.stdout
            } else {
                error = result.stderr.ifBlank { "Exit: ${result.exitCode}" }
            }
        }
    }

    // Live monitor loop
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            val result = repository.getThreads(pkgInput.takeIf { it.isNotBlank() })
            if (result.success) {
                output = result.stdout
                error = null
            } else {
                error = result.stderr.ifBlank { "Exit: ${result.exitCode}" }
            }
            delay(3000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.live_monitor),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = pkgInput,
            onValueChange = { pkgInput = it },
            label = { Text(stringResource(R.string.enter_package)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { fetch() },
                enabled = !isLoading && !isMonitoring,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.show_threads))
            }
            Button(
                onClick = { isMonitoring = !isMonitoring },
                colors = if (isMonitoring)
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                else ButtonDefaults.buttonColors(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(Modifier.width(4.dp))
                Text(if (isMonitoring) "停止监控" else "实时监控")
            }
        }

        Spacer(Modifier.height(12.dp))

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
            SelectionContainer {
                Text(
                    text = output.ifBlank { stringResource(R.string.empty) },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}
