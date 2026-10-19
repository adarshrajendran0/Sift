import re

with open('app/src/main/java/com/adarsh/sift/ManageScreens.kt', 'r') as f:
    content = f.read()

old_list_item_context = """                ListItem(
                    headlineContent = { Text(context.label) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteContext(context.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )"""

new_list_item_context = """                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(context.label, style = MaterialTheme.typography.bodyLarge)
                    IconButton(onClick = { viewModel.deleteContext(context.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }"""
                
old_list_item_project = """                ListItem(
                    headlineContent = { Text(project.title) }
                )"""

new_list_item_project = """                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(project.title, style = MaterialTheme.typography.bodyLarge)
                }"""

content = content.replace(old_list_item_context, new_list_item_context)
content = content.replace(old_list_item_project, new_list_item_project)
content = content.replace("HorizontalDivider()", "androidx.compose.material3.Divider()")

with open('app/src/main/java/com/adarsh/sift/ManageScreens.kt', 'w') as f:
    f.write(content)
