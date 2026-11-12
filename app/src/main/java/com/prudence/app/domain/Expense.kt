package com.prudence.app.domain

import java.time.LocalDate

/** Domain type per GLOSSARY.md: a single recorded act of spending. UI never sees Room entities. */
data class Expense(
    val id: Long,
    val amountMinor: Long,
    val date: LocalDate,
    val categoryId: Long,
    val note: String?,
)
