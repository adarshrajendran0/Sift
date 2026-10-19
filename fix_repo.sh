#!/bin/bash
sed -i '/suspend fun deleteItem/i \    suspend fun resetSkipCount(id: String) {\n        dao.resetSkipCount(id)\n    }\n' app/src/main/java/com/adarsh/sift/data/ItemRepository.kt
