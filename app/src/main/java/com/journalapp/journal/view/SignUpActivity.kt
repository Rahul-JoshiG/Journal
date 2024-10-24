package com.journalapp.journal.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.journalapp.journal.R
import com.journalapp.journal.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivitySignUpBinding

    //for auth
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mFirebaseAuthListener: FirebaseAuth.AuthStateListener
    private var mCurrentUser: FirebaseUser? = null

    //firebase connection
    private val mFireStore = FirebaseFirestore.getInstance()
    private val mCollectionReference = mFireStore.collection("Users")


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
            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@SignUpActivity, "Sign Up successful", Toast.LENGTH_SHORT).show()
                        openActivity(LogInActivity::class.java)
                    } else {
                        Log.e(TAG, "Sign Up failed: ${task.exception?.message}")
                        Toast.makeText(this@SignUpActivity, "${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error creating user: ${exception.message}")
                    Toast.makeText(this@SignUpActivity, "${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this@SignUpActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setOnClickListener() {
        Log.d(TAG, "setOnClickListener: clicked")
        mBinding.signUpBtn.setOnClickListener(this)
        mBinding.googleImageBtn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        Log.d(TAG, "onClick: $p0 clicked")
        when (p0?.id) {
            R.id.sign_up_btn -> createUserEmailAccount(
                mBinding.userEditText.text.toString(),
                mBinding.emailIdEditText.text.toString(),
                mBinding.passwordEditText.text.toString()
            )
        }
    }

    private fun openActivity(activityClass: Class<*>) {
        Log.d(TAG, "openLogInActivity: opening $activityClass")
        val intent = Intent(this@SignUpActivity, activityClass)
        startActivity(intent)
    }


    companion object {
        private const val TAG = "SignUp Activity"
    }
}