package com.prudence.app.domain

import java.time.format.DateTimeFormatter

/** The one app-wide date display format ("13 Nov 2026"). */
val AppDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
