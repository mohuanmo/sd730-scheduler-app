package com.mohuanmo.sd730app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Memory
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.mohuanmo.sd730app.data.SchedulerRepository
import com.mohuanmo.sd730app.data.ShellExecutor
import com.mohuanmo.sd730app.ui.screen.StatusScreen
import com.mohuanmo.sd730app.ui.screen.ModeScreen
import com.mohuanmo.sd730app.ui.screen.ThreadsScreen
import com.mohuanmo.sd730app.ui.screen.LearningScreen
import com.mohuanmo.sd730app.ui.screen.SettingsScreen
import com.mohuanmo.sd730app.ui.theme.SD730AppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SD730AppTheme(darkTheme = true) {
                MainScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold() {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavItem(stringResource(R.string.nav_status), Icons.Default.Dashboard),
        NavItem(stringResource(R.string.nav_mode), Icons.Default.Speed),
        NavItem(stringResource(R.string.nav_threads), Icons.Default.Memory),
        NavItem(stringResource(R.string.nav_learning), Icons.Default.Psychology),
        NavItem(stringResource(R.string.nav_settings), Icons.Default.Settings),
    )

    var rootChecked by remember { mutableStateOf(false) }
    var hasRoot by remember { mutableStateOf(false) }
    var hasBinary by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val repo = SchedulerRepository()
        val (root, binary) = repo.checkEnvironment()
        hasRoot = root
        hasBinary = binary
        rootChecked = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                !rootChecked -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("检测环境中…")
                        }
                    }
                }
                !hasRoot -> {
                    NoRootScreen(onRetry = {
                        scope.launch {
                            rootChecked = false
                            val repo = SchedulerRepository()
                            val (root, binary) = repo.checkEnvironment()
                            hasRoot = root
                            hasBinary = binary
                            rootChecked = true
                        }
                    })
                }
                else -> {
                    when (selectedItem) {
                        0 -> StatusScreen()
                        1 -> ModeScreen()
                        2 -> ThreadsScreen()
                        3 -> LearningScreen()
                        4 -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun NoRootScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_root),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.no_root_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

private data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
