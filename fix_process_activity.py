import re

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

# Fix ProcessScreen call
old_call = """                                ProcessScreen(
                                    state = state,
                                    onAnswer = viewModel::answer,
                                    onSkip = viewModel::skipCurrent,
                                    onActionTextChanged = viewModel::updateActionText,
                                    onProjectNameChanged = viewModel::updateProjectName,
                                    onDelegateToChanged = viewModel::updateDelegateTo,
                                    onContextIdChanged = viewModel::updateContextId
                                )"""

new_call = """                                ProcessScreen(
                                    state = state,
                                    onAnswer = viewModel::answer,
                                    onSkip = viewModel::skipCurrent,
                                    onActionTextChanged = viewModel::updateActionText,
                                    onProjectNameChanged = viewModel::updateProjectName,
                                    onDelegateToChanged = viewModel::updateDelegateTo,
                                    onDueAtChanged = viewModel::updateDueAt,
                                    onContextIdChanged = viewModel::updateContextId
                                )"""

content = content.replace(old_call, new_call)

# Fix duplicate @Composable
content = content.replace('@Composable\n@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable', '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable')
content = content.replace('@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\n@Composable', '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable')

# Wait, the output was:
# @Composable
# @androidx.compose.material3.ExperimentalMaterial3Api
# @Composable
content = content.replace('@Composable\n@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun ProcessScreen', '@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun ProcessScreen')

# To be safe, just remove all composable for ProcessScreen and add one
content = re.sub(
    r'(@Composable\s*)+@androidx\.compose\.material3\.ExperimentalMaterial3Api\s*(@Composable\s*)+fun ProcessScreen',
    r'@androidx.compose.material3.ExperimentalMaterial3Api\n@Composable\nfun ProcessScreen',
    content
)

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)
