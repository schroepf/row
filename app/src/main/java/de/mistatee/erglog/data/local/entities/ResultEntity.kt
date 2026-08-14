package de.mistatee.erglog.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.mistatee.erglog.data.model.Result
import kotlin.time.Duration
import kotlin.time.Instant

/** The Room-persisted copy of a single [Result]. */
@Entity(tableName = "results")
data class ResultEntity(
    @PrimaryKey val id: Long,
    val date: Instant,
    val distance: Int,
    val type: String,
    val duration: Duration,
)

/** Maps a domain [Result], as fetched from the API, to its cached [ResultEntity] form. */
fun Result.toEntity(): ResultEntity =
    ResultEntity(
        id = id,
        date = date,
        distance = distance,
        type = type,
        duration = duration,
    )

/** Maps a cached [ResultEntity] back to the domain [Result] the UI consumes. */
fun ResultEntity.toDomain(): Result =
    Result(
        id = id,
        date = date,
        distance = distance,
        type = type,
        duration = duration,
    )
