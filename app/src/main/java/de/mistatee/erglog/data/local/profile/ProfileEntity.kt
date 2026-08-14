package de.mistatee.erglog.data.local.profile

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single-row Room-persisted copy of the Logbook Account holder's
 * [de.mistatee.erglog.data.concept2.logbook.profile.model.Profile]. There is exactly one Profile
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
