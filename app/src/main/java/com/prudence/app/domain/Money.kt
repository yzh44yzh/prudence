package com.prudence.app.domain

/**
 * Money is stored as a [Long] of minor units (kopecks/cents). Never floating point.
 * Display currency is a formatting concern only (settings ticket adds the app-wide choice).
 */
object Money {

    private val pattern = Regex("""\d+(\.\d{1,2})?""")

    /** Parses user input like "42", "42.1", "42.10" into minor units. Null when invalid or overflowing. */
    fun parseMinor(input: String): Long? {
        val s = input.trim()
        if (!pattern.matches(s)) return null
        return try {
            s.toBigDecimal().movePointRight(2).longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }

    /** Formats minor units for display, e.g. 4210 -> "42.10". Display currency arrives with the settings ticket. */
    fun format(minor: Long, symbol: String = ""): String =
        "%s%d.%02d".format(symbol, minor / 100, minor % 100)
}
