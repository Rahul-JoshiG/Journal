package com.journalapp.journal.model

import com.google.firebase.Timestamp

data class Journal(
    var title: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var date: Timestamp = Timestamp.now(),
    var name: String = "",
    var id: String = ""
)
