package org.akvo.rsr.up.worker

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class WorkManagerTestRule : TestWatcher() {
    lateinit var targetContext: Context
    lateinit var testContext: Context
    lateinit var configuration: Configuration
    lateinit var workManager: WorkManager
    lateinit var testDriver: TestDriver

    override fun starting(description: Description?) {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        testContext = InstrumentationRegistry.getInstrumentation().context
        configuration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(targetContext, configuration)
        workManager = WorkManager.getInstance(targetContext)
        testDriver = WorkManagerTestInitHelper.getTestDriver(targetContext)!!
    }
}
