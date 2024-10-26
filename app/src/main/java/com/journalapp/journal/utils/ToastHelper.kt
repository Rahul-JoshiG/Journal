package com.journalapp.journal.utils

import android.content.Context
import android.widget.Toast

object ToastHelper {

    private lateinit var appContext:Context

    fun initToast(context: Context){
        appContext = context.applicationContext
    }

    fun showToast(message: String){
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }
}