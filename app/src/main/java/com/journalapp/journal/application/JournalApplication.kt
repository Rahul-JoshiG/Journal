package com.journalapp.journal.application

import android.app.Application
import android.util.Log
import com.journalapp.journal.utils.SessionManager

class JournalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: $TAG")
        SessionManager.initPreference(this)
    }
    companion object{
        private const val TAG = "Journal Application"
    }
}