package ru.hryasch.coachnotes.repository.common

import java.time.LocalDate

typealias AbsoluteAge = Int
typealias RelativeAge = Int

fun AbsoluteAge.toRelative(): RelativeAge = LocalDate.now().year - this
fun RelativeAge.toAbsolute(): AbsoluteAge = LocalDate.now().year - this