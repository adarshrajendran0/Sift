import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

# I will add the DatePickerDialog logic.
date_picker_imports = """
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
"""

if "import androidx.compose.material3.DatePicker" not in content:
    content = content.replace("import androidx.compose.material3.MaterialTheme", date_picker_imports + "import androidx.compose.material3.MaterialTheme")

old_step_pickdate = """                Step.PickDate -> {
                    // Simulating date picker
                    Text("Pick a date", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.PickDateDone) }, modifier = Modifier.fillMaxWidth()) { Text("Set") }
                }"""

new_step_pickdate = """                Step.PickDate -> {
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

content = content.replace(old_step_pickdate, new_step_pickdate)

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
