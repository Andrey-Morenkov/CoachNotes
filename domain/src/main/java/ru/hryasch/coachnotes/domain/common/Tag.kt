package ru.hryasch.coachnotes.domain.common

open class Tag(val text: String)

class PersonTag(text: String): Tag(text)

class GroupTag(text: String): Tag(text)