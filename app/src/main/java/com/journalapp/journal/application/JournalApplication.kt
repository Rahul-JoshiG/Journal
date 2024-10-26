package com.journalapp.journal.application

import android.app.Application
import android.util.Log
import com.journalapp.journal.utils.SessionManager
import com.journalapp.journal.utils.ToastHelper

class JournalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: $TAG")
        SessionManager.initPreference(this)
        ToastHelper.initToast(this)
    }
    companion object{
        private const val TAG = "Journal Application"
    }
}