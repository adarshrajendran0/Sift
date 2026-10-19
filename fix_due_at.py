import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

# Add onDueAtChanged to ProcessScreen signature
old_sig = """fun ProcessScreen(
    state: ProcessSessionState,
    onAnswer: (Answer) -> Unit,
    onSkip: () -> Unit,
    onActionTextChanged: (String) -> Unit,
    onProjectNameChanged: (String) -> Unit,
    onDelegateToChanged: (String) -> Unit,
    onContextIdChanged: (String) -> Unit
) {"""

new_sig = """@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ProcessScreen(
    state: ProcessSessionState,
    onAnswer: (Answer) -> Unit,
    onSkip: () -> Unit,
    onActionTextChanged: (String) -> Unit,
    onProjectNameChanged: (String) -> Unit,
    onDelegateToChanged: (String) -> Unit,
    onDueAtChanged: (java.time.Instant?) -> Unit,
    onContextIdChanged: (String) -> Unit
) {"""

if "onDueAtChanged: (java.time.Instant?) -> Unit" not in content:
    content = content.replace(old_sig, new_sig)

# Add onDueAtChanged to ProcessScreen call in ProcessActivity
old_call = """                ProcessScreen(
                    state = state,
                    onAnswer = { viewModel.answer(it) },
                    onSkip = { viewModel.skipCurrent() },
                    onActionTextChanged = { viewModel.updateActionText(it) },
                    onProjectNameChanged = { viewModel.updateProjectName(it) },
                    onDelegateToChanged = { viewModel.updateDelegateTo(it) },
                    onContextIdChanged = { viewModel.updateContextId(it) }
                )"""
                
new_call = """                ProcessScreen(
                    state = state,
                    onAnswer = { viewModel.answer(it) },
                    onSkip = { viewModel.skipCurrent() },
                    onActionTextChanged = { viewModel.updateActionText(it) },
                    onProjectNameChanged = { viewModel.updateProjectName(it) },
                    onDelegateToChanged = { viewModel.updateDelegateTo(it) },
                    onDueAtChanged = { viewModel.updateDueAt(it) },
                    onContextIdChanged = { viewModel.updateContextId(it) }
                )"""
                
if "onDueAtChanged = { viewModel.updateDueAt(it) }" not in content:
    content = content.replace(old_call, new_call)

# Now we can safely add the date picker.
old_pickdate = """                Step.PickDate -> {
                    // Simulating date picker
                    Text("Pick a date", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { onAnswer(Answer.PickDateDone) }, modifier = Modifier.fillMaxWidth()) { Text("Set") }
                }"""

new_pickdate = """                Step.PickDate -> {
                    val datePickerState = androidx.compose.material3.rememberDatePickerState()
                    androidx.compose.material3.DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                onDueAtChanged(java.time.Instant.ofEpochMilli(selectedMillis))
                            }
                            handleAnswer(Answer.PickDateDone) 
                        }, 
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Set Date") }
                }"""

content = content.replace(old_pickdate, new_pickdate)

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
