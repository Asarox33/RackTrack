package com.racktrack.data

import android.content.Context
import com.racktrack.domain.MatchSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.UUID

class JsonMatchHistoryStore(
    context: Context,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) : MatchHistoryStore {
    private val file = File(context.applicationContext.filesDir, FILE_NAME)
    private val mutex = Mutex()
    private val _matches = MutableStateFlow(readFromDisk())
    override val matches: Flow<List<StoredMatch>> = _matches.asStateFlow()

    override suspend fun saveCompleted(
        summary: MatchSummary,
        completedAtMillis: Long,
    ): StoredMatch = mutex.withLock {
        val stored = StoredMatch(
            id = idFactory(),
            completedAtMillis = completedAtMillis,
            summary = summary,
        )
        val next = listOf(stored) + _matches.value
        writeToDisk(next)
        _matches.value = next
        stored
    }

    override suspend fun getById(id: String): StoredMatch? =
        _matches.value.firstOrNull { it.id == id }

    override suspend fun deleteById(id: String): Boolean = mutex.withLock {
        val next = _matches.value.filterNot { it.id == id }
        if (next.size == _matches.value.size) return@withLock false
        writeToDisk(next)
        _matches.value = next
        true
    }

    private fun readFromDisk(): List<StoredMatch> {
        if (!file.exists()) return emptyList()
        return runCatching {
            MatchSummaryJson.decodeStoredList(file.readText())
        }.getOrDefault(emptyList())
    }

    private fun writeToDisk(matches: List<StoredMatch>) {
        file.writeText(MatchSummaryJson.encodeStoredList(matches))
    }

    private companion object {
        const val FILE_NAME = "match_history.json"
    }
}
