package de.mistatee.erglog.data.local

import androidx.room.TypeConverter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/** Room [androidx.room.TypeConverters] for the [kotlin.time] types used by cached entities. */
internal class Converters {
    @TypeConverter
    fun instantToEpochMilliseconds(instant: Instant): Long = instant.toEpochMilliseconds()

    @TypeConverter
    fun epochMillisecondsToInstant(epochMilliseconds: Long): Instant =
        Instant.fromEpochMilliseconds(epochMilliseconds)

    @TypeConverter
    fun durationToMilliseconds(duration: Duration): Long = duration.inWholeMilliseconds

    @TypeConverter
    fun millisecondsToDuration(milliseconds: Long): Duration = milliseconds.milliseconds
}
