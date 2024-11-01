package com.journalapp.journal.view

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.journalapp.journal.R
import com.journalapp.journal.adapters.JournalAdapter
import com.journalapp.journal.databinding.ActivityDashBoardBinding
import com.journalapp.journal.interfaces.IJournalAction
import com.journalapp.journal.model.Journal
import com.journalapp.journal.utils.Constant
import com.journalapp.journal.utils.SessionManager
import com.journalapp.journal.utils.ToastHelper

@Suppress("DEPRECATION")
class DashBoardActivity : AppCompatActivity(), IJournalAction {
    private lateinit var mBinding: ActivityDashBoardBinding
    private var mShowJournalFragment: ShowJournalFragment? = null

    // Firebase auth
    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mUser: FirebaseUser

    // Fire store
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var mCollectionReference: CollectionReference

    // Adapter
    private lateinit var mAdapter: JournalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: DashBoardActivity initialized")
        enableEdgeToEdge()
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_dash_board)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(getColor(R.color.pink_700)))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mBinding.addNewJournalFab.setOnClickListener {
            openAddJournalActivity()
        }
    }

    private fun openAddJournalActivity() {
        Log.d(TAG, "openAddJournalActivity: opening AddJournalActivity")
        startActivity(Intent(this, AddJournalActivity::class.java))
    }

    private fun initializeRecyclerView(list: ArrayList<Journal>) {
        Log.d(TAG, "initializeRecyclerView: initializing RecyclerView")
        mAdapter = JournalAdapter(list, this)
        mBinding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@DashBoardActivity)
            addItemDecoration(DividerItemDecoration(this@DashBoardActivity, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.journal_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sign_out -> signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        mFirebaseAuth.signOut()
        SessionManager.removeKey(Constant.Keys.IS_LOGGED_IN)
        startActivity(Intent(this, LogInActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: loading user journals")

        mUser = mFirebaseAuth.currentUser ?: run {
            ToastHelper.showToast("User not logged in")
            finish()
            return
        }

        loadJournals()
    }

    private fun loadJournals() {
        Log.d(TAG, "loadJournals: fetching data from collections")
        mCollectionReference = mFireStore.collection("${mUser.uid}_Journals")
        val journalList = ArrayList<Journal>()

        mCollectionReference.orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                journalList.clear()
                querySnapshot?.forEach { document ->
                    val journal = document.toObject(Journal::class.java)
                    journalList.add(journal)
                }
                initializeRecyclerView(journalList)
            }
            .addOnFailureListener {
                ToastHelper.showToast("Something went wrong...")
            }
    }

    override fun deleteJournal(journal: Journal) {
        val brownColor = ContextCompat.getColor(this, R.color.brown)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure?")
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { _, _ -> deleteThisJournal(journal) }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(brownColor)
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(brownColor)
        }

        dialog.show()
    }

    private fun deleteThisJournal(journal: Journal) {
        Log.d(TAG, "deleteThisJournal: ")
        // Ensure that mCollectionReference points to the correct user-specific collection
        val documentRef = mFireStore.collection("${mUser.uid}_Journals").document(journal.id)

        documentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                Log.d(TAG, "Document exists. Proceeding with deletion.")

                // Delete the document
                documentRef.delete().addOnSuccessListener {
                    Log.d(TAG, "Document with ID ${journal.id} deleted successfully.")
                    ToastHelper.showToast("Journal deleted successfully.")
                    loadJournals() // Optionally refresh the journal list
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error deleting document", e)
                    ToastHelper.showToast("Failed to delete document. Please try again.")
                }
            } else {
                Log.d(TAG, "Document not found: ${documentRef.path}")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error fetching document before deletion", e)
        }

    }


    override fun openJournal(journal: Journal) {
        Log.d(TAG, "openJournal: opening ShowJournalFragment")

        // Show the fragment container if it's hidden
        mBinding.fragmentView.visibility = VISIBLE
        hideActivityData()

        // Avoid adding multiple instances of the fragment
        mShowJournalFragment = ShowJournalFragment(journal)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,  // enter
                R.anim.slide_out_left,  // exit
                R.anim.slide_in_left,   // popEnter
                R.anim.slide_out_right  // popExit
            )
            .replace(R.id.fragment_view, mShowJournalFragment!!)
            .addToBackStack(null) // Allows back navigation
            .commit()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        // If the fragment is visible, close it and hide the fragment view container
        if (mShowJournalFragment != null && mShowJournalFragment!!.isVisible) {
            supportFragmentManager.popBackStack()
            mBinding.fragmentView.visibility = View.GONE
            showActivityData()
        } else {
            super.onBackPressed()
        }
    }

    private fun hideActivityData() {
        Log.d(TAG, "hideActivityData: hiding activity data")
        mBinding.apply {
            addNewJournalFab.visibility = INVISIBLE
            recyclerView.visibility = INVISIBLE
            appBar.visibility = INVISIBLE
        }
    }

    private fun showActivityData() {
        Log.d(TAG, "showActivityData: showing activity data")
        mBinding.apply {
            addNewJournalFab.visibility = VISIBLE
            recyclerView.visibility = VISIBLE
            appBar.visibility = VISIBLE
        }
    }

    companion object {
        private const val TAG = "DashBoardActivity"
    }
}
