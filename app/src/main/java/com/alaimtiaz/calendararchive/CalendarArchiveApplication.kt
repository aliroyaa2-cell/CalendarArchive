package com.alaimtiaz.calendararchive

import android.app.Application
import com.alaimtiaz.calendararchive.data.AppDatabase

class CalendarArchiveApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    companion object {
        @Volatile
        private var INSTANCE: CalendarArchiveApplication? = null

        fun getInstance(): CalendarArchiveApplication {
            return INSTANCE ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}
