@file:Suppress("MatchingDeclarationName")
package de.mistatee.erglog

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Main : NavKey

@Serializable
data object Login : NavKey
