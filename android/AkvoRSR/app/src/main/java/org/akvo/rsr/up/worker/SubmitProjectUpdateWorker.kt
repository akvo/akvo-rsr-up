package org.akvo.rsr.up.worker

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.akvo.rsr.up.util.Uploader
import org.akvo.rsr.up.util.Uploader.FailedPostException
import org.akvo.rsr.up.util.Uploader.UnresolvedPostException

class SubmitProjectUpdateWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    @SuppressLint("LongLogTag")
    override fun doWork(): Result {
        val projectId = inputData.getString(ConstantUtil.UPDATE_ID_KEY)
        val appContext = applicationContext
        val sendImg = SettingsUtil.ReadBoolean(appContext, ConstantUtil.SEND_IMG_SETTING_KEY, true)
        val user = SettingsUtil.getAuthUser(appContext)
        if (projectId != null) {
            try {
                Uploader.sendUpdate(
                    appContext,
                    projectId,
                    SettingsUtil.host(appContext) + ConstantUtil.POST_UPDATE_URL, // + ConstantUtil.API_KEY_PATTERN,
                    SettingsUtil.host(appContext) + ConstantUtil.VERIFY_UPDATE_PATTERN,
                    sendImg,
                    user
                ) { sofar, total ->
                    setProgressAsync(
                        workDataOf(
                            ConstantUtil.SOFAR_KEY to sofar,
                            ConstantUtil.TOTAL_KEY to total
                        )
                    )
                }
                return Result.success()
            } catch (e: FailedPostException) {
                return Result.failure(workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to e.message))
            } catch (e: UnresolvedPostException) {
                return Result.failure(
                    workDataOf(
                        ConstantUtil.SERVICE_ERRMSG_KEY to e.message,
                        ConstantUtil.SERVICE_UNRESOLVED_KEY to true
                    )
                )
            } catch (e: Exception) { // TODO: show to user
                Log.e(TAG, "Config problem", e)
            }
        }
        return Result.failure()
    }

    companion object {
        const val TAG = "SubmitProjectUpdateWorker"
    }
}
