import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

bad_pickdate = """                Step.PickDate -> {
                    val datePickerState = rememberDatePickerState()
                    DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                onDueAtChanged(Instant.ofEpochMilli(selectedMillis))
                            }
                            handleAnswer(Answer.PickDateDone) 
                        }, 
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Set Date") }
                }"""

good_pickdate = """                Step.PickDate -> {
                    // Simulating date picker
                    Text("Pick a date", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { onAnswer(Answer.PickDateDone) }, modifier = Modifier.fillMaxWidth()) { Text("Set") }
                }"""

content = content.replace(bad_pickdate, good_pickdate)

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
