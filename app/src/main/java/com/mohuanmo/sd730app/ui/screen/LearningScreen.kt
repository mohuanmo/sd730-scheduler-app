package com.mohuanmo.sd730app.ui.screen

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import kotlinx.coroutines.launch

// Data classes for parsed tables
data class TableRow(val cells: List<String>)
data class ParsedTable(val title: String, val headers: List<String>, val rows: List<TableRow>)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LearningScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.learning_stats),
        stringResource(R.string.mode_learning),
        stringResource(R.string.prediction_stats),
        stringResource(R.string.tpin_status)
    )
    var output by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Parsed tpin fields
    var tpinEnabled by remember { mutableStateOf("—") }
    var tpinActiveModes by remember { mutableStateOf("—") }
    var tpinBudget by remember { mutableStateOf("—") }
    var tpinThresholds by remember { mutableStateOf("—") }
    var tpinEscalation by remember { mutableStateOf("—") }
    var tpinThermal by remember { mutableStateOf("—") }
    var tpinSelfPin by remember { mutableStateOf("—") }
    var tpinTrackedApp by remember { mutableStateOf("—") }
    var tpinPinned by remember { mutableStateOf("—") }
    var tpinCorrelationEnabled by remember { mutableStateOf("—") }
    var tpinCorrelationBoost by remember { mutableStateOf("—") }
    var tpinSelfMgmtEnabled by remember { mutableStateOf("—") }
    var tpinSelfMgmtSession by remember { mutableStateOf("—") }
    var tpinVerdicts by remember { mutableStateOf(listOf<String>()) }
    var threadNamesTable by remember { mutableStateOf<ParsedTable?>(null) }
    var correlationsTable by remember { mutableStateOf<ParsedTable?>(null) }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            val result = when (selectedTab) {
                0 -> repository.getStats()
                1 -> repository.getModeLearningStats()
                2 -> repository.getPredictionStats()
                3 -> repository.getTpinStatus()
                else -> repository.getStats()
            }
            isLoading = false

            output = result.stdout

            if (selectedTab == 3) {
                // Parse tpin-status output
                val lines = result.stdout.lineSequence().toList()
                val newThreadNamesTable = parseTable(lines, "Learned thread names:")
                val newCorrelationsTable = parseTable(lines, "Learned correlations:")
                threadNamesTable = newThreadNamesTable
                correlationsTable = newCorrelationsTable

                lines.forEach { line ->
                    when {
                        line.startsWith("Enabled:") && !line.contains("Prediction") && !line.contains("Self-Management") ->
                            tpinEnabled = line.substringAfter(":").trim()
                        line.startsWith("Active in modes:") ->
                            tpinActiveModes = line.substringAfter(":").trim()
                        line.startsWith("Budget:") ->
                            tpinBudget = line.substringAfter(":").trim()
                        line.startsWith("Thresholds:") ->
                            tpinThresholds = line.substringAfter(":").trim()
                        line.startsWith("Escalation:") ->
                            tpinEscalation = line.substringAfter(":").trim()
                        line.startsWith("Thermal release:") ->
                            tpinThermal = line.substringAfter(":").trim()
                        line.startsWith("Self-pin (daemon):") ->
                            tpinSelfPin = line.substringAfter(":").trim()
                        line.startsWith("Tracked app:") ->
                            tpinTrackedApp = line.substringAfter(":").trim()
                        line.startsWith("Pinned threads:") ->
                            tpinPinned = line.substringAfter(":").trim()
                        line.startsWith("Active correlation boosts:") ->
                            tpinCorrelationBoost = line.substringAfter(":").trim()
                        line.contains("Thread Correlation Prediction") -> {
                            // Look ahead for Enabled line
                            val idx = lines.indexOf(line)
                            if (idx >= 0 && idx + 1 < lines.size && lines[idx + 1].startsWith("Enabled:")) {
                                tpinCorrelationEnabled = lines[idx + 1].substringAfter(":").trim()
                            }
                        }
                        line.contains("Self-Management Detection") -> {
                            val idx = lines.indexOf(line)
                            if (idx >= 0) {
                                if (idx + 1 < lines.size && lines[idx + 1].startsWith("Enabled:")) {
                                    tpinSelfMgmtEnabled = lines[idx + 1].substringAfter(":").trim()
                                }
                                if (idx + 2 < lines.size && lines[idx + 2].startsWith("Session:")) {
                                    tpinSelfMgmtSession = lines[idx + 2].substringAfter(":").trim()
                                }
                            }
                        }
                    }
                }

                // Parse verdicts
                val verdictLines = mutableListOf<String>()
                var inVerdicts = false
                lines.forEach { line ->
                    if (line.startsWith("Verdicts")) {
                        inVerdicts = true
                    } else if (inVerdicts && line.isNotBlank() && !line.startsWith("===")) {
                        verdictLines.add(line.trim())
                    } else if (inVerdicts && (line.startsWith("===") || line.isBlank())) {
                        inVerdicts = false
                    }
                }
                tpinVerdicts = verdictLines
            }

            if (!result.success) {
                error = result.stderr.ifBlank { "Exit: ${result.exitCode}" }
            }
        }
    }

    LaunchedEffect(selectedTab) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.learning_stats),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { load() }, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
            }
        }

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

        if (selectedTab == 3) {
            // TPin Status view with parsed tables
            TpinStatusView(
                tpinEnabled, tpinActiveModes, tpinBudget, tpinThresholds,
                tpinEscalation, tpinThermal, tpinSelfPin, tpinTrackedApp,
                tpinPinned, tpinCorrelationEnabled, tpinCorrelationBoost,
                tpinSelfMgmtEnabled, tpinSelfMgmtSession, tpinVerdicts,
                threadNamesTable, correlationsTable, output
            )
        } else {
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
}

@Composable
private fun TpinStatusView(
    enabled: String, activeModes: String, budget: String, thresholds: String,
    escalation: String, thermal: String, selfPin: String, trackedApp: String,
    pinned: String, corrEnabled: String, corrBoost: String,
    selfMgmtEnabled: String, selfMgmtSession: String, verdicts: List<String>,
    threadNamesTable: ParsedTable?, correlationsTable: ParsedTable?, rawOutput: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Engine status cards
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 2
        ) {
            TpinInfoCard(stringResource(R.string.thread_pin), enabled)
            TpinInfoCard(stringResource(R.string.tracked_app), trackedApp)
            TpinInfoCard(stringResource(R.string.active_modes), activeModes)
            TpinInfoCard(stringResource(R.string.budget), budget)
            TpinInfoCard(stringResource(R.string.pinned_threads), pinned)
            TpinInfoCard(stringResource(R.string.self_pin), selfPin)
        }

        Spacer(Modifier.height(12.dp))

        // Detailed info
        TpinInfoCard(stringResource(R.string.thresholds), thresholds)
        Spacer(Modifier.height(4.dp))
        TpinInfoCard(stringResource(R.string.escalation), escalation)
        Spacer(Modifier.height(4.dp))
        TpinInfoCard(stringResource(R.string.thermal_release), thermal)

        Spacer(Modifier.height(16.dp))

        // Thread Correlation Prediction
        Text(
            text = stringResource(R.string.thread_correlation),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        TpinInfoCard("Enabled", corrEnabled)
        Spacer(Modifier.height(4.dp))
        TpinInfoCard("Boost", corrBoost)
        Spacer(Modifier.height(4.dp))
        TpinInfoCard("Active boosts", corrBoost)

        Spacer(Modifier.height(16.dp))

        // Learned thread names table
        if (threadNamesTable != null) {
            Text(
                text = stringResource(R.string.learned_thread_names),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            DataTable(table = threadNamesTable)
            Spacer(Modifier.height(16.dp))
        }

        // Learned correlations table
        if (correlationsTable != null) {
            Text(
                text = stringResource(R.string.learned_correlations),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            DataTable(table = correlationsTable)
            Spacer(Modifier.height(16.dp))
        }

        // Self-Management
        Text(
            text = stringResource(R.string.self_management),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        TpinInfoCard("Enabled", selfMgmtEnabled)
        Spacer(Modifier.height(4.dp))
        TpinInfoCard("Session", selfMgmtSession)

        if (verdicts.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.verdicts),
                style = MaterialTheme.typography.labelLarge
            )
            verdicts.forEach { v ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = v,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

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
                    text = rawOutput.ifBlank { stringResource(R.string.empty) },
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun TpinInfoCard(label: String, value: String) {
    Card(
        modifier = Modifier.widthIn(min = 140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DataTable(table: ParsedTable) {
    if (table.headers.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                table.headers.forEach { header ->
                    Text(
                        text = header.trim(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            // Rows
            if (table.rows.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                table.rows.forEachIndexed { index, row ->
                    val bgColor = if (index % 2 == 0)
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        table.headers.indices.forEach { colIndex ->
                            val cellValue = row.cells.getOrNull(colIndex) ?: ""
                            Text(
                                text = cellValue.trim(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseTable(lines: List<String>, sectionPrefix: String): ParsedTable? {
    val sectionIdx = lines.indexOfFirst { it.startsWith("=== $sectionPrefix") }
    if (sectionIdx < 0 || sectionIdx + 1 >= lines.size) return null

    val title = lines[sectionIdx].removePrefix("=== ").removeSuffix(" ===").trim()

    // Find header line (skip empty lines)
    var headerIdx = sectionIdx + 1
    while (headerIdx < lines.size && lines[headerIdx].isBlank()) headerIdx++
    if (headerIdx >= lines.size) return null

    val headerLine = lines[headerIdx]
    if (!headerLine.contains("|")) return null

    val headers = headerLine.split("|").map { it.trim() }.filter { it.isNotBlank() }
    if (headers.isEmpty()) return null

    // Parse data rows
    val rows = mutableListOf<TableRow>()
    var rowIdx = headerIdx + 1
    while (rowIdx < lines.size) {
        val line = lines[rowIdx]
        if (line.isBlank() || line.startsWith("===") || line.startsWith("Active") || line.startsWith("Boost:")) break

        if (line.contains("|")) {
            val cells = line.split("|").map { it.trim() }.filter { it.isNotBlank() }
            if (cells.isNotEmpty()) {
                rows.add(TableRow(cells))
            }
        } else {
            // Single value line -> first column only
            rows.add(TableRow(listOf(line.trim())))
        }
        rowIdx++
    }

    return ParsedTable(title, headers, rows)
}
