package org.akvo.rsr.up.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.akvo.rsr.up.R
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.SettingsUtil
import org.akvo.rsr.up.util.Uploader

class SubmitIpdWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext
        val period: Int = getPeriod()
        val data: String = inputData.getString(ConstantUtil.DATA_KEY) ?: ""
        val currentActualValue: String =
            inputData.getString(ConstantUtil.CURRENT_ACTUAL_VALUE_KEY) ?: ""
        val relative: Boolean = inputData.getBoolean(ConstantUtil.RELATIVE_DATA_KEY, false)
        val description: String = inputData.getString(ConstantUtil.DESCRIPTION_KEY) ?: ""
        val photoFn: String = inputData.getString(ConstantUtil.PHOTO_FN_KEY) ?: ""
        val fileFn: String = inputData.getString(ConstantUtil.FILE_FN_KEY) ?: ""

        return try {
            Uploader.sendIndicatorPeriodData(
                appContext,
                ConstantUtil.POST_RESULT_URL,
                ConstantUtil.IPD_ATTACHMENT_PATTERN,
                period,
                data,
                currentActualValue,
                relative,
                description,
                photoFn,
                fileFn,
                SettingsUtil.getAuthUser(appContext),
                null
            )
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SubmitIpdService() error:", e)
            Result.failure(
                workDataOf(
                    ConstantUtil.SERVICE_ERRMSG_KEY to appContext.resources.getString(
                        R.string.errmsg_resultpost_failed
                    ) + e.message
                )
            )
        }
    }

    private fun getPeriod(): Int {
        val periodKey = inputData.getString(ConstantUtil.PERIOD_ID_KEY) ?: ""
        return if (periodKey.isNotBlank()) {
            periodKey.toInt()
        } else {
            -1
        }
    }

    companion object {
        const val TAG = "SubmitIpdWorker"
    }
}
