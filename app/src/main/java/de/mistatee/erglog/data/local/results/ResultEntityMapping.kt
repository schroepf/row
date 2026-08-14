package de.mistatee.erglog.data.local.results

import de.mistatee.erglog.data.concept2.logbook.results.model.Result

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
