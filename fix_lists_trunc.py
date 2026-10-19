with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'r') as f:
    content = f.read()

# find @Composable fun ItemRow
index = content.find('@Composable\nfun ItemRow')
if index != -1:
    content = content[:index]

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'w') as f:
    f.write(content)
