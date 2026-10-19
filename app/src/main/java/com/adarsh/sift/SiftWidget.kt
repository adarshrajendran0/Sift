package com.adarsh.sift

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.ColorFilter
import com.adarsh.sift.R

class SiftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SiftWidget()
}

class SiftWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val intent = Intent(context, CaptureActivity::class.java).apply {
                    putExtra("source", "WIDGET")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .cornerRadius(28.dp)
                        .background(GlanceTheme.colors.secondaryContainer)
                        .padding(16.dp)
                        .clickable(actionStartActivity(intent)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Capture a thought",
                        modifier = GlanceModifier.defaultWeight().padding(start = 16.dp),
                        style = TextStyle(color = GlanceTheme.colors.onSecondaryContainer)
                    )
                    
                    Image(
                        provider = ImageProvider(R.drawable.ic_launcher_foreground),
                        contentDescription = "Capture",
                        modifier = GlanceModifier.size(24.dp).padding(end = 16.dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
                    )
                }
            }
        }
    }
}
