package de.mistatee.erglog.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import de.mistatee.erglog.data.model.Profile

/**
 * The single-row Room-persisted copy of the Logbook Account holder's
 * [Profile]. There is exactly one Profile
 * per authenticated Session, so this table only ever holds one row.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val username: String,
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}

/** Maps a domain [Profile], as fetched from the API, to its cached [ProfileEntity] form. */
fun Profile.toEntity(): ProfileEntity = ProfileEntity(username = username)

/** Maps a cached [ProfileEntity] back to the domain [Profile] the UI consumes. */
fun ProfileEntity.toDomain(): Profile = Profile(username = username)
