package com.adarsh.sift

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContextsScreen(viewModel: InboxViewModel, onBack: () -> Unit) {
    val contexts by viewModel.allContexts.collectAsStateWithLifecycle(emptyList())
    var showAddContext by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contexts") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddContext = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Context")
            }
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            items(contexts, key = { it.id }) { context ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(context.label, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.deleteContext(context.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
                androidx.compose.material3.Divider()
            }
        }
        
        if (showAddContext) {
            var label by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddContext = false },
                title = { Text("Add Context") },
                text = {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Name") },
                        placeholder = { Text("@Home") }
                    )
                },
                confirmButton = {
                    Button(onClick = { 
                        if (label.isNotBlank()) {
                            viewModel.insertContext(label)
                        }
                        showAddContext = false 
                    }) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddContext = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(viewModel: InboxViewModel, onBack: () -> Unit) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle(emptyList())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues, modifier = Modifier.fillMaxSize()) {
            items(projects, key = { it.id }) { project ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(project.title, style = MaterialTheme.typography.bodyLarge)
                }
                androidx.compose.material3.Divider()
            }
        }
    }
}
