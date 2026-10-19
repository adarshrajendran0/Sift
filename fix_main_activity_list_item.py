import re

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

old_contexts = """                            ListItem(
                                headlineContent = { Text("Manage Contexts") },
                                modifier = Modifier.clickable { currentScreen = Screen.CONTEXTS }
                            )"""
                            
new_contexts = """                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentScreen = Screen.CONTEXTS }
                                    .padding(16.dp)
                            ) {
                                Text("Manage Contexts", style = MaterialTheme.typography.titleMedium)
                            }"""

old_projects = """                            ListItem(
                                headlineContent = { Text("Manage Projects") },
                                modifier = Modifier.clickable { currentScreen = Screen.PROJECTS }
                            )"""

new_projects = """                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentScreen = Screen.PROJECTS }
                                    .padding(16.dp)
                            ) {
                                Text("Manage Projects", style = MaterialTheme.typography.titleMedium)
                            }"""

content = content.replace(old_contexts, new_contexts)
content = content.replace(old_projects, new_projects)
content = content.replace("HorizontalDivider()", "androidx.compose.material3.Divider()")

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
