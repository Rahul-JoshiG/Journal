package com.journalapp.journal.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journalapp.journal.R
import com.journalapp.journal.utils.Constant
import com.journalapp.journal.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkedIsLoggedIn()
    }

    private fun checkedIsLoggedIn() {
        Log.d(TAG, "checkedIsLoggedIn: check that user is already logged in or not")
        val isLoggedIn = SessionManager.getBoolean(Constant.Keys.IS_LOGGED_IN)
        Log.d(TAG, "checkedIsLoggedIn: $isLoggedIn")
        if(isLoggedIn)
            openActivity(DashBoardActivity::class.java)
        else
            openActivity(LogInActivity::class.java)

    }

    private fun openActivity(activity: Class<*>) {
        Log.d(TAG, "openActivity: opening $activity")
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, activity)
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN_TIME)
    }


    companion object{
        private const val TAG = "Splash Activity"
        private const val SPLASH_SCREEN_TIME = 3000L
    }
}