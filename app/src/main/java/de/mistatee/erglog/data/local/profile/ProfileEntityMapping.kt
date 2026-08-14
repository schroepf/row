package de.mistatee.erglog.data.local.profile

import de.mistatee.erglog.data.concept2.logbook.profile.model.Profile

/** Maps a domain [Profile], as fetched from the API, to its cached [ProfileEntity] form. */
fun Profile.toEntity(): ProfileEntity = ProfileEntity(username = username)

/** Maps a cached [ProfileEntity] back to the domain [Profile] the UI consumes. */
fun ProfileEntity.toDomain(): Profile = Profile(username = username)
