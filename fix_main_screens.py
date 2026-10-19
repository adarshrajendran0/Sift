import re

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

if "else if (currentScreen == Screen.CONTEXTS)" not in content:
    old_else = "} else {"
    new_else = "} else if (currentScreen == Screen.CONTEXTS) {\n                    BackHandler { currentScreen = Screen.SETTINGS }\n                    ContextsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.SETTINGS })\n                } else if (currentScreen == Screen.PROJECTS) {\n                    BackHandler { currentScreen = Screen.SETTINGS }\n                    ProjectsScreen(viewModel = viewModel, onBack = { currentScreen = Screen.SETTINGS })\n                } else {"
    # We want to replace the LAST '} else {' that comes before the INBOX rendering
    # Wait, the INBOX rendering starts with 'val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()'
    
    parts = content.split("val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()")
    if len(parts) == 2:
        part1 = parts[0]
        if part1.strip().endswith("} else {"):
            part1 = part1[:part1.rfind("} else {")] + new_else + "\n                    "
            content = part1 + "val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()" + parts[1]

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
