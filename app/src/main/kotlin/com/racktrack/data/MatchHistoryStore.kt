package com.racktrack.data

import com.racktrack.domain.MatchSummary
import kotlinx.coroutines.flow.Flow

interface MatchHistoryStore {
    val matches: Flow<List<StoredMatch>>

    suspend fun saveCompleted(summary: MatchSummary, completedAtMillis: Long): StoredMatch

    suspend fun getById(id: String): StoredMatch?

    suspend fun deleteById(id: String): Boolean
}
