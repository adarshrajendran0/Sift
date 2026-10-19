#!/bin/bash
# Remove the extra functions from the bottom of ListsScreen.kt
sed -i '/@Composable\nfun ItemRow/,/}/d' app/src/main/java/com/adarsh/sift/ListsScreen.kt
# Since sed multiline delete can be tricky, I'll use a python script to truncate the file
