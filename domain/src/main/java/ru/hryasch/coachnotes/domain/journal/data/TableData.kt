package ru.hryasch.coachnotes.domain.journal.data

import ru.hryasch.coachnotes.domain.common.GroupId
import ru.hryasch.coachnotes.domain.person.data.Person
import java.time.LocalDate

class TableData(val groupId:    GroupId,
                val peopleData: List<Person>,
                val daysData:   List<LocalDate>,
                val cellsData:  List<List<CellData?>>)
