package org.akvo.rsr.up.worker

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import org.akvo.rsr.up.R
import org.akvo.rsr.up.dao.RsrDbAdapter
import org.akvo.rsr.up.util.ConstantUtil
import org.akvo.rsr.up.util.ConstantUtil.PASSWORD_KEY
import org.akvo.rsr.up.util.ConstantUtil.USERNAME_KEY
import org.akvo.rsr.up.util.Downloader
import org.akvo.rsr.up.util.SettingsUtil
import org.akvo.rsr.up.util.Uploader
import java.net.URL

class SignInWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val username = inputData.getString(USERNAME_KEY)
        val password = inputData.getString(PASSWORD_KEY)
        val appContext = applicationContext

        return try {
            val user = Uploader.authorize(
                URL(SettingsUtil.host(appContext) + ConstantUtil.AUTH_URL),
                username,
                password
            )
            if (user != null) {
                // Yes!
                SettingsUtil.signIn(appContext, user)

                // use project list to set projects visible
                val dba = RsrDbAdapter(appContext)
                dba.open()
                dba.setVisibleProjects(user.publishedProjIds)
                // detailed employment list is short and will be useful on early logins
                Downloader().fetchEmploymentListPaged(
                    appContext,
                    dba,
                    URL(
                        SettingsUtil.host(appContext) + String.format(
                            ConstantUtil.FETCH_EMPLOYMENTS_URL_PATTERN,
                            SettingsUtil.getAuthUser(appContext).id
                        )
                    ),
                    null
                )
                // TODO maybe fetch countries and (minimal)organisations too (if never done before)
                dba.close()
                Result.success()
            } else {
                SettingsUtil.signOut(appContext)
                Result.failure(createOutputDataErrorCredentials(appContext))
            }
        } catch (throwable: Throwable) {
            Log.e(TAG, "SignIn error", throwable)
            SettingsUtil.signOut(appContext)
            Result.failure(createOutputDataWithError(appContext, throwable))
        }
    }

    private fun createOutputDataWithError(context: Context, throwable: Throwable): Data {
        return workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to context.resources.getString(R.string.errmsg_signin_failed) + throwable.message)
    }

    private fun createOutputDataErrorCredentials(context: Context): Data {
        return workDataOf(ConstantUtil.SERVICE_ERRMSG_KEY to context.resources.getString(R.string.errmsg_signin_denied))
    }

    companion object {
        const val TAG = "SignInWorker"
    }
}
