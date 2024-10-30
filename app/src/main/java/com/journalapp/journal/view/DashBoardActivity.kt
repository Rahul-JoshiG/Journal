package com.journalapp.journal.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import com.journalapp.journal.model.Journal
import com.journalapp.journal.utils.Constant
import com.journalapp.journal.utils.SessionManager
import com.journalapp.journal.utils.ToastHelper

class DashBoardActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityDashBoardBinding

    // Firebase auth
    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mUser: FirebaseUser

    // FireStore
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var mCollectionReference: CollectionReference

    // Adapter
    private lateinit var mAdapter: JournalAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: on create method in DashBoard Activity")
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
        Log.d(TAG, "openAddJournalActivity: opening add journal activity")
        val intent = Intent(this@DashBoardActivity, AddJournalActivity::class.java)
        startActivity(intent)
    }

    private fun initializeRecyclerView(list: ArrayList<Journal>) {
        Log.d(TAG, "initializeRecyclerView: initializing recycler view")
        // Initialize the adapter with an empty list initially
        mAdapter = JournalAdapter(list)
        // Set the adapter to the RecyclerView
        mBinding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@DashBoardActivity)
            addItemDecoration(
                DividerItemDecoration(this@DashBoardActivity, LinearLayoutManager.VERTICAL)
            )
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onCreateOptionsMenu: menu option")
        menuInflater.inflate(R.menu.journal_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: menu option select ")
        when (item.itemId) {
            R.id.sign_out -> {
                mFirebaseAuth.signOut()
                SessionManager.removeKey(Constant.Keys.IS_LOGGED_IN)
                val intent = Intent(this@DashBoardActivity, LogInActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: dashboard activity")

        mUser = mFirebaseAuth.currentUser ?: run {
            ToastHelper.showToast("User not logged in")
            finish()
            return
        }
        mCollectionReference = mFireStore.collection("${mUser.uid}_Journals")

        val journalList: ArrayList<Journal> = ArrayList()

        // Fetch data with date ordered in descending order to show the latest posts on top
        mCollectionReference.orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Clear previous data to avoid duplicates
                journalList.clear()

                // Populate the journalList with fetched data
                querySnapshot?.forEach { documentSnapshot ->
                    val journal = documentSnapshot.toObject(Journal::class.java)
                    journalList.add(journal)
                }

                initializeRecyclerView(journalList)

            }
            .addOnFailureListener {
                ToastHelper.showToast("Something went wrong...")
            }
    }

    companion object {
        private const val TAG = "DashBoard Activity"
    }
}
