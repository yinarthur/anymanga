package com.anymanga.sync

import android.content.Context
import androidx.work.*
import com.anymanga.data.AppDatabase
import com.anymanga.data.PreferencesManager
import com.anymanga.data.TemplateRepository
import com.anymanga.data.TemplatesUpdater
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class TemplateSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val preferencesManager = PreferencesManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        val updater = TemplatesUpdater(applicationContext, preferencesManager)
        val repository = TemplateRepository(updater, database.sourceDao())

        return try {
            val result = repository.syncRemoteTemplates()
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "TemplateSyncWork"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<TemplateSyncWorker>(12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
