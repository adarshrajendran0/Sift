import re

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.material3.*', 'import androidx.compose.material3.*\nimport androidx.compose.material3.ListItem\nimport androidx.compose.material3.HorizontalDivider')

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
