package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateUtils {
    private const val MILLIS_PER_DAY = 86_400_000L

    fun todayEpochDay(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis + cal.timeZone.getOffset(cal.timeInMillis))
    }

    fun millisToEpochDay(utcMillis: Long): Long {
        return TimeUnit.MILLISECONDS.toDays(utcMillis)
    }

    fun epochDayToLocalMillis(epochDay: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochDay * MILLIS_PER_DAY
        // Adjust back from UTC epoch day to local midnight
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utcCal.timeInMillis = epochDay * MILLIS_PER_DAY
        val localCal = Calendar.getInstance()
        localCal.set(
            utcCal.get(Calendar.YEAR),
            utcCal.get(Calendar.MONTH),
            utcCal.get(Calendar.DAY_OF_MONTH),
            9,
            0,
            0
        )
        localCal.set(Calendar.MILLISECOND, 0)
        return localCal.timeInMillis
    }

    fun epochDayToUtcPickerMillis(epochDay: Long): Long = epochDay * MILLIS_PER_DAY

    fun formatShortDate(epochDay: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = epochDay * MILLIS_PER_DAY
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }

    fun formatMediumDate(epochDay: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = epochDay * MILLIS_PER_DAY
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }

    fun daysUntil(targetEpochDay: Long): Long = targetEpochDay - todayEpochDay()

    fun formatCountdown(epochDay: Long, isCancelled: Boolean): String {
        val dateStr = formatShortDate(epochDay)
        if (isCancelled) return "Ended $dateStr"
        val diff = daysUntil(epochDay)
        return when {
            diff < -1 -> "Overdue ${-diff}d • $dateStr"
            diff == -1L -> "Due yesterday • $dateStr"
            diff == 0L -> "Due today • $dateStr"
            diff == 1L -> "Due tomorrow • $dateStr"
            else -> "Due in $diff days • $dateStr"
        }
    }

    fun formatIcsUtc(epochDay: Long): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = epochDay * MILLIS_PER_DAY
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }

    fun formatNowIcsUtc(): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun formatRelativeBackupAge(timestampMs: Long): String {
        if (timestampMs <= 0L) return "Never backed up"
        val diffDays = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - timestampMs)
        return when {
            diffDays <= 0L -> "Backed up today"
            diffDays == 1L -> "Backed up yesterday"
            else -> "Last backup $diffDays days ago"
        }
    }
}
