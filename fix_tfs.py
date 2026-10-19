import re

for filepath in ['app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'app/src/main/java/com/adarsh/sift/ProcessActivity.kt']:
    with open(filepath, 'r') as f:
        content = f.read()

    # Add shape to TextFields if not already present
    content = re.sub(
        r'TextField\(\s*value',
        r'TextField(\n                    shape = MaterialTheme.shapes.extraSmall,\n                    value',
        content
    )
    
    # Also change TextField background colors if we want them to look standard? 
    # Usually standard TextField looks okay, but we can just add the shape constraint.
    with open(filepath, 'w') as f:
        f.write(content)
