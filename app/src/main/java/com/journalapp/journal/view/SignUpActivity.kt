package com.journalapp.journal.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.journalapp.journal.R
import com.journalapp.journal.databinding.ActivitySignUpBinding
import com.journalapp.journal.utils.ToastHelper

class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivitySignUpBinding

    //for auth
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseAuthListener: FirebaseAuth.AuthStateListener
    private var mCurrentUser: FirebaseUser? = null

    //firebase connection
    private lateinit var mDatabaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        setOnClickListener()

        //firebase auth
        mFirebaseAuth = FirebaseAuth.getInstance()

        mFirebaseAuthListener = FirebaseAuth.AuthStateListener {
            mCurrentUser = mFirebaseAuth.currentUser
        }

    }

    private fun createUserEmailAccount(user: String, email: String, password: String) {
        Log.d(TAG, "createUserEmailAccount: create new account with($user, $email, $password)")
        if (user.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            mBinding.progressBar.visibility = VISIBLE
            mBinding.signUpBtn.isEnabled = false

            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mCurrentUser = mFirebaseAuth.currentUser
                        // Initialize Realtime Database reference to Users node
                        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

                        // Create a map to store user data
                        val userData = mapOf(
                            "name" to user,
                            "email" to email,
                            "password" to password
                        )

                        // Store user data under a child node with their UID as the key
                        mDatabaseReference.child(mCurrentUser!!.uid)
                            .setValue(userData)
                            .addOnSuccessListener {
                                ToastHelper.showToast("Sign Up Successful")
                                openActivity(LogInActivity::class.java)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Failed to save user data: ${e.message}")
                                ToastHelper.showToast("Failed to save user data.")
                                mBinding.progressBar.visibility = INVISIBLE
                                mBinding.signUpBtn.isEnabled = true
                            }
                    } else {
                        Log.e(TAG, "Sign Up failed: ${task.exception?.message}")
                        mBinding.progressBar.visibility = INVISIBLE
                        mBinding.signUpBtn.isEnabled = true
                        ToastHelper.showToast("${task.exception?.message}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error creating user: ${exception.message}")
                    mBinding.progressBar.visibility = INVISIBLE
                    mBinding.signUpBtn.isEnabled = true
                }
        } else {
            mBinding.progressBar.visibility = INVISIBLE
            mBinding.signUpBtn.isEnabled = true
            ToastHelper.showToast("Please fill all fields")
        }
    }


    private fun setOnClickListener() {
        Log.d(TAG, "setOnClickListener: clicked")
        mBinding.signUpBtn.setOnClickListener(this)
        mBinding.logInBtn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        Log.d(TAG, "onClick: $p0 clicked")
        when (p0?.id) {
            R.id.sign_up_btn -> createUserEmailAccount(
                mBinding.userEditText.text.toString(),
                mBinding.emailIdEditText.text.toString(),
                mBinding.passwordEditText.text.toString()
            )

            R.id.log_in_btn -> openActivity(LogInActivity::class.java)
        }
    }

    private fun openActivity(activityClass: Class<*>) {
        Log.d(TAG, "openLogInActivity: opening $activityClass")
        val intent = Intent(this@SignUpActivity, activityClass)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "SignUp Activity"
    }
}