with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('ProcessViewModelFactory(app.repository, app.applicationScope, intent.getStringExtra("item_id"))', 'ProcessViewModelFactory(app.repository, app.applicationScope, intent.getStringExtra("item_id"), app.haptics)')

# InboxEmpty trigger
content = content.replace('Text("Your mind is clear."', 'LaunchedEffect(Unit) { (application as SiftApplication).haptics.fire(Feel.InboxEmpty) }\n                Text("Your mind is clear."')

with open('app/src/main/java/com/adarsh/sift/ProcessActivity.kt', 'w') as f:
    f.write(content)

