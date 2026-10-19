import re

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'r') as f:
    content = f.read()

# I will add EditItemSheet at the bottom of the file
edit_sheet = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemSheet(
    item: com.adarsh.sift.data.Item,
    viewModel: InboxViewModel,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(item.actionText ?: item.rawText) }
    var selectedContextId by remember { mutableStateOf(item.contextId) }
    val contexts by viewModel.allContexts.collectAsStateWithLifecycle(emptyList())

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp).padding(bottom = 32.dp)) {
            Text("Edit Item", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                shape = MaterialTheme.shapes.extraSmall,
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("What needs to be done?") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (contexts.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contexts, key = { it.id }) { ctx ->
                        FilterChip(
                            selected = ctx.id == selectedContextId,
                            onClick = { 
                                selectedContextId = if (selectedContextId == ctx.id) null else ctx.id 
                            },
                            label = { Text(ctx.label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (item.status != com.adarsh.sift.data.Status.DONE) {
                    Button(
                        onClick = { 
                            viewModel.markAsDone(item)
                            onDismiss() 
                        }
                    ) { Text("Mark Done") }
                }
                Button(
                    onClick = { 
                        viewModel.updateItemDetails(item, text, selectedContextId)
                        onDismiss() 
                    }
                ) { Text("Save") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    viewModel.deleteItem(item)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Trash")
            }
        }
    }
}
"""

if "fun EditItemSheet" not in content:
    content = content + edit_sheet

# Modify the Lists to have editingItem state.
# We will do this by injecting the EditItemSheet invocation in ListsScreen, 
# and changing the click handlers. Wait, ListsScreen delegates to NextActionsList etc.
# We should probably pass an `onEdit: (Item) -> Unit` instead of `openProcess`.
# Let's see how many lists we have: NextActionsList, ProjectsList, WaitingForList, CalendarList, SimpleList.

content = content.replace("openProcess: (String) -> Unit", "onEdit: (com.adarsh.sift.data.Item) -> Unit")
content = content.replace("openProcess(item.id)", "onEdit(item)")
content = content.replace("openProcess(item)", "onEdit(item)")

# ListsScreen changes
old_open_process = """    val openProcess = { itemId: String ->
        val intent = Intent(context, ProcessActivity::class.java).apply {
            putExtra("item_id", itemId)
        }
        context.startActivity(intent)
    }"""
    
new_open_process = """    var editingItem by remember { mutableStateOf<com.adarsh.sift.data.Item?>(null) }
    val onEdit = { item: com.adarsh.sift.data.Item ->
        editingItem = item
    }"""
    
content = content.replace(old_open_process, new_open_process)

content = content.replace("0 -> NextActionsList(viewModel, openProcess)", "0 -> NextActionsList(viewModel, onEdit)")
content = content.replace("1 -> ProjectsList(viewModel, openProcess)", "1 -> ProjectsList(viewModel, onEdit)")
content = content.replace("2 -> WaitingForList(viewModel, openProcess)", "2 -> WaitingForList(viewModel, onEdit)")
content = content.replace("3 -> CalendarList(viewModel, openProcess)", "3 -> CalendarList(viewModel, onEdit)")
content = content.replace("4 -> SimpleList(viewModel, com.adarsh.sift.data.Status.SOMEDAY, openProcess)", "4 -> SimpleList(viewModel, com.adarsh.sift.data.Status.SOMEDAY, onEdit)")
content = content.replace("5 -> SimpleList(viewModel, com.adarsh.sift.data.Status.REFERENCE, openProcess)", "5 -> SimpleList(viewModel, com.adarsh.sift.data.Status.REFERENCE, onEdit)")

# Add EditItemSheet call in ListsScreen Box
box_content_old = """                }
                
                // Scroll edge fade"""
box_content_new = """                }
                
                if (editingItem != null) {
                    EditItemSheet(item = editingItem!!, viewModel = viewModel, onDismiss = { editingItem = null })
                }
                
                // Scroll edge fade"""
                
content = content.replace(box_content_old, box_content_new)

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'w') as f:
    f.write(content)
