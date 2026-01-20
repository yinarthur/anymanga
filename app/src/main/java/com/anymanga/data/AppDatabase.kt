package com.anymanga.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "manga")
data class MangaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val sourceId: String,
    val favorite: Boolean = false,
    val lastReadChapterId: String? = null
)

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val id: String,
    val mangaId: String,
    val title: String,
    val url: String,
    val date: String? = null,
    val isDownloaded: Boolean = false,
    val localPath: String? = null
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val chapterId: String,
    val mangaId: String,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED
}

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga WHERE favorite = 1")
    fun getLibrary(): Flow<List<MangaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: MangaEntity)

    @Query("SELECT * FROM chapters WHERE mangaId = :mangaId")
    fun getChapters(mangaId: String): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE isDownloaded = 1")
    fun getDownloadedChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM downloads")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Delete
    suspend fun deleteManga(manga: MangaEntity)
}

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val mangaId: String, // Combined or URL
    val sourceId: String,
    val mangaTitle: String,
    val mangaThumbnailUrl: String,
    val chapterId: String,
    val chapterTitle: String,
    val lastReadTime: Long = System.currentTimeMillis()
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY lastReadTime DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE mangaId = :mangaId")
    suspend fun deleteHistory(mangaId: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}

@Database(entities = [MangaEntity::class, ChapterEntity::class, DownloadEntity::class, HistoryEntity::class, SourceTemplateEntity::class, UserSourceEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
    abstract fun historyDao(): HistoryDao
    abstract fun sourceDao(): SourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anymanga-db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
