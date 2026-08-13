package de.mistatee.erglog.data.concept2.logbook.profile

import de.mistatee.erglog.data.concept2.logbook.profile.model.Profile

interface ProfileRepository {
    suspend fun fetchProfile(): Result<Profile>
}

