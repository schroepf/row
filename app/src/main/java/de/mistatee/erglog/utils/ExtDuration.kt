package de.mistatee.erglog.utils

import kotlin.time.Duration

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600

fun Duration.formatResultDuration(): String {
    val totalSeconds = inWholeSeconds
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}
