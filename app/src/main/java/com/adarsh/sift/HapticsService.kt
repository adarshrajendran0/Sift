package com.adarsh.sift

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat

enum class Feel {
    Captured,
    Decided,
    Filed,
    Parked,
    DoneOnSpot,
    Undone,
    InboxEmpty
}

class HapticsService(private val context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager?
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    }

    private val prefs = context.getSharedPreferences("sift_prefs", Context.MODE_PRIVATE)

    fun fire(feel: Feel) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        if (!prefs.getBoolean("haptics_enabled", true)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val effect = when (feel) {
                Feel.Captured -> VibrationEffect.startComposition().addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK).compose()
                Feel.Decided -> VibrationEffect.startComposition().addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK).compose()
                Feel.Filed -> VibrationEffect.startComposition().addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK).compose()
                Feel.Parked -> VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.5f, 50)
                    .compose()
                Feel.DoneOnSpot -> VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK)
                    .compose()
                Feel.Undone -> VibrationEffect.startComposition().addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL).compose()
                Feel.InboxEmpty -> VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD)
                    .compose()
            }
            vibrator.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val effect = when (feel) {
                Feel.Captured -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                Feel.Decided -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                Feel.Filed -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                Feel.Parked -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                Feel.DoneOnSpot -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                Feel.Undone -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                Feel.InboxEmpty -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
            }
            vibrator.vibrate(effect)
        } else {
            // Pre-Q fallback
            @Suppress("DEPRECATION")
            when (feel) {
                Feel.Captured, Feel.Decided, Feel.Parked, Feel.Undone -> vibrator.vibrate(10)
                Feel.Filed -> vibrator.vibrate(20)
                Feel.DoneOnSpot -> vibrator.vibrate(longArrayOf(0, 20, 50, 20), -1)
                Feel.InboxEmpty -> vibrator.vibrate(longArrayOf(0, 50, 50, 100), -1)
            }
        }
    }
}
