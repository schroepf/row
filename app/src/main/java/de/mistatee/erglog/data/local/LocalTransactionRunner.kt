package de.mistatee.erglog.data.local

import androidx.room.RoomDatabase
import androidx.room.withTransaction

/**
 * Runs a block of local-cache writes atomically. Exists as a seam so callers (e.g.
 * [de.mistatee.erglog.data.local.results.ResultRemoteMediator]) don't depend on a concrete
 * [RoomDatabase], keeping them mockable in tests.
 */
interface LocalTransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

class RoomTransactionRunner(private val database: RoomDatabase) : LocalTransactionRunner {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T = database.withTransaction(block)
}
