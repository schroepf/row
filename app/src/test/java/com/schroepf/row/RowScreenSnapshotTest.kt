package com.schroepf.row

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.material3.MaterialTheme
import org.junit.Rule
import org.junit.Test

class RowScreenSnapshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test
    fun `renders success state`() {
        paparazzi.snapshot {
            MaterialTheme {
                RowScreen(
                    state = RowState(isLoading = false, message = "Welcome from Ktor", error = null),
                    onRefresh = {}
                )
            }
        }
    }
}
