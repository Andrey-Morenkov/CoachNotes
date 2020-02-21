package ru.hryasch.coachnotes.domain.common

open class Label(protected val text: String)

class PersonLabel(text: String): Label(text)

class GroupLabel(text: String): Label(text)