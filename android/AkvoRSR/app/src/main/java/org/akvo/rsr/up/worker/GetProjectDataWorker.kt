package org.akvo.rsr.up.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.akvo.rsr.up.R
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.domain.Project
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.Downloader
import org.akvo.rsr.up.util.FileUtil
import org.akvo.rsr.up.util.SettingsUtil
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashSet
import java.util.Locale
import java.util.TimeZone

class GetProjectDataWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val projectId = inputData.getString(ConstantUtil.PROJECT_ID_KEY)
        val appContext = applicationContext

        val ad = RsrDbAdapter(appContext)
        val dl = Downloader()
        var errMsg: String? = null
        val fetchImages = !SettingsUtil.ReadBoolean(appContext, "setting_delay_image_fetch", false)
        val fullSynch = SettingsUtil.ReadBoolean(appContext, "setting_fullsynch", false)
        val host = SettingsUtil.host(appContext)
        val start = System.currentTimeMillis()

        ad.open()
        val user = SettingsUtil.getAuthUser(appContext)
        try {
            try {
                // Make the list of projects to update
                val projectSet: MutableSet<String>
                if (projectId == null) {
                    projectSet = user.publishedProjIds
                } else {
                    projectSet = HashSet()
                    projectSet.add(projectId)
                }
                val projects = projectSet.size
                //Iterate over projects instead of using a complex query URL, since it can take so long that the proxy times out
                projectSet.withIndex().forEach { (i, id) ->
                    dl.fetchProject(appContext, ad, getProjectUpdateUrl(appContext, id)) //TODO: JSON
                    if (FETCH_RESULTS) {
                        dl.fetchProjectResultsPaged(appContext, ad, getResultsUrl(host, id))
                    }
                    setProgressAsync(workDataOf(ConstantUtil.PHASE_KEY to 0, ConstantUtil.SOFAR_KEY to i + 1,
                        ConstantUtil.TOTAL_KEY to projects))
                }
                // country list rarely changes, so only fetch countries if we never did that
                if (FETCH_COUNTRIES && (fullSynch || ad.countryCount == 0)) {
                    dl.fetchCountryListRestApiPaged(appContext, ad, getCountriesUrl(appContext))
                }
                setProgressAsync(workDataOf(ConstantUtil.PHASE_KEY to 0, ConstantUtil.SOFAR_KEY to 100,
                    ConstantUtil.TOTAL_KEY to 100))

                if (FETCH_UPDATES) {
                    val df1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    df1.timeZone = TimeZone.getTimeZone("UTC")
                    val fetchedIds = ArrayList<String>()
                    projectSet.withIndex().forEach { (k, projId) ->
                        val project = ad.findProject(projId)
                        if (project != null) {
                            //since last fetch or forever?
                            val url = getProjectUpdateUrl(projId, df1, fullSynch, project, host)
                            val date = dl.fetchUpdateListRestApiPaged(appContext, url, fetchedIds)
                            //fetch completed; remember fetch date of this project - other users of the app may have different project set
                            ad.updateProjectLastFetch(projId, date)
                            if (fullSynch) { //now delete those that went away
                                val removeIds = ad.getUpdatesForList(projId)
                                removeIds.removeAll(fetchedIds)
                                removeIds.forEach { id ->
                                    Log.i(TAG, "Deleting update $id")
                                    ad.deleteUpdate(id)
                                }
                            }
                        }
                        //show progress
                        //TODO this is *very* uninformative for a user with one project and many updates!
                        setProgressAsync(workDataOf(ConstantUtil.PHASE_KEY to 1, ConstantUtil.SOFAR_KEY to k + 1,
                            ConstantUtil.TOTAL_KEY to projects))

                    }
                }
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "Cannot find:", e)
                errMsg =
                    appContext.resources.getString(R.string.errmsg_not_found_on_server) + e.message
            } catch (e: Exception) {
                Log.e(TAG, "Bad updates fetch:", e)
                errMsg =
                    appContext.resources.getString(R.string.errmsg_update_fetch_failed) + e.message
            }
            if (FETCH_USERS) {
                val orgIds = ad.missingUsersList
                orgIds.forEach { id ->
                    try {
                        dl.fetchUser(appContext, ad, buildUserUrl(host, id), id)
                    } catch (e: Exception) { // probably network reasons
                        Log.e(TAG, "Bad user fetch:", e)
                        errMsg =
                            appContext.resources.getString(R.string.errmsg_user_fetch_failed) + e.message
                    }
                }
            }
            if (FETCH_ORGS) {
                // Fetch user data for the organisations of users.
                val orgIds = ad.missingOrgsList
                var j = 0
                orgIds.forEach { id ->
                    try {
                        dl.fetchOrg(appContext, ad, getOrgsUrl(host, id), id)
                        j++
                    } catch (e: Exception) { // probably network reasons
                        Log.e(TAG, "Bad org fetch:", e)
                        errMsg =
                            appContext.resources.getString(R.string.errmsg_org_fetch_failed) + e.message
                    }
                }
                Log.i(TAG, "Fetched $j orgs")
            }
            setProgressAsync(workDataOf(ConstantUtil.PHASE_KEY to 1, ConstantUtil.SOFAR_KEY to 100,
                ConstantUtil.TOTAL_KEY to 100))
            if (fetchImages) {
                try {
                    dl.fetchMissingThumbnails(appContext,
                        host,
                        FileUtil.getExternalCacheDir(appContext).toString()
                    ) { sofar, total ->
                        setProgressAsync(workDataOf(ConstantUtil.PHASE_KEY to 2, ConstantUtil.SOFAR_KEY to sofar,
                            ConstantUtil.TOTAL_KEY to total))

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

        return if (errMsg != null) {
            Result.failure(createOutputDataWithError(appContext, errMsg))
        } else {
            Result.success()
        }
    }

    private fun createOutputDataWithError(context: Context, message: String?): Data {
        return workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to context.resources.getString(R.string.errmsg_signin_failed) + message)
    }

    private fun getOrgsUrl(host: String, id: String?) = URL(host
            + String.format(Locale.US,
        ConstantUtil.FETCH_ORG_URL_PATTERN, id))

    private fun getCountriesUrl(appContext: Context) =
        URL(SettingsUtil.host(appContext) + String.format(ConstantUtil.FETCH_COUNTRIES_URL))

    private fun getResultsUrl(host: String, id: String) =
        URL(host + String.format(ConstantUtil.FETCH_RESULTS_URL_PATTERN, id))

    private fun getProjectUpdateUrl(
        appContext: Context,
        id: String,
    ) = URL(SettingsUtil.host(appContext) + String.format(ConstantUtil.FETCH_PROJ_URL_PATTERN, id))

    private fun getProjectUpdateUrl(
        projId: String,
        df1: SimpleDateFormat,
        fullSynch: Boolean,
        project: Project,
        host: String,
    ): URL {
        val u = String.format(ConstantUtil.FETCH_UPDATE_URL_PATTERN,
            projId,
            df1.format(if (fullSynch) 0 else project.lastFetch))
        return URL(host + u)
    }

    private fun buildUserUrl(host: String, id: String?) =
        URL(host + String.format(Locale.US, ConstantUtil.FETCH_USER_URL_PATTERN, id))

    companion object {
        const val TAG = "GetProjectDataWorker"
        private const val FETCH_USERS = true
        private const val FETCH_COUNTRIES = true
        private const val FETCH_UPDATES = true
        private const val FETCH_ORGS = true
        private const val FETCH_RESULTS = false //TODO: why is this false?
    }
}
