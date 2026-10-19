import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

# Add OptIn to ProcessScreen
content = content.replace(
    '@Composable\nfun ProcessScreen(',
    '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun ProcessScreen('
)

# Fix onDueAtChanged
# `ProcessScreen` currently has:
# fun ProcessScreen(
#     state: ProcessSessionState,
#     onActionTextChanged: (String) -> Unit,
#     ...
# I need to find its signature.
