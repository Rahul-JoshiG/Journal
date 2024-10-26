package com.journalapp.journal.adapters

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.journalapp.journal.databinding.JournalLayoutBinding
import com.journalapp.journal.model.Journal

class JournalAdapter(private var journal: List<Journal>) :
    RecyclerView.Adapter<JournalAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder: view holder created")
        val binding =
            JournalLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: bind views in adapter")
        val currentJournal = journal[position]
        holder.bind(currentJournal)
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${journal.size}")
        return journal.size
    }

    // ViewHolder class to bind views
    class ViewHolder(private val binding: JournalLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(journal: Journal) {
            val timeAgo =
                DateUtils.getRelativeTimeSpanString(journal.date.seconds * 1000L).toString()
            binding.journalDate.text = timeAgo
            binding.journalTitle.text = journal.title
            binding.journalDescription.text = journal.description
            setImage(journal.imageUrl)
        }

        private fun setImage(url: String?) {
            Log.d(TAG, "setImage: setting up images")
            Glide.with(binding.journalImage.context)
                .load(url)
                .apply(RequestOptions().centerCrop())
                .into(binding.journalImage)
        }
    }

    companion object {
        private const val TAG = "JournalAdapter"
    }
}
