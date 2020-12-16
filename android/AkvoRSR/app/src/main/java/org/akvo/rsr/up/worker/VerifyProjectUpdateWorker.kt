package org.akvo.rsr.up.worker

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.akvo.rsr.up.util.Uploader
import org.akvo.rsr.up.util.Uploader.FailedPostException

class VerifyProjectUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        return try {
            val context: Context = applicationContext
            val unresolvedUpdates = Uploader.verifyUpdates(context,
                SettingsUtil.host(context) + ConstantUtil.VERIFY_UPDATE_PATTERN)
            if (unresolvedUpdates == 0) { //mission accomplished
                Log.i(TAG, "Every update verified")
                val helper = NotificationHelper()
                helper.createNotificationChannel(context)
                helper.displayNotification(context)
                //stop the service
                val workManager = WorkManager.getInstance(context)
                workManager.cancelAllWorkByTag(TAG)
                Result.success()
            } else {
                Log.i(TAG, "Still unverified:$unresolvedUpdates")
                Result.failure()
            }
        } catch (e: FailedPostException) {
            Log.e(TAG, "Update error", e)
            Result.failure()
        } catch (e: Exception) {
            Log.e(TAG, "Update error", e)
            Result.failure(workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to e.message))
        }
    }

    companion object {
        const val TAG = "VerifyProjectUpdateWork"
    }
}
