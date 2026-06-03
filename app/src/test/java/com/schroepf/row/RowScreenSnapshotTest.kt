package com.schroepf.row

import com.schroepf.row.api.log.UserProfile

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.material3.MaterialTheme
import org.junit.Rule
import org.junit.Test

class RowScreenSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun `renders signed out state`() {
        paparazzi.snapshot {
            MaterialTheme {
                RowScreen(
                    state = RowState(),
                    onLogin = {}
                )
            }
        }
    }

    @Test
    fun `renders loading state`() {
        paparazzi.snapshot {
            MaterialTheme {
                RowScreen(
                    state = RowState(isLoading = true),
                    onLogin = {}
                )
            }
        }
    }

    @Test
    fun `renders success state`() {
        paparazzi.snapshot {
            MaterialTheme {
                RowScreen(
                    state = RowState(
                        profile = UserProfile(
                            username = "davidhart",
                            fullName = "David Hart",
                            email = "davidh@concept2.com",
                            country = "GBR"
                        )
                    ),
                    onLogin = {}
                )
            }
        }
    }

    @Test
    fun `renders error state`() {
        paparazzi.snapshot {
            MaterialTheme {
                RowScreen(
                    state = RowState(error = "invalid_grant"),
                    onLogin = {}
                )
            }
        }
    }
}
