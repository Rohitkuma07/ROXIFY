package com.roxify.music.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Small formatting helpers shared across the UI. Pure functions, easy to unit test. */
object Formatters {

    /** Formats milliseconds as `m:ss`, or `h:mm:ss` for hour-long tracks. */
    fun duration(ms: Long): String {
        if (ms <= 0L) return "0:00"
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    /** Compact count, e.g. 1200 -> "1.2K". */
    fun compactCount(count: Int): String = when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }

    fun date(epochMs: Long): String {
        if (epochMs <= 0L) return "Unknown"
        val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        return fmt.format(Date(epochMs))
    }

    /** Returns the total minutes of listening rendered as `3h 24m`, or `12m`. */
    fun listeningTime(totalMinutes: Long): String {
        if (totalMinutes <= 0L) return "0m"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun greeting(hour: Int): String = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }

    fun greetingNow(): String = greeting(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
}

/** Runtime permission helpers for reading device audio. */
object MediaPermissions {

    /** The storage permission required to read audio on this API level. */
    val audioPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    fun hasAudioPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, audioPermission) ==
            PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
}

/** Makes sure a value stays inside a range (used by seek bars, sliders, tests). */
fun Long.coerceToRange(min: Long, max: Long): Long = coerceIn(min, max)

fun Int?.orZero(): Int = this ?: 0
