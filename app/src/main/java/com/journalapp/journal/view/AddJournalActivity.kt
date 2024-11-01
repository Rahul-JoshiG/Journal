package com.journalapp.journal.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.journalapp.journal.R
import com.journalapp.journal.databinding.ActivityAddJournalBinding
import com.journalapp.journal.model.Journal
import com.journalapp.journal.utils.ToastHelper

class AddJournalActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mBinding: ActivityAddJournalBinding
    private var mImageUri: Uri? = null

    // Firebase references
    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mUser: FirebaseUser

    private var mFireStore = FirebaseFirestore.getInstance()
    private var mStorageReference: StorageReference = FirebaseStorage.getInstance().reference
    private lateinit var mCollectionReference: CollectionReference

    private var mImage: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
            result?.let {
                mImageUri = result
                mBinding.cardView.visibility = INVISIBLE
                mBinding.showAddImage.visibility = VISIBLE
                mBinding.showAddImage.setImageURI(it)
            }
        }

    override fun onStart() {
        super.onStart()
        mUser = mFirebaseAuth.currentUser ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()

            // Redirect to LogInActivity
            val intent = Intent(this@AddJournalActivity, LogInActivity::class.java)
            startActivity(intent)

            // Close the current activity
            finish()
            return
        }

        // Proceed if the user is logged in
        mCollectionReference = mFireStore.collection("${mUser.uid}_Journals")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_journal)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.addJournalImage.setOnClickListener(this)
        mBinding.postButton.setOnClickListener(this)
        mBinding.changePictureBtn.setOnClickListener(this)
        mBinding.progressBar.visibility = INVISIBLE
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.add_journal_image -> openGallery()
            R.id.post_button -> saveJournal()
            R.id.change_picture_btn->openGallery()
        }
    }

    private fun saveJournal() {
        val title = mBinding.addJournalTitle.text.toString()
        val desc = mBinding.addJournalDescription.text.toString()
        val currentUser = mFirebaseAuth.currentUser

        if (title.isNotEmpty() && desc.isNotEmpty() && mImageUri != null) {
            // Show the progress bar and disable UI elements
            mBinding.progressBar.visibility = VISIBLE
            mBinding.addJournalTitle.isEnabled = false
            mBinding.addJournalDescription.isEnabled = false
            mBinding.addJournalImage.isEnabled = false
            mBinding.postButton.isEnabled = false
            mBinding.showAddImage.isEnabled = false

            val filePath = mStorageReference.child("${mUser.uid}_Journals_images/${mUser.uid}_image_${Timestamp.now().seconds}")

            Log.d(TAG, "Uploading to path: ${filePath.path} ${mUser.displayName}")

            filePath.putFile(mImageUri!!)
                .addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        val imageUri = uri.toString()

                        // Create a temporary Journal object without the `id` field set
                        val journal = Journal(
                            title = title,
                            description = desc,
                            imageUrl = imageUri,
                            date = Timestamp.now(),
                            name = currentUser?.displayName ?: ""
                        )

                        // Add the journal document to Firestore
                        mCollectionReference.add(journal)
                            .addOnSuccessListener { documentReference ->
                                // Retrieve and set the Firestore document ID as the `id` field in the Journal document
                                val journalId = documentReference.id
                                documentReference.update("id", journalId)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Journal saved with ID: $journalId")

                                        // Hide progress and navigate to the Dashboard
                                        mBinding.progressBar.visibility = INVISIBLE
                                        val intent = Intent(this, DashBoardActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        showError("Error updating journal ID: ${e.message}")
                                    }
                            }
                            .addOnFailureListener { e ->
                                showError("Error adding journal: ${e.message}")
                            }
                    }.addOnFailureListener { e ->
                        showError("Error retrieving image URL: ${e.message}")
                    }
                }.addOnFailureListener { e ->
                    showError("Error uploading image: ${e.message}")
                }
        } else {
            ToastHelper.showToast("Please fill all the fields and add an image")
        }
    }


    private fun showError(errorMessage: String?) {
        Log.e(TAG, "Error: $errorMessage")
        ToastHelper.showToast("$errorMessage")
        // Hide the progress bar and make UI elements visible again
        mBinding.progressBar.visibility = INVISIBLE
        mBinding.addJournalTitle.isEnabled = true
        mBinding.addJournalDescription.isEnabled = true
        mBinding.addJournalImage.isEnabled = true
        mBinding.postButton.isEnabled = true
        mBinding.showAddImage.isEnabled = true
    }

    private fun openGallery() {
        mImage.launch("image/*")
    }

    companion object {
        private const val TAG = "Add Journal Activity"
    }
}
