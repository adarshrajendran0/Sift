#!/bin/bash
sed -i 's/state.currentIndex.toFloat()/((state.currentIndex + 1).toFloat())/g' app/src/main/java/com/adarsh/sift/ProcessActivity.kt
sed -i 's/text = "${state.currentIndex} \/ ${state.queue.size}"/text = "${state.currentIndex + 1} \/ ${state.queue.size}"/g' app/src/main/java/com/adarsh/sift/ProcessActivity.kt
