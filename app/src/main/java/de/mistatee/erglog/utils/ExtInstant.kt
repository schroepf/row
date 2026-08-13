package de.mistatee.erglog.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.formatResultDate(): String =
    toLocalDateTime(TimeZone.currentSystemDefault()).let {
        "${it.month.name.take(1)}${it.month.name.drop(1).lowercase()} ${it.day}, ${it.year}"
    }
