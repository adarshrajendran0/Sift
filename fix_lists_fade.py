import re

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'r') as f:
    content = f.read()

old_box = """            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> NextActionsList(viewModel, openProcess)
                    1 -> ProjectsList(viewModel, openProcess)
                    2 -> WaitingForList(viewModel, openProcess)
                    3 -> CalendarList(viewModel, openProcess)
                    4 -> SimpleList(viewModel, Status.SOMEDAY, openProcess)
                    5 -> SimpleList(viewModel, Status.REFERENCE, openProcess)
                }
            }"""
new_box = """            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> NextActionsList(viewModel, openProcess)
                    1 -> ProjectsList(viewModel, openProcess)
                    2 -> WaitingForList(viewModel, openProcess)
                    3 -> CalendarList(viewModel, openProcess)
                    4 -> SimpleList(viewModel, Status.SOMEDAY, openProcess)
                    5 -> SimpleList(viewModel, Status.REFERENCE, openProcess)
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
            }"""
content = content.replace(old_box, new_box)

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'w') as f:
    f.write(content)
