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

class SubmitEmploymentWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext

        val orgId: Int = inputData.getString(ConstantUtil.ORG_ID_KEY)?.toInt() ?: -1
        val countryId = inputData.getString(ConstantUtil.COUNTRY_ID_KEY)?.toInt() ?: -1
        val jobTitle: String = inputData.getString(ConstantUtil.JOB_TITLE_KEY) ?: ""

        return try {
            Uploader.postEmployment(
                appContext,
                SettingsUtil.host(appContext) + ConstantUtil.POST_EMPLOYMENT_PATTERN,
                orgId,
                countryId,
                jobTitle,
                SettingsUtil.getAuthUser(appContext)
            )
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SubmitEmploymentService() error:", e)
            Result.failure(workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to appContext.resources.getString(
                R.string.errmsg_emp_application_failed) + e.message))
        }
    }

    companion object {
        const val TAG = "SubmitEmploymentWorker"
    }
}
