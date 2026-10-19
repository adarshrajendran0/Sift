import os

content = """package com.adarsh.sift

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adarsh.sift.data.Context
import com.adarsh.sift.data.Item
import com.adarsh.sift.data.Project
import com.adarsh.sift.data.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(viewModel: InboxViewModel, onBack: () -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Next Actions", "Projects", "Waiting For", "Calendar", "Someday", "Reference")
    val context = LocalContext.current
    
    val openProcess = { itemId: String ->
        val intent = Intent(context, ProcessActivity::class.java).apply {
            putExtra("item_id", itemId)
        }
        context.startActivity(intent)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Lists") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> NextActionsList(viewModel, openProcess)
                    1 -> ProjectsList(viewModel, openProcess)
                    2 -> WaitingForList(viewModel, openProcess)
                    3 -> CalendarList(viewModel, openProcess)
                    4 -> SimpleList(viewModel, Status.SOMEDAY, openProcess)
                    5 -> SimpleList(viewModel, Status.REFERENCE, openProcess)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NextActionsList(viewModel: InboxViewModel, openProcess: (String) -> Unit) {
    val items by viewModel.itemsByStatus(Status.NEXT_ACTION).collectAsStateWithLifecycle(emptyList())
    val contexts by viewModel.allContexts.collectAsStateWithLifecycle(emptyList())
    
    if (items.isEmpty()) {
        EmptyState("No next actions")
        return
    }
    
    val contextMap = contexts.associateBy { it.id }
    val grouped = items.groupBy { it.contextId }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        grouped.forEach { (contextId, contextItems) ->
            stickyHeader {
                val label = contextMap[contextId]?.label ?: "No Context"
                ListHeader(label)
            }
            items(contextItems, key = { it.id }) { item ->
                ItemRow(item = item, onClick = { openProcess(item.id) })
            }
        }
    }
}

@Composable
fun ProjectsList(viewModel: InboxViewModel, openProcess: (String) -> Unit) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle(emptyList())
    
    if (projects.isEmpty()) {
        EmptyState("No active projects")
        return
    }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(projects, key = { it.id }) { project ->
            ProjectRow(project, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectRow(project: Project, viewModel: InboxViewModel) {
    val count by viewModel.openActionCount(project.id).collectAsStateWithLifecycle(0)
    var showAddAction by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = project.title, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                if (count > 0) {
                    Text(text = "$count open next action(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(text = "No next action", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
            }
            TextButton(onClick = { showAddAction = true }) {
                Text("Add next action")
            }
        }
    }

    if (showAddAction) {
        var actionText by remember { mutableStateOf("") }
        var selectedContextId by remember { mutableStateOf<String?>(null) }
        val contexts by viewModel.allContexts.collectAsStateWithLifecycle(emptyList())

        ModalBottomSheet(onDismissRequest = { showAddAction = false }) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
                Text("Add Next Action", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = actionText,
                    onValueChange = { actionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What needs to be done?") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Context", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contexts) { ctx ->
                        val isSelected = selectedContextId == ctx.id
                        Button(
                            onClick = { selectedContextId = ctx.id },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(ctx.label)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (actionText.isNotBlank()) {
                            viewModel.addNextActionToProject(project.id, actionText, selectedContextId)
                            showAddAction = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = actionText.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        }
    }
}

@Composable
fun WaitingForList(viewModel: InboxViewModel, openProcess: (String) -> Unit) {
    val items by viewModel.itemsByStatus(Status.WAITING_FOR).collectAsStateWithLifecycle(emptyList())
    
    if (items.isEmpty()) {
        EmptyState("Not waiting on anyone")
        return
    }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(items, key = { it.id }) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { openProcess(item.id) }
                    .padding(16.dp)
            ) {
                Text(text = item.actionText ?: item.rawText, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                val delegate = item.delegateTo ?: "Unknown"
                val time = formatRelativeTime(item.processedAt ?: item.createdAt)
                Text(text = "$delegate · $time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarList(viewModel: InboxViewModel, openProcess: (String) -> Unit) {
    val items by viewModel.itemsByStatus(Status.CALENDAR).collectAsStateWithLifecycle(emptyList())
    
    if (items.isEmpty()) {
        EmptyState("Nothing scheduled")
        return
    }
    
    val grouped = items.groupBy { 
        it.dueAt?.let { at -> getDayHeader(at) } ?: "Unscheduled" 
    }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        grouped.forEach { (header, itemsForDay) ->
            stickyHeader { ListHeader(header) }
            items(itemsForDay, key = { it.id }) { item ->
                ItemRow(item = item, onClick = { openProcess(item.id) })
            }
        }
    }
}

@Composable
fun SimpleList(viewModel: InboxViewModel, status: Status, openProcess: (String) -> Unit) {
    val items by viewModel.itemsByStatus(status).collectAsStateWithLifecycle(emptyList())
    
    if (items.isEmpty()) {
        EmptyState("Empty list")
        return
    }
    
    LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
        items(items, key = { it.id }) { item ->
            ItemRow(item = item, onClick = { openProcess(item.id) })
        }
    }
}

@Composable
fun ListHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text(text = item.actionText ?: item.rawText, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        val time = formatRelativeTime(item.processedAt ?: item.createdAt)
        Text(text = time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun formatRelativeTime(instant: Instant): String {
    // A simple formatter for now
    val duration = java.time.Duration.between(instant, Instant.now())
    return when {
        duration.toDays() > 0 -> "${duration.toDays()}d ago"
        duration.toHours() > 0 -> "${duration.toHours()}h ago"
        duration.toMinutes() > 0 -> "${duration.toMinutes()}m ago"
        else -> "Just now"
    }
}

fun getDayHeader(instant: Instant): String {
    val localDate = java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()).toLocalDate()
    val today = java.time.LocalDate.now()
    return when (localDate) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        else -> localDate.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }
}
"""

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'w') as f:
    f.write(content)
