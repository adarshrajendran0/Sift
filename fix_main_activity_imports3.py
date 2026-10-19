with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'r') as f:
    content = f.read()

if "import androidx.compose.material3.*" not in content:
    content = content.replace("import android.os.Bundle", "import android.os.Bundle\nimport androidx.compose.material3.*")

with open('app/src/main/java/com/adarsh/sift/MainActivity.kt', 'w') as f:
    f.write(content)
