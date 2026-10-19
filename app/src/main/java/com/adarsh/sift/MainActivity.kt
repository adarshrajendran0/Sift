package com.adarsh.sift

import android.Manifest
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import androidx.compose.material3.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.HorizontalDivider

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adarsh.sift.data.Item
import com.adarsh.sift.ui.theme.SiftTheme
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class Screen { INBOX, SETTINGS, LISTS, CONTEXTS, PROJECTS }

class MainActivity : ComponentActivity() {

    private val viewModel: InboxViewModel by viewModels {
        InboxViewModelFactory((application as SiftApplication).repository)
    }

    private lateinit var prefs: SharedPreferences

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                prefs.edit().putBoolean("notification_enabled", true).apply()
                NotificationHelper.showNotification(this)
            }
        }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prefs = getSharedPreferences("sift_prefs", MODE_PRIVATE)

        setContent {
            SiftTheme {
                var currentScreen by remember { mutableStateOf(Screen.INBOX) }
                val items by viewModel.items.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                var notificationEnabled by remember { mutableStateOf(prefs.getBoolean("notification_enabled", false)) }
                var tileEnabled by remember {
                    val pm = packageManager
                    val compName = ComponentName(this@MainActivity, SiftTileService::class.java)
                    val state = pm.getComponentEnabledSetting(compName)
                    mutableStateOf(state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                }
                
                var editingItem by remember { mutableStateOf<Item?>(null) }
                
                if (editingItem != null) {
                    var text by remember { mutableStateOf(editingItem!!.rawText) }
                    ModalBottomSheet(onDismissRequest = { editingItem = null }) {
                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp, top = 8.dp)) {
                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                minLines = 3,
                                maxLines = 8,
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        val itemToDelete = editingItem!!
                                        editingItem = null
                                        viewModel.deleteItem(itemToDelete)
                                        coroutineScope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Deleted",
                                                actionLabel = "Undo"
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoDelete(itemToDelete.id)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Text("Delete")
                                }
                                Button(onClick = {
                                    if (text.isNotBlank() && text != editingItem!!.rawText) {
                                        viewModel.updateItem(editingItem!!.id, text)
                                    }
                                    editingItem = null
                                }) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }

                if (currentScreen == Screen.LISTS) {
                    BackHandler { currentScreen = Screen.INBOX }
                    ListsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.INBOX })
                } else if (currentScreen == Screen.SETTINGS) {
                    BackHandler { currentScreen = Screen.INBOX }
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Settings", style = MaterialTheme.typography.titleMedium) },
                                navigationIcon = {
                                    IconButton(onClick = { currentScreen = Screen.INBOX }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentScreen = Screen.CONTEXTS }
                                    .padding(16.dp)
                            ) {
                                Text("Manage Contexts", style = MaterialTheme.typography.titleMedium)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentScreen = Screen.PROJECTS }
                                    .padding(16.dp)
                            ) {
                                Text("Manage Projects", style = MaterialTheme.typography.titleMedium)
                            }
                            androidx.compose.material3.Divider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Haptic Feedback", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Feel when capturing or processing items", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                var hapticsEnabled by remember { mutableStateOf(prefs.getBoolean("haptics_enabled", true)) }
                                Switch(
                                    checked = hapticsEnabled,
                                    onCheckedChange = { enabled ->
                                        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
                                        hapticsEnabled = enabled
                                    }
                                )
                            }
                            // Ongoing Notification Setting
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Ongoing Notification", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Capture thoughts from the notification shade", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = notificationEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                                    prefs.edit().putBoolean("notification_enabled", true).apply()
                                                    notificationEnabled = true
                                                    NotificationHelper.showNotification(this@MainActivity)
                                                } else {
                                                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    notificationEnabled = true
                                                }
                                            } else {
                                                prefs.edit().putBoolean("notification_enabled", true).apply()
                                                notificationEnabled = true
                                                NotificationHelper.showNotification(this@MainActivity)
                                            }
                                        } else {
                                            prefs.edit().putBoolean("notification_enabled", false).apply()
                                            notificationEnabled = false
                                            NotificationHelper.cancelNotification(this@MainActivity)
                                        }
                                    }
                                )
                            }
                            
                            // Quick Settings Tile Setting
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Quick Settings Tile", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Add a capture button to your quick settings panel", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = tileEnabled,
                                    onCheckedChange = { enabled ->
                                        tileEnabled = enabled
                                        val compName = ComponentName(this@MainActivity, SiftTileService::class.java)
                                        if (enabled) {
                                            packageManager.setComponentEnabledSetting(
                                                compName,
                                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                PackageManager.DONT_KILL_APP
                                            )
                                            requestTileService()
                                        } else {
                                            packageManager.setComponentEnabledSetting(
                                                compName,
                                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                                PackageManager.DONT_KILL_APP
                                            )
                                        }
                                    }
                                )
                            }
                            // Context Management Setting
                            val contexts by viewModel.allContexts.collectAsStateWithLifecycle(emptyList())
                            var showContextDialog by remember { mutableStateOf<com.adarsh.sift.data.Context?>(null) }
                            var isAddingContext by remember { mutableStateOf(false) }

                            if (showContextDialog != null || isAddingContext) {
                                val currentContext = showContextDialog
                                var contextLabel by remember { mutableStateOf(currentContext?.label ?: "") }

                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = {
                                        showContextDialog = null
                                        isAddingContext = false
                                    },
                                    title = { Text(if (isAddingContext) "Add Context" else "Edit Context") },
                                    text = {
                                        Column {
                                            TextField(
                                                value = contextLabel,
                                                onValueChange = { contextLabel = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                placeholder = { Text("Context label (e.g., @work)") }
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            if (contextLabel.isNotBlank()) {
                                                if (isAddingContext) {
                                                    viewModel.insertContext(contextLabel)
                                                } else if (currentContext != null) {
                                                    viewModel.updateContext(currentContext.id, contextLabel)
                                                }
                                            }
                                            showContextDialog = null
                                            isAddingContext = false
                                        }) {
                                            Text("Save")
                                        }
                                    },
                                    dismissButton = {
                                        if (!isAddingContext && currentContext != null) {
                                            TextButton(
                                                onClick = {
                                                    viewModel.deleteContext(currentContext.id)
                                                    showContextDialog = null
                                                    isAddingContext = false
                                                },
                                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error
                                                )
                                            ) {
                                                Text("Delete")
                                            }
                                        } else {
                                            TextButton(onClick = {
                                                showContextDialog = null
                                                isAddingContext = false
                                            }) {
                                                Text("Cancel")
                                            }
                                        }
                                    }
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Manage Contexts", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Add, rename, or delete your contexts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(onClick = { isAddingContext = true }) {
                                    Text("Add")
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                                contexts.forEach { ctx ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showContextDialog = ctx }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(ctx.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Icon(
                                            androidx.compose.material.icons.Icons.Filled.Settings,
                                            contentDescription = "Edit Context",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (currentScreen == Screen.CONTEXTS) {
                    BackHandler { currentScreen = Screen.SETTINGS }
                    ContextsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.SETTINGS })
                } else if (currentScreen == Screen.PROJECTS) {
                    BackHandler { currentScreen = Screen.SETTINGS }
                    ProjectsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.SETTINGS })
                } else {
                    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            LargeTopAppBar(
                                title = { 
                                    Column {
                                        Text("Inbox", style = MaterialTheme.typography.titleMedium)
                                        Text("${items.size} thoughts", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { currentScreen = Screen.LISTS }) {
                                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Lists")
                                    }
                                    IconButton(onClick = { currentScreen = Screen.SETTINGS }) {
                                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                                    }
                                },
                                scrollBehavior = scrollBehavior
                            )
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = {
                                val intent = Intent(this@MainActivity, CaptureActivity::class.java)
                                startActivity(intent)
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Capture")
                            }
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { paddingValues ->
                        if (items.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(32.dp), 
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Your mind is clear.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Add the Sift widget to your home screen to capture without opening the app.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        } else {
                            val groupedItems = items.groupBy { getDayHeader(it.createdAt) }
                            
                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = 80.dp)
                                ) {
                                item {
                                    Button(
                                        onClick = { startActivity(Intent(this@MainActivity, ProcessActivity::class.java)) },
                                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                                    ) {
                                        Text("Process ${items.size} thoughts")
                                    }
                                }
                                groupedItems.forEach { (header, itemsForDay) ->
                                    stickyHeader {
                                        Text(
                                            text = header,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                    items(itemsForDay, key = { it.id }) { item ->
                                        ItemRow(item = item, onClick = { editingItem = item })
                                    }
                                }
                                }
                                // Scroll edge fade
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                                colors = listOf(
                                                    androidx.compose.ui.graphics.Color.Transparent,
                                                    MaterialTheme.colorScheme.surface
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestTileService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarManager = getSystemService(StatusBarManager::class.java)
            statusBarManager.requestAddTileService(
                ComponentName(this, SiftTileService::class.java),
                getString(R.string.add_tile),
                Icon.createWithResource(this, R.drawable.ic_launcher_foreground),
                { },
                { }
            )
        }
    }
}

@Composable
fun ItemRow(item: Item, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = item.rawText, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = formatRelativeTime(item.createdAt), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun getDayHeader(instant: Instant): String {
    val zone = ZoneId.systemDefault()
    val itemDate = instant.atZone(zone).toLocalDate()
    val today = Instant.now().atZone(zone).toLocalDate()
    
    val daysBetween = ChronoUnit.DAYS.between(itemDate, today)
    return when (daysBetween) {
        0L -> "Today"
        1L -> "Yesterday"
        else -> itemDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

fun formatRelativeTime(instant: Instant): String {
    val duration = Duration.between(instant, Instant.now())
    return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toHours() < 1 -> "${duration.toMinutes()}m ago"
        duration.toDays() < 1 -> "${duration.toHours()}h ago"
        else -> "${duration.toDays()}d ago"
    }
}
