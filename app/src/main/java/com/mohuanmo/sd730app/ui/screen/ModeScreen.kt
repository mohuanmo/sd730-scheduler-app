package com.mohuanmo.sd730app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohuanmo.sd730app.R
import com.mohuanmo.sd730app.data.SchedulerRepository
import com.mohuanmo.sd730app.ui.theme.BalancedBlue
import com.mohuanmo.sd730app.ui.theme.PerformanceOrange
import com.mohuanmo.sd730app.ui.theme.PowersaveGreen
import com.mohuanmo.sd730app.ui.theme.UltraRed
import kotlinx.coroutines.launch

@Composable
fun ModeScreen(repository: SchedulerRepository = remember { SchedulerRepository() }) {
    val scope = rememberCoroutineScope()
    var currentMode by remember { mutableStateOf("balanced") }
    var currentScene by remember { mutableStateOf("auto") }
    var isLoading by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val modes = listOf(
        ModeItem("powersave", stringResource(R.string.powersave), R.string.mode_powersave_desc, PowersaveGreen, Icons.Default.Eco),
        ModeItem("balanced", stringResource(R.string.balanced), R.string.mode_balanced_desc, BalancedBlue, Icons.Default.Balance),
        ModeItem("performance", stringResource(R.string.performance), R.string.mode_performance_desc, PerformanceOrange, Icons.Default.RocketLaunch),
        ModeItem("ultra", stringResource(R.string.ultra), R.string.mode_ultra_desc, UltraRed, Icons.Default.Bolt)
    )

    val sceneModes = listOf(
        SceneItem("auto", stringResource(R.string.auto), "跟随系统/应用自动切换", Color.Gray, Icons.Default.AutoMode),
        SceneItem("powersave", stringResource(R.string.powersave), "强制省电模式", PowersaveGreen, Icons.Default.Eco),
        SceneItem("balanced", stringResource(R.string.balanced), "强制均衡模式", BalancedBlue, Icons.Default.Balance),
        SceneItem("performance", stringResource(R.string.performance), "强制性能模式", PerformanceOrange, Icons.Default.RocketLaunch),
        SceneItem("ultra", stringResource(R.string.ultra), "强制极速模式", UltraRed, Icons.Default.Bolt)
    )

    fun switchMode(mode: String) {
        scope.launch {
            isLoading = true
            val result = repository.setMode(mode)
            isLoading = false
            snackbarMessage = if (result.success) {
                currentMode = mode
                "已切换到 ${modes.first { it.key == mode }.label}"
            } else {
                "切换失败: ${result.stderr.ifBlank { "code ${result.exitCode}" }}"
            }
        }
    }

    fun switchScene(mode: String) {
        scope.launch {
            isLoading = true
            val result = repository.setSceneMode(mode)
            isLoading = false
            snackbarMessage = if (result.success) {
                currentScene = mode
                "Scene 已切换到 ${sceneModes.first { it.key == mode }.label}"
            } else {
                "Scene 切换失败: ${result.stderr.ifBlank { "code ${result.exitCode}" }}"
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
            text = stringResource(R.string.switch_mode),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))

        modes.forEach { mode ->
            val isSelected = currentMode == mode.key
            Card(
                onClick = { if (!isLoading) switchMode(mode.key) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) mode.color.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isSelected) ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(mode.color)
                ) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = mode.icon,
                        contentDescription = null,
                        tint = mode.color,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(mode.descRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (isSelected) {
                        Badge(containerColor = mode.color) {
                            Text("当前", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Scene Integration
        Text(
            text = stringResource(R.string.scene_integration),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "通过 Scene 框架切换模式（写入 scene_mode 配置）",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))

        sceneModes.forEach { scene ->
            val isSelected = currentScene == scene.key
            Card(
                onClick = { if (!isLoading) switchScene(scene.key) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) scene.color.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = scene.icon,
                        contentDescription = null,
                        tint = scene.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = scene.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = scene.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    if (isSelected) {
                        Badge(containerColor = scene.color) {
                            Text("当前", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
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

private data class ModeItem(
    val key: String,
    val label: String,
    val descRes: Int,
    val color: Color,
    val icon: ImageVector
)

private data class SceneItem(
    val key: String,
    val label: String,
    val desc: String,
    val color: Color,
    val icon: ImageVector
)
