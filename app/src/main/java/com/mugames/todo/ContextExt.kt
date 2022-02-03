package com.mugames.todo

import android.content.Context
import android.graphics.Color
import android.os.*
import androidx.annotation.*
import androidx.core.content.res.use

@ColorInt
fun Context.themeColor(@AttrRes attrId: Int): Int {
    return obtainStyledAttributes(
        intArrayOf(attrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

@Dimension
fun Context.themeStyle(@AttrRes attrId: Int): Int {
    return obtainStyledAttributes(
        intArrayOf(attrId)
    ).use {
        it.getDimensionPixelSize(0, 0)
    }
}

@RequiresPermission(android.Manifest.permission.VIBRATE)
@RequiresApi(Build.VERSION_CODES.O)
fun Context.vibrateDevice(milliseconds: Long, amplitude: Int) = vibrate(milliseconds, amplitude)

@RequiresPermission(android.Manifest.permission.VIBRATE)
@Deprecated("This method is deprecated from Build.VERSION_CODES.O(API 26)",
    level =  DeprecationLevel.WARNING
)
fun Context.vibrateDevice(milliseconds: Long) = vibrate(milliseconds, 0)

private fun Context.vibrate(milliseconds: Long, amplitude: Int) {
    val oneShotVibe = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        VibrationEffect.createOneShot(milliseconds, amplitude)
    else
        null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.vibrate(CombinedVibration.createParallel(VibrationEffect.createOneShot(
            100,
            VibrationEffect.DEFAULT_AMPLITUDE)
        ))
    } else {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(oneShotVibe)
        else vibrator.vibrate(milliseconds)
    }
}