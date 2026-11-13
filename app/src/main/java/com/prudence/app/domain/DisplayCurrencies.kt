package com.prudence.app.domain

/**
 * Display currency choices (formatting only — SPEC.md §2: expenses carry no currency).
 * The single source for both the settings UI's choices and Money's placement rule.
 */
object DisplayCurrencies {
    /** "" = no symbol. */
    val ALL = listOf("", "$", "€", "₽", "BYN", "£")

    /** Symbols that follow the amount ("42.10 ₽"); the rest prefix ("$42.10"). */
    private val SUFFIX = setOf("₽", "BYN")

    fun isSuffix(symbol: String): Boolean = symbol in SUFFIX
}
