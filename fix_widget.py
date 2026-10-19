import re

with open('app/src/main/java/com/adarsh/sift/SiftWidget.kt', 'r') as f:
    content = f.read()

content = content.replace('R.drawable.ic_mic', 'R.drawable.ic_launcher_foreground')
content = content.replace('"Microphone"', '"Capture"')

with open('app/src/main/java/com/adarsh/sift/SiftWidget.kt', 'w') as f:
    f.write(content)
