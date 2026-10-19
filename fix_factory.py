import re

with open('app/src/main/java/com/adarsh/sift/ProcessViewModelFactory.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '    private val singleItemId: String?',
    '    private val singleItemId: String?,\n    private val haptics: HapticsService'
)

with open('app/src/main/java/com/adarsh/sift/ProcessViewModelFactory.kt', 'w') as f:
    f.write(content)
