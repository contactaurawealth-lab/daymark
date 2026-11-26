package com.daymark.app

import android.app.Application
import com.daymark.app.core.di.AppContainer
import com.daymark.app.core.di.DefaultAppContainer
import com.daymark.app.core.utilities.NotificationHelper

class DaymarkApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
        NotificationHelper.createNotificationChannels(this)
    }

    companion object {
        lateinit var instance: DaymarkApplication
            private set
    }
}
