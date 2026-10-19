import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

# Copy updates
content = content.replace('"This one keeps getting skipped. Park it?"', '"Still not ready to decide? Park it for later."')
content = content.replace('"Is it actionable?"', '"Is there something to do here?"')
content = content.replace('"Someday / Maybe"', '"Someday"')
content = content.replace('"Project name..."', '"Project name…"')
content = content.replace('"start with a verb"', '"Call Peter about the contract"')
content = content.replace('"Schedule..."', '"Schedule…"')
content = content.replace('"Delegate..."', '"Delegate…"')

# Typography updates
content = content.replace('style = MaterialTheme.typography.displayMedium', 'style = MaterialTheme.typography.headlineSmall')
content = content.replace('style = MaterialTheme.typography.bodyLarge', 'style = MaterialTheme.typography.headlineSmall')
# But wait, there is a bodyLarge for the main item text. Let's see:
# Text(state.item.rawText, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
# I should change that to headlineSmall.
content = content.replace('Text(state.item.rawText, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)', 'Text(state.item.rawText, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)')

# The timer in DoingNow is displayMedium. Prompt: Subject = headlineSmall (processing item text, the timer, "Inbox empty")
# I'll let displayMedium become headlineSmall. Wait, the prompt says "the timer".
content = content.replace('Text(timeString, style = MaterialTheme.typography.displayMedium', 'Text(timeString, style = MaterialTheme.typography.headlineSmall')

# Replace buttons in Actionable
actionable_old = """                    Button(onClick = { handleAnswer(Answer.Yes) }, modifier = Modifier.fillMaxWidth()) { Text("Yes") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.No) }, modifier = Modifier.fillMaxWidth()) { Text("No") }"""
actionable_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.Yes) }, modifier = Modifier.fillMaxWidth()) { Text("Yes") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.No) }, modifier = Modifier.fillMaxWidth()) { Text("No") }"""
content = content.replace(actionable_old, actionable_new)

# WorthKeeping
worthkeeping_old = """                    Button(onClick = { handleAnswer(Answer.Keep) }, modifier = Modifier.fillMaxWidth()) { Text("Keep") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Trash) }, modifier = Modifier.fillMaxWidth()) { Text("Trash") }"""
worthkeeping_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.Keep) }, modifier = Modifier.fillMaxWidth()) { Text("Keep") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.Trash) }, modifier = Modifier.fillMaxWidth()) { Text("Trash") }"""
content = content.replace(worthkeeping_old, worthkeeping_new)

# NeedsActionLater
needs_old = """                    Button(onClick = { handleAnswer(Answer.Someday) }, modifier = Modifier.fillMaxWidth()) { Text("Someday") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Reference) }, modifier = Modifier.fillMaxWidth()) { Text("Just reference") }"""
needs_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.Someday) }, modifier = Modifier.fillMaxWidth()) { Text("Someday") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.Reference) }, modifier = Modifier.fillMaxWidth()) { Text("Just reference") }"""
content = content.replace(needs_old, needs_new)

# SingleOrProject
single_old = """                    Button(onClick = { handleAnswer(Answer.SingleAction) }, modifier = Modifier.fillMaxWidth()) { Text("Single action") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Project) }, modifier = Modifier.fillMaxWidth()) { Text("It's a project") }"""
single_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.SingleAction) }, modifier = Modifier.fillMaxWidth()) { Text("Single action") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.Project) }, modifier = Modifier.fillMaxWidth()) { Text("It's a project") }"""
content = content.replace(single_old, single_new)

# Clarify
clarify_old = """                    Button(onClick = { handleAnswer(Answer.DoItNow) }, modifier = Modifier.fillMaxWidth()) { Text("Do it now") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Later) }, modifier = Modifier.fillMaxWidth()) { Text("Later") }"""
clarify_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.DoItNow) }, modifier = Modifier.fillMaxWidth()) { Text("Do it now") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.Later) }, modifier = Modifier.fillMaxWidth()) { Text("Later") }"""
content = content.replace(clarify_old, clarify_new)

# DoingNow
doing_old = """                    Button(onClick = { handleAnswer(Answer.Done) }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.NeedMoreTime) }, modifier = Modifier.fillMaxWidth()) { Text("Need more time") }"""
doing_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.Done) }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.NeedMoreTime) }, modifier = Modifier.fillMaxWidth()) { Text("Need more time") }"""
content = content.replace(doing_old, doing_new)

# StalledNudge
stalled_old = """                    Button(onClick = { handleAnswer(Answer.Someday) }, modifier = Modifier.fillMaxWidth()) { Text("Someday") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.KeepTrying) }, modifier = Modifier.fillMaxWidth()) { Text("Keep trying") }"""
stalled_new = """                    FilledTonalButton(onClick = { handleAnswer(Answer.Someday) }, modifier = Modifier.fillMaxWidth()) { Text("Someday") }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(onClick = { handleAnswer(Answer.KeepTrying) }, modifier = Modifier.fillMaxWidth()) { Text("Keep trying") }"""
content = content.replace(stalled_old, stalled_new)

# Destination
dest_old = """                    OutlinedButton(onClick = { handleAnswer(Answer.Schedule) }, modifier = Modifier.fillMaxWidth()) { Text("Schedule…") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Delegate) }, modifier = Modifier.fillMaxWidth()) { Text("Delegate…") }"""
dest_new = """                    TextButton(onClick = { handleAnswer(Answer.Schedule) }, modifier = Modifier.fillMaxWidth()) { Text("Schedule…") }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { handleAnswer(Answer.Delegate) }, modifier = Modifier.fillMaxWidth()) { Text("Delegate…") }"""
content = content.replace(dest_old, dest_new)

# Add FilledTonalButton to imports
if 'import androidx.compose.material3.FilledTonalButton' not in content:
    content = content.replace('import androidx.compose.material3.Button', 'import androidx.compose.material3.Button\nimport androidx.compose.material3.FilledTonalButton')

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)

