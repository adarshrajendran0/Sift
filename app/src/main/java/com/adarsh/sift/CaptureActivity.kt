package com.adarsh.sift

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.adarsh.sift.data.Source
import com.adarsh.sift.ui.theme.SiftTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CaptureActivity : ComponentActivity() {

    private var currentText by mutableStateOf("")
    private var source = Source.IN_APP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val action = intent.action
        val textExtra = intent.getStringExtra(Intent.EXTRA_TEXT) ?: intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
        if ((action == Intent.ACTION_SEND || action == Intent.ACTION_PROCESS_TEXT) && !textExtra.isNullOrBlank()) {
            val s = if (action == Intent.ACTION_SEND) Source.SHARE else Source.PROCESS_TEXT
            saveToDatabase(textExtra, s)
            Toast.makeText(this, "Captured", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        source = intent.getStringExtra("source")?.let {
            try { Source.valueOf(it) } catch (e: Exception) { Source.IN_APP }
        } ?: Source.IN_APP

        setContent {
            SiftTheme {
                Surface(color = Color.Transparent, modifier = Modifier.fillMaxSize()) {
                    CaptureView(
                        text = currentText,
                        onTextChange = { currentText = it },
                        onSend = { text ->
                            if (text.isNotBlank()) {
                                saveToDatabase(text, source)
                                currentText = ""
                            }
                        },
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (currentText.isNotBlank()) {
            saveToDatabase(currentText, source)
            currentText = ""
        }
    }

    private fun saveToDatabase(text: String, src: Source) {
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
    }
}

@Composable
fun CaptureView(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var placeholderText by remember { mutableStateOf("Capture a thought") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Scrim
        Surface(
            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {}
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    minLines = 1,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
                        .focusRequester(focusRequester),
                    shape = MaterialTheme.shapes.extraSmall,
                    placeholder = { androidx.compose.material3.Text(placeholderText) },
                    trailingIcon = {
                        FilledTonalIconButton(
                            onClick = { 
                                val currentText = text
                                onSend(currentText) 
                                coroutineScope.launch {
                                    placeholderText = "Captured"
                                    delay(800)
                                    placeholderText = "Capture a thought"
                                }
                            },
                            enabled = text.isNotBlank()
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
