package com.anymanga.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "source_templates")
data class SourceTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val domain: String,
    val baseUrl: String,
    val engineType: String,
    val lang: String,
    val isNsfw: Boolean,
    val hasCloudflare: Boolean = false,
    val isDead: Boolean = false,
    val lastUpdate: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_sources")
data class UserSourceEntity(
    @PrimaryKey val domain: String,
    val enabled: Boolean = false,
    val pinnedOrder: Int? = null
)

data class SourceWithStatus(
    @Embedded val template: SourceTemplateEntity,
    @Relation(
        parentColumn = "domain",
        entityColumn = "domain"
    )
    val userSource: UserSourceEntity?
)

@Dao
interface SourceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTemplates(templates: List<SourceTemplateEntity>)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM source_templates")
    fun getAllTemplates(): Flow<List<SourceTemplateEntity>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM source_templates")
    fun observeTemplatesWithStatus(): Flow<List<SourceWithStatus>>

    @Transaction
    @RewriteQueriesToDropUnusedColumns
    @Query("""
        SELECT * FROM source_templates 
        JOIN user_sources ON source_templates.domain = user_sources.domain 
        WHERE user_sources.enabled = 1
    """)
    fun observeEnabledSources(): Flow<List<SourceTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setUserSource(userSource: UserSourceEntity)

    @Query("UPDATE user_sources SET enabled = :enabled WHERE domain = :domain")
    suspend fun setEnabled(domain: String, enabled: Boolean)

    @Query("DELETE FROM source_templates WHERE id NOT IN (:currentIds)")
    suspend fun deleteOldTemplates(currentIds: List<String>)
}
