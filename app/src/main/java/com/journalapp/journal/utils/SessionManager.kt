package com.journalapp.journal.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SessionManager {
    private const val TAG = "Session Manager"
    private lateinit var mSharedPreferences: SharedPreferences

    fun initPreference(context: Context){
        Log.d(TAG, "initPreference: ")
        mSharedPreferences = context.getSharedPreferences(Constant.APP.PREFERENCE_NAME, Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String){
        Log.d(TAG, "putString: $key = $value")
        mSharedPreferences.edit().putString(key, value).apply()
    }

    fun putBoolean(key:String, value:Boolean){
        Log.d(TAG, "putBoolean: $key = $value")
        mSharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key:String): Boolean{
        Log.d(TAG, "getBoolean: $key")
        return mSharedPreferences.getBoolean(key,false)
    }
    fun removeKey(key:String){
        Log.d(TAG, "removeKey: removing key $key")
        mSharedPreferences.edit().remove(key).apply()
    }
}