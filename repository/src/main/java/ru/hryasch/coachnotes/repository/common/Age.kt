package ru.hryasch.coachnotes.repository.common

import com.soywiz.klock.DateTime

typealias AbsoluteAge = Int
typealias RelativeAge = Int

fun AbsoluteAge.toRelative(): RelativeAge = DateTime.nowLocal().yearInt - this
fun RelativeAge.toAbsolute(): AbsoluteAge = DateTime.nowLocal().yearInt - this