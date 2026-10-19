#!/bin/bash
# Remove LaunchedEffect block from Step.Clarify inside ProcessActivity.kt
sed -i '/} else {/,/}/d' app/src/main/java/com/adarsh/sift/ProcessActivity.kt

# Conditionally display the main header
# First let's find the header text rendering
sed -i 's/Text(/if (!(state.currentStep == Step.Clarify \&\& ENABLE_CLARIFY_STEP)) {\n            Text(/g' app/src/main/java/com/adarsh/sift/ProcessActivity.kt
sed -i 's/modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)/modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)\n            )\n        }/g' app/src/main/java/com/adarsh/sift/ProcessActivity.kt
