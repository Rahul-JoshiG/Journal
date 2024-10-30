package com.journalapp.journal.adapters

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.journalapp.journal.R
import com.journalapp.journal.databinding.JournalLayoutBinding
import com.journalapp.journal.databinding.ShowSingleJournalBinding
import com.journalapp.journal.model.Journal

class JournalAdapter(
    private var journalList: List<Journal>
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        Log.d(TAG, "onCreateViewHolder: create view holder")
        val binding =
            JournalLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JournalViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: bind view holder")
        holder.bind(journalList[position])
    }

    override fun getItemCount(): Int = journalList.size

    class JournalViewHolder(
        private val binding: JournalLayoutBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(journal: Journal) {
            Log.d(TAG, "bind: bind the data")
            val timeAgo =
                DateUtils.getRelativeTimeSpanString(journal.date.seconds * 1000L).toString()
            binding.journalDate.text = timeAgo
            binding.journalTitle.text = journal.title
            binding.journalDescription.text = journal.description
            setImage(journal.imageUrl)

            // Handle click to show AlertDialog
            binding.root.setOnClickListener {
                showJournalDialog(journal)
            }
        }

        private fun setImage(url: String?) {
            Log.d(TAG, "setImage: setting image")
            Glide.with(binding.journalImage.context)
                .load(url)
                .apply(RequestOptions().centerCrop())
                .into(binding.journalImage)
        }

        private fun showJournalDialog(journal: Journal) {
            Log.d(TAG, "showJournalDialog: showing current journal")
            val dialogBinding = ShowSingleJournalBinding.inflate(LayoutInflater.from(context))
            dialogBinding.journalTitle.text = journal.title
            dialogBinding.journalDescription.text = journal.description
            Glide.with(binding.journalImage.context)
                .load(journal.imageUrl)
                .apply(RequestOptions().centerCrop())
                .into(dialogBinding.journalImage)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .setNegativeButton("Close") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(context, R.color.brown))

        }
    }

    companion object {
        private const val TAG = "JournalAdapter"
    }
}

