package com.journalapp.journal.interfaces

import com.journalapp.journal.model.Journal

interface IJournalAction {
    fun deleteJournal(journal:Journal)
    fun openJournal(journal: Journal)
}