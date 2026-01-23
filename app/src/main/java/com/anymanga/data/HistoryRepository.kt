package com.anymanga.data

import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val database: AppDatabase) {
    private val historyDao = database.historyDao()

    fun getHistory(): Flow<List<HistoryEntity>> {
        return historyDao.getHistory()
    }

    suspend fun insertHistory(history: HistoryEntity) {
        historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(mangaId: String) {
        historyDao.deleteHistory(mangaId)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}
