package com.racktrack.data

import com.racktrack.domain.MatchSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/** JVM-friendly store for tests and previews. */
class InMemoryMatchHistoryStore(
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) : MatchHistoryStore {
    private val _matches = MutableStateFlow<List<StoredMatch>>(emptyList())
    override val matches: Flow<List<StoredMatch>> = _matches.asStateFlow()

    override suspend fun saveCompleted(
        summary: MatchSummary,
        completedAtMillis: Long,
    ): StoredMatch {
        val stored = StoredMatch(
            id = idFactory(),
            completedAtMillis = completedAtMillis,
            summary = summary,
        )
        _matches.value = listOf(stored) + _matches.value
        return stored
    }

    override suspend fun getById(id: String): StoredMatch? =
        _matches.value.firstOrNull { it.id == id }

    override suspend fun deleteById(id: String): Boolean {
        val before = _matches.value
        val next = before.filterNot { it.id == id }
        if (next.size == before.size) return false
        _matches.value = next
        return true
    }
}
