import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

completion_old = """fun CompletionScreen(
    total: Int,
    processed: Int,
    done: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inbox empty", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("$processed processed · $done done on the spot", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)"""
completion_new = """fun CompletionScreen(
    total: Int,
    processed: Int,
    done: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onFinish: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        val app = context.applicationContext as SiftApplication
        app.haptics.fire(Feel.InboxEmpty)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Inbox empty", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("$processed processed · $done done on the spot", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)"""

content = content.replace(completion_old, completion_new)

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
