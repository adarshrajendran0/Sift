with open('app/src/main/java/com/adarsh/sift/CaptureActivity.kt', 'r') as f:
    content = f.read()

# 1. Haptics in saveToDatabase
old_save = """    private fun saveToDatabase(text: String, src: Source) {
        if (text.isBlank()) return
        val app = application as SiftApplication
        app.applicationScope.launch {
            try {
                app.repository.insertItem(text, src)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }"""
new_save = """    private fun saveToDatabase(text: String, src: Source) {
        if (text.isBlank()) return
        val app = application as SiftApplication
        app.haptics.fire(Feel.Captured)
        app.applicationScope.launch {
            try {
                app.repository.insertItem(text, src)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }"""
content = content.replace(old_save, new_save)

# 2. Captured -> Captured
content = content.replace('placeholderText = "Captured ✓"', 'placeholderText = "Captured"')

# 3. Shape for TextField
old_tf = """                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
                        .focusRequester(focusRequester),
                    placeholder = { androidx.compose.material3.Text(placeholderText) },"""
new_tf = """                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
                        .focusRequester(focusRequester),
                    shape = MaterialTheme.shapes.extraSmall,
                    placeholder = { androidx.compose.material3.Text(placeholderText) },"""
content = content.replace(old_tf, new_tf)

# 4. TextField colors
old_colors = """                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )"""
new_colors = """                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )"""
content = content.replace(old_colors, new_colors)

with open('app/src/main/java/com/adarsh/sift/CaptureActivity.kt', 'w') as f:
    f.write(content)
