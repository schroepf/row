package com.schroepf.row.ui.profile

import com.schroepf.row.ui.theme.RowTheme
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode
import com.schroepf.row.api.log.UserProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ProfileScreenSnapshotTest(private val config: Config) {
    enum class Config(val deviceConfig: DeviceConfig) {
        Light(DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NOTNIGHT)),
        Dark(DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data() = Config.entries.toTypedArray()
    }

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = config.deviceConfig)

    @Test
    fun `renders signed out state`() {
        paparazzi.snapshot {
            ProfileScreenPreview(
                state = ProfileState(),
            )
        }
    }

    @Test
    fun `renders loading state`() {
        paparazzi.snapshot {
            ProfileScreenPreview(
                state = ProfileState(isLoading = true),
            )
        }
    }

    @Test
    fun `renders success state`() {
        paparazzi.snapshot {
            ProfileScreenPreview(
                state = ProfileState(
                    profile = UserProfile(
                        username = "davidhart",
                        fullName = "David Hart",
                        email = "davidh@concept2.com",
                        country = "GBR"
                    )
                ),
            )
        }
    }

    @Test
    fun `renders error state`() {
        paparazzi.snapshot {
            ProfileScreenPreview(
                state = ProfileState(error = "invalid_grant"),
            )
        }
    }
}
