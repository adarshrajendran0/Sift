import re

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Snackbar text
content = content.replace('message = "Item deleted"', 'message = "Deleted"')

# 2. Process thoughts
content = content.replace('Text("Process (${items.size})")', 'Text("Process ${items.size} thoughts")')

# 3. Add Settings toggle for Haptics
haptics_setting = """                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                    Text("Haptic Feedback", style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Feel when capturing or processing items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                var hapticsEnabled by remember { mutableStateOf(prefs.getBoolean("haptics_enabled", true)) }
                                Switch(
                                    checked = hapticsEnabled,
                                    onCheckedChange = { enabled ->
                                        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
                                        hapticsEnabled = enabled
                                    }
                                )
                            }"""

# Insert it before the Ongoing Notification Setting
content = content.replace('                            // Ongoing Notification Setting', haptics_setting + '\n                            // Ongoing Notification Setting')

# 4. Add scroll edge fade to Inbox
old_inbox = """                            val groupedItems = items.groupBy { getDayHeader(it.createdAt) }
                            
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = 80.dp)
                            ) {"""
new_inbox = """                            val groupedItems = items.groupBy { getDayHeader(it.createdAt) }
                            
                            Box(modifier = Modifier.fillMaxSize()) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(top = paddingValues.calculateTopPadding(), bottom = 80.dp)
                                ) {"""
content = content.replace(old_inbox, new_inbox)

# Close the Box and add the gradient
old_inbox_end = """                                    }
                                }
                            }
                        }
                    }
                }
            }"""
new_inbox_end = """                                    }
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
            }"""
content = content.replace(old_inbox_end, new_inbox_end)

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
