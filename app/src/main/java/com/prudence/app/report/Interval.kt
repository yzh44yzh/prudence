package com.prudence.app.report

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/**
 * The date range a report covers (GLOSSARY.md): inclusive of both endpoints.
 * Presets are the current ISO week / calendar month; custom is any start/end pair.
 */
data class Interval(val start: LocalDate, val end: LocalDate, val kind: Kind) {

    enum class Kind { WEEK, MONTH, CUSTOM }

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    /** Moves the interval by [periods] of its own kind/length (week→7d, month→calendar month, custom→its length). */
    fun shifted(periods: Int): Interval = when (kind) {
        Kind.WEEK -> copy(start = start.plusWeeks(periods.toLong()), end = end.plusWeeks(periods.toLong()))
        Kind.MONTH -> {
            val shiftedStart = start.plusMonths(periods.toLong())
            // End recomputed from the shifted start: Dec must end on the 31st even from Nov 30.
            copy(start = shiftedStart, end = shiftedStart.plusMonths(1).minusDays(1))
        }
        Kind.CUSTOM -> {
            val days = ChronoUnit.DAYS.between(start, end) + 1
            copy(start = start.plusDays(days * periods), end = end.plusDays(days * periods))
        }
    }

    companion object {
        fun thisWeek(today: LocalDate): Interval {
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) // ISO week
            return Interval(monday, monday.plusDays(6), Kind.WEEK)
        }

        fun thisMonth(today: LocalDate): Interval {
            val first = today.withDayOfMonth(1)
            return Interval(first, first.plusMonths(1).minusDays(1), Kind.MONTH)
        }

        /** Endpoints are coerced into order (end >= start). */
        fun custom(start: LocalDate, end: LocalDate): Interval =
            if (end < start) Interval(end, start, Kind.CUSTOM) else Interval(start, end, Kind.CUSTOM)
    }
}
