import re

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

imports_to_add = """
import androidx.compose.material3.ListItem
import androidx.compose.material3.HorizontalDivider
"""

content = content.replace('import androidx.compose.foundation.layout.padding', 'import androidx.compose.foundation.layout.padding' + imports_to_add)

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
