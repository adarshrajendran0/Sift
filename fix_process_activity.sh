#!/bin/bash
cat << 'INNER_EOF' > app/src/main/java/com/adarsh/sift/ProcessActivity.kt
package com.adarsh.sift

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adarsh.sift.domain.Answer
import com.adarsh.sift.domain.Step
import com.adarsh.sift.ui.theme.SiftTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val ENABLE_CLARIFY_STEP = true

class ProcessActivity : ComponentActivity() {

    private val viewModel: ProcessViewModel by viewModels {
        val app = application as SiftApplication
        ProcessViewModelFactory(app.repository, app.applicationScope, intent.getStringExtra("item_id"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SiftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(state.lastResolvedItem) {
                        if (state.lastResolvedItem != null) {
                            val statusName = state.lastResolvedStatus?.name?.replace("_", " ") ?: ""
                            val result = snackbarHostState.showSnackbar(
                                message = "Filed in $statusName",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoLast()
                            }
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.systemBarsPadding()
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                            if (state.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            } else if (state.isComplete) {
                                CompletionScreen(
                                    total = state.queue.size,
                                    processed = state.processedCount,
                                    done = state.doneOnSpotCount,
                                    canUndo = state.lastResolvedItem != null,
                                    onUndo = { viewModel.undoLast() },
                                    onFinish = { finish() }
                                )
                            } else {
                                ProcessScreen(
                                    state = state,
                                    onAnswer = viewModel::answer,
                                    onSkip = viewModel::skipCurrent,
                                    onActionTextChanged = viewModel::updateActionText,
                                    onProjectNameChanged = viewModel::updateProjectName,
                                    onDelegateToChanged = viewModel::updateDelegateTo,
                                    onContextIdChanged = viewModel::updateContextId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompletionScreen(
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
        Text("$processed processed · $done done on the spot", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Inbox")
        }
        if (canUndo) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onUndo) {
                Text("Undo last")
            }
        }
    }
}

@Composable
fun ProcessScreen(
    state: ProcessSessionState,
    onAnswer: (Answer) -> Unit,
    onSkip: () -> Unit,
    onActionTextChanged: (String) -> Unit,
    onProjectNameChanged: (String) -> Unit,
    onDelegateToChanged: (String) -> Unit,
    onContextIdChanged: (String) -> Unit
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val handleAnswer = { answer: Answer ->
        focusManager.clearFocus()
        keyboardController?.hide()
        onAnswer(answer)
    }

    val handleSkip = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onSkip()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { if (state.queue.isEmpty()) 0f else (state.currentIndex + 1).toFloat() / state.queue.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(4.dp)
        )
        Text(
            text = "${state.currentIndex + 1} / ${state.queue.size}",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End).padding(8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Item text large and centered in upper middle
        val rawText = state.queue.getOrNull(state.currentIndex)?.rawText ?: ""
        if (!(state.currentStep == Step.Clarify && ENABLE_CLARIFY_STEP)) {
            Text(
                text = rawText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Question and specific inputs/buttons
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.currentStep) {
                Step.StalledNudge -> {
                    Text("This one keeps getting skipped. Park it?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.Someday) }, modifier = Modifier.fillMaxWidth()) { Text("Someday / Maybe") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.KeepTrying) }, modifier = Modifier.fillMaxWidth()) { Text("Keep trying") }
                }
                Step.Actionable -> {
                    Text("Is it actionable?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.Yes) }, modifier = Modifier.fillMaxWidth()) { Text("Yes") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.No) }, modifier = Modifier.fillMaxWidth()) { Text("No") }
                }
                Step.WorthKeeping -> {
                    Text("Worth keeping?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.Keep) }, modifier = Modifier.fillMaxWidth()) { Text("Keep") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Trash) }, modifier = Modifier.fillMaxWidth()) { Text("Trash") }
                }
                Step.NeedsActionLater -> {
                    Text("Might it need action later?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.Someday) }, modifier = Modifier.fillMaxWidth()) { Text("Someday / Maybe") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Reference) }, modifier = Modifier.fillMaxWidth()) { Text("Just reference") }
                }
                Step.SingleOrProject -> {
                    Text("One action, or several?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.SingleAction) }, modifier = Modifier.fillMaxWidth()) { Text("Single action") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Project) }, modifier = Modifier.fillMaxWidth()) { Text("It's a project") }
                }
                Step.ProjectOutcome -> {
                    Text("What does done look like?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    TextField(
                        value = state.projectName,
                        onValueChange = onProjectNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Project name...") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { handleAnswer(Answer.Next) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.projectName.isNotBlank()
                    ) { Text("Next") }
                }
                Step.Clarify -> {
                    if (ENABLE_CLARIFY_STEP) {
                        TextField(
                            value = state.actionText,
                            onValueChange = onActionTextChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("start with a verb") }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    Text("Under 2 minutes?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.DoItNow) }, modifier = Modifier.fillMaxWidth()) { Text("Do it now") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Later) }, modifier = Modifier.fillMaxWidth()) { Text("Later") }
                }
                Step.DoingNow -> {
                    var elapsedSeconds by remember { mutableStateOf(0) }
                    LaunchedEffect(Unit) {
                        while(true) {
                            delay(1000)
                            elapsedSeconds++
                        }
                    }
                    val isOverrun = elapsedSeconds > 120
                    val timerColor = if (isOverrun) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
                    val mins = elapsedSeconds / 60
                    val secs = elapsedSeconds % 60
                    val timeString = String.format("%02d:%02d", mins, secs)
                    
                    Text(timeString, style = MaterialTheme.typography.displayMedium, color = timerColor)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.Done) }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.NeedMoreTime) }, modifier = Modifier.fillMaxWidth()) { Text("Need more time") }
                }
                Step.Destination -> {
                    Text("Where does it go?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    // Context chips
                    state.contexts.forEach { ctx ->
                        Button(onClick = {
                            onContextIdChanged(ctx.id)
                            handleAnswer(Answer.ContextChip)
                        }, modifier = Modifier.fillMaxWidth()) { Text(ctx.label) }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedButton(onClick = { handleAnswer(Answer.Schedule) }, modifier = Modifier.fillMaxWidth()) { Text("Schedule...") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { handleAnswer(Answer.Delegate) }, modifier = Modifier.fillMaxWidth()) { Text("Delegate...") }
                }
                Step.PickDate -> {
                    // Simulating date picker
                    Text("Pick a date", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { handleAnswer(Answer.PickDateDone) }, modifier = Modifier.fillMaxWidth()) { Text("Set") }
                }
                Step.PickDelegate -> {
                    Text("Who?", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(24.dp))
                    TextField(
                        value = state.delegateTo,
                        onValueChange = onDelegateToChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Name") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { handleAnswer(Answer.PickDelegateDone) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.delegateTo.isNotBlank()
                    ) { Text("Save") }
                }
                is Step.Resolved -> {}
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = handleSkip) {
                Text("Not now", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
INNER_EOF
