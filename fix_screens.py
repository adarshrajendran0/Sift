import re

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'enum class Screen { INBOX, SETTINGS, LISTS }',
    'enum class Screen { INBOX, SETTINGS, LISTS, CONTEXTS, PROJECTS }'
)

# And add the screens to the when block
old_when = """                    when (currentScreen) {
                        Screen.INBOX -> {
                            InboxScreen(
                                items = items.filter { it.status == Status.INBOX && it.skipCount < 2 },
                                stalledItems = items.filter { it.status == Status.INBOX && it.skipCount >= 2 },
                                onProcessAll = {
                                    val intent = Intent(this@MainActivity, ProcessActivity::class.java)
                                    startActivity(intent)
                                },
                                onEditItem = { editingItem = it },
                                onTrashItem = { item ->
                                    viewModel.deleteItem(item)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete(item.id)
                                        }
                                    }
                                }
                            )
                        }
                        Screen.LISTS -> {
                            ListsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.INBOX })
                        }"""

new_when = old_when + """
                        Screen.CONTEXTS -> {
                            ContextsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.SETTINGS })
                        }
                        Screen.PROJECTS -> {
                            ProjectsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.SETTINGS })
                        }"""
                        
content = content.replace(old_when, new_when)

# In Settings screen, add links to Contexts and Projects
# "Haptic Feedback" is there, we can add Contexts and Projects just above it.
old_settings = """                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Haptic Feedback", style = MaterialTheme.typography.titleMedium)"""
                                    
new_settings = """                            ListItem(
                                headlineContent = { Text("Manage Contexts") },
                                modifier = Modifier.clickable { currentScreen = Screen.CONTEXTS }
                            )
                            ListItem(
                                headlineContent = { Text("Manage Projects") },
                                modifier = Modifier.clickable { currentScreen = Screen.PROJECTS }
                            )
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Haptic Feedback", style = MaterialTheme.typography.titleMedium)"""
                                    
content = content.replace(old_settings, new_settings)

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
