import re

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'r') as f:
    content = f.read()

# I will just write a regex or replace each function that is missing its brace.
# Functions missing brace:
# NextActionsList, ProjectsList, ProjectRow, WaitingForList, CalendarList, SimpleList, ListHeader, EmptyState

fixes = [
    ("        }\n    }\n@Composable\nfun ProjectsList", "        }\n    }\n}\n\n@Composable\nfun ProjectsList"),
    ("        }\n    }\n@OptIn", "        }\n    }\n}\n\n@OptIn"),
    ("            }\n        }\n    }\n@Composable\nfun WaitingForList", "            }\n        }\n    }\n}\n\n@Composable\nfun WaitingForList"),
    ("            }\n        }\n    }\n@OptIn", "            }\n        }\n    }\n}\n\n@OptIn"),
    ("            }\n        }\n    }\n@Composable\nfun SimpleList", "            }\n        }\n    }\n}\n\n@Composable\nfun SimpleList"),
    ("        }\n    }\n@Composable\nfun ListHeader", "        }\n    }\n}\n\n@Composable\nfun ListHeader"),
    ("            .padding(horizontal = 16.dp, vertical = 8.dp)\n    )\n@Composable\nfun EmptyState", "            .padding(horizontal = 16.dp, vertical = 8.dp)\n    )\n}\n\n@Composable\nfun EmptyState"),
    ("        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)\n    }\n}", "        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)\n    }\n}")
]

for old, new in fixes:
    content = content.replace(old, new)

with open('app/src/main/java/com/adarsh/sift/ListsScreen.kt', 'w') as f:
    f.write(content)
