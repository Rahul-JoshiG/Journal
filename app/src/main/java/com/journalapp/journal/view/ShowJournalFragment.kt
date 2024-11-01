package com.journalapp.journal.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.journalapp.journal.databinding.FragmentShowJournalBinding
import com.journalapp.journal.model.Journal

class ShowJournalFragment(private val journalList: Journal) : Fragment() {

    private lateinit var mBinding: FragmentShowJournalBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        mBinding = FragmentShowJournalBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addJournalData()
    }

    private fun addJournalData() {
        Log.d(TAG, "addJournalData: adding journal data into fragment")
        mBinding.apply {
            journalTitle.text = journalList.title
            journalDescription.text = journalList.description
        }
        Glide.with(this)
            .load(journalList.imageUrl)
            .into(mBinding.journalImage)

    }


    companion object{
        private const val TAG = "Show Journal Fragment"
    }
}