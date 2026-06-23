package com.schroepf.row

import com.schroepf.row.api.log.UserProfile

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.material3.MaterialTheme
import com.schroepf.row.ui.profile.ProfileScreen
import com.schroepf.row.ui.profile.ProfileState
import org.junit.Rule
import org.junit.Test

class ProfileScreenSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun `renders signed out state`() {
        paparazzi.snapshot {
            MaterialTheme {
                ProfileScreen(
                    state = ProfileState(),
                    onLogin = {},
                    onLogout = {}
                )
            }
        }
    }

    @Test
    fun `renders loading state`() {
        paparazzi.snapshot {
            MaterialTheme {
                ProfileScreen(
                    state = ProfileState(isLoading = true),
                    onLogin = {},
                    onLogout = {}
                )
            }
        }
    }

    @Test
    fun `renders success state`() {
        paparazzi.snapshot {
            MaterialTheme {
                ProfileScreen(
                    state = ProfileState(
                        profile = UserProfile(
                            username = "davidhart",
                            fullName = "David Hart",
                            email = "davidh@concept2.com",
                            country = "GBR"
                        )
                    ),
                    onLogin = {},
                    onLogout = {}
                )
            }
        }
    }

    @Test
    fun `renders error state`() {
        paparazzi.snapshot {
            MaterialTheme {
                ProfileScreen(
                    state = ProfileState(error = "invalid_grant"),
                    onLogin = {},
                    onLogout = {}
                )
            }
        }
    }
}
