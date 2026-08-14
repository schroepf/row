package de.mistatee.erglog.data.local.results

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Duration
import kotlin.time.Instant

/** The Room-persisted copy of a single [de.mistatee.erglog.data.concept2.logbook.results.model.Result]. */
@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey val id: Long,
    val date: Instant,
    val distance: Int,
    val type: String,
    val duration: Duration,
)
