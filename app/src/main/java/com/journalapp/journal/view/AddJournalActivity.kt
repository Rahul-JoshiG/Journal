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
                mBinding.cardView.visibility = View.INVISIBLE
                mBinding.showAddImage.visibility = View.VISIBLE
                mBinding.showAddImage.setImageURI(it)
            }
        }

    override fun onStart() {
        super.onStart()
        mUser = mFirebaseAuth.currentUser ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
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
        mBinding.progressBar.visibility = INVISIBLE
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.add_journal_image -> openGallery()
            R.id.post_button -> saveJournal()
        }
    }

    private fun saveJournal() {
        val title = mBinding.addJournalTitle.text.toString()
        val desc = mBinding.addJournalDescription.text.toString()
        val currentUser = mFirebaseAuth.currentUser

        if (title.isNotEmpty() && desc.isNotEmpty() && mImageUri != null) {
            // Show progress bar and disable interactions
            mBinding.progressBar.visibility = VISIBLE
            mBinding.root.isEnabled = false  // Disable the entire layout to prevent user interaction

            val filePath = mStorageReference.child("${mUser.uid}_Journals_images/${mUser.uid}_image_${Timestamp.now().seconds}")

            Log.d(TAG, "Uploading to path: ${filePath.path} ${mUser.displayName}")

            filePath.putFile(mImageUri!!)
                .addOnSuccessListener {
                    filePath.downloadUrl.addOnSuccessListener { uri ->
                        val imageUri = uri.toString()

                        val journal = Journal(
                            title = title,
                            description = desc,
                            imageUrl = imageUri,
                            date = Timestamp.now(),
                            name = currentUser?.displayName ?: "",
                            id = mUser.uid
                        )

                        mCollectionReference.add(journal)
                            .addOnSuccessListener {
                                // Hide progress bar and re-enable interactions
                                mBinding.progressBar.visibility = INVISIBLE
                                mBinding.root.isEnabled = true  // Enable user interaction again

                                val intent = Intent(this, DashBoardActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding journal: ${e.message}")
                                mBinding.progressBar.visibility = INVISIBLE
                                mBinding.root.isEnabled = true  // Enable user interaction on failure
                                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                            }
                    }.addOnFailureListener { e ->
                        Log.e(TAG, "Error getting download URL: ${e.message}")
                        mBinding.progressBar.visibility = INVISIBLE
                        mBinding.root.isEnabled = true  // Enable user interaction on failure
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading image: ${e.message}")
                    mBinding.progressBar.visibility = INVISIBLE
                    mBinding.root.isEnabled = true  // Enable user interaction on failure
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill the fields and select an image", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openGallery() {
        mImage.launch("image/*")
    }

    companion object {
        private const val TAG = "Add Journal Activity"
    }
}
