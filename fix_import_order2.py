import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

# Just strip the import from where it is and put it near the top
content = content.replace("import androidx.compose.material3.ExperimentalMaterial3Api", "")

content = content.replace("import android.os.Bundle", "import android.os.Bundle\nimport androidx.compose.material3.ExperimentalMaterial3Api\n")

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
