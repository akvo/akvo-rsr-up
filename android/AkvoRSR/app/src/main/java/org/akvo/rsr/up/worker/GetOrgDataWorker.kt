package org.akvo.rsr.up.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.akvo.rsr.up.R
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.Downloader
import org.akvo.rsr.up.util.FileUtil
import org.akvo.rsr.up.util.SettingsUtil
import java.net.MalformedURLException
import java.net.URL

class GetOrgDataWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    
    override fun doWork(): Result {
        val appContext = applicationContext

        val ad = RsrDbAdapter(appContext)
        val dl = Downloader()
        var errMsg: String? = null
        val fetchImages = !SettingsUtil.ReadBoolean(appContext, "setting_delay_image_fetch", false)


        val host = SettingsUtil.host(appContext)
        val start = System.currentTimeMillis()

        ad.open()
        try {
            if (FETCH_ORGS) {
                // Fetch org data.
                try {
                    if (BRIEF) {
                        dl.fetchTypeaheadOrgList(
                            appContext,
                            ad,
                            URL(host + ConstantUtil.FETCH_ORGS_TYPEAHEAD_URL)
                        ) { sofar, total ->
                            updateProgress(0, sofar, total)
                        }
                    } else {
                        dl.fetchOrgListRestApiPaged(
                            appContext,
                            ad,
                            URL(host + ConstantUtil.FETCH_ORGS_URL)
                        ) { sofar, total ->
                            updateProgress(0, sofar, total)
                        }
                    }
                    //TODO need a way to get this called by the paged fetch: broadcastProgress(0, j, dl.???);
                } catch (e: Exception) { // probably network reasons
                    Log.e(TAG, "Bad organisation fetch:", e)
                    errMsg = appContext.resources.getString(R.string.errmsg_org_fetch_failed) + e.message
                }
            }
            if (FETCH_EMPLOYMENTS) {
                // Fetch emp data.
                try {
                    dl.fetchEmploymentListPaged(
                        appContext,
                        ad,
                        URL(host + String.format(ConstantUtil.FETCH_EMPLOYMENTS_URL_PATTERN,
                            SettingsUtil.getAuthUser(appContext).id))
                    ) { sofar, total ->
                        updateProgress(0, sofar, total)
                    }
                    //TODO need a way to get this called by the paged fetch: broadcastProgress(0, j, dl.???);
                } catch (e: Exception) { // probably network reasons
                    Log.e(TAG, "Bad employment fetch:", e)
                    errMsg = appContext.resources.getString(R.string.errmsg_emp_fetch_failed) + e.message
                }
            }
            updateProgress(0, 100, 100)
            try {
                if (FETCH_COUNTRIES && ad.getCountryCount() == 0) { // rarely changes, so only fetch countries if we never did that
                    dl.fetchCountryListRestApiPaged(appContext,
                        ad,
                        URL(SettingsUtil.host(appContext) + String.format(ConstantUtil.FETCH_COUNTRIES_URL)))
                }
            } catch (e: Exception) { // probably network reasons
                Log.e(TAG, "Bad organisation fetch:", e)
                errMsg = appContext.resources.getString(R.string.errmsg_org_fetch_failed) + e.message
            }
            updateProgress(1, 100, 100)
            //logos?
            if (fetchImages) {
                try {
                    dl.fetchMissingThumbnails(appContext,
                        host,
                        FileUtil.getExternalCacheDir(appContext).toString()
                    ) { sofar, total ->
                        updateProgress(2, sofar, total)
                    }
                } catch (e: MalformedURLException) {
                    Log.e(TAG, "Bad thumbnail URL:", e)
                    errMsg = "Thumbnail url problem: $e"
                }
            }
        } finally {
            ad.close()
        }

        val end = System.currentTimeMillis()
        Log.i(TAG, "Fetch complete in: " + (end - start) / 1000.0)

        // broadcast completion
        return if (errMsg != null) {
            Result.failure(workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to errMsg))
        } else {
            Result.success()
        }
    }

    private fun updateProgress(phase: Int, sofar: Int, total: Int) {
        setProgressAsync(workDataOf(ConstantUtil.PHASE_KEY to phase, ConstantUtil.SOFAR_KEY to sofar,
            ConstantUtil.TOTAL_KEY to total))
    }

    companion object {
        const val TAG = "GetOrgDataWorker"
        private const val FETCH_EMPLOYMENTS = true
        private const val FETCH_ORGS = true
        private const val FETCH_COUNTRIES = true
        private const val BRIEF = true //TODO put the brief/full flag in the intent
    }
}