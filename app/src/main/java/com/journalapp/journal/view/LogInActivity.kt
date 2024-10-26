package com.journalapp.journal.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.journalapp.journal.R
import com.journalapp.journal.databinding.ActivityLoginBinding
import com.journalapp.journal.utils.Constant
import com.journalapp.journal.utils.SessionManager
import com.journalapp.journal.utils.ToastHelper

class LogInActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityLoginBinding
    private val mFirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rememberMe()
        setOnClickListener()

    }

    private fun rememberMe() {
        Log.d(TAG, "rememberMe: remember me")
        mBinding.rememberMe.setOnCheckedChangeListener{_,_->
            SessionManager.putBoolean(Constant.Keys.IS_LOGGED_IN, true)
            SessionManager.putString(Constant.Keys.EMAIL_KEY,mBinding.emailTextView.text.toString().trim())
        }
    }

    override fun onClick(p0: View?) {
        Log.d(TAG, "onClick: clicking $p0")
        when (p0?.id) {
            R.id.sign_up_btn -> openActivity(SignUpActivity::class.java)
            R.id.login_btn -> loginUsingEmailPassword(
                mBinding.emailTextView.text.toString().trim(),
                mBinding.passwordTextView.text.toString().trim()
            )

            R.id.forget_text_view -> forgetPassword(mBinding.emailTextView.text.toString().trim())

        }
    }

    private fun forgetPassword(email: String) {
        Log.d(TAG, "forgetPassword: forget password method called")
        if(email.isEmpty())
        {
            ToastHelper.showToast("Please provide email")
            return
        }
        mFirebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            ToastHelper.showToast("Reset Password Email sent in your email")
        }
    }

    private fun openActivity(activityClass: Class<*>) {
        Log.d(TAG, "openActivity: opening $activityClass")
        val intent = Intent(this@LogInActivity, activityClass)
        val bundle = Bundle()
        bundle.putString(Constant.Keys.EMAIL_KEY,mBinding.emailTextView.text.toString().trim())
        intent.putExtras(bundle)
        startActivity(intent)
        finish()
    }

    private fun loginUsingEmailPassword(email: String, password: String) {
        Log.d(TAG, "loginUsingEmailPassword: login using email and Password")
        if (email.isNotEmpty() && password.isNotEmpty()) {
            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    openActivity(DashBoardActivity::class.java)
                }.addOnFailureListener {
                    ToastHelper.showToast("User Doesn't Exists")
                }
        }
    }

    private fun setOnClickListener() {
        Log.d(TAG, "setOnClickListener: button clicked")
        mBinding.signUpBtn.setOnClickListener(this)
        mBinding.loginBtn.setOnClickListener(this)
        mBinding.forgetTextView.setOnClickListener(this)
    }

    companion object {
        private const val TAG = "LogIn Activity"
    }
}