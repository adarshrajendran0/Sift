import re

def update_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    # Apply typography consolidations
    content = content.replace('style = MaterialTheme.typography.bodySmall', 'style = MaterialTheme.typography.bodyMedium')
    content = content.replace('style = MaterialTheme.typography.titleSmall', 'style = MaterialTheme.typography.bodyMedium')
    content = content.replace('style = MaterialTheme.typography.bodyLarge', 'style = MaterialTheme.typography.titleMedium')
    
    # Update TopAppBars
    content = content.replace('Text("Inbox")', 'Text("Inbox", style = MaterialTheme.typography.titleMedium)')
    content = content.replace('Text("Settings")', 'Text("Settings", style = MaterialTheme.typography.titleMedium)')
    content = content.replace('Text("Lists")', 'Text("Lists", style = MaterialTheme.typography.titleMedium)')

    with open(filepath, 'w') as f:
        f.write(content)

update_file('app/src/main/java/com/adarsh/sift/MainActivity.kt')
update_file('app/src/main/java/com/adarsh/sift/ListsScreen.kt')
update_file('app/src/main/java/com/adarsh/sift/ProcessActivity.kt')

