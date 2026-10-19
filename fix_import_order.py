with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'const val ENABLE_CLARIFY_STEP = true\nimport androidx.compose.material3.ExperimentalMaterial3Api',
    'import androidx.compose.material3.ExperimentalMaterial3Api\nconst val ENABLE_CLARIFY_STEP = true'
)

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
