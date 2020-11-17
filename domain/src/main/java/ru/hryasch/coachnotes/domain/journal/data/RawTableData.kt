package ru.hryasch.coachnotes.domain.journal.data

import ru.hryasch.coachnotes.domain.group.data.Group
import ru.hryasch.coachnotes.domain.person.data.Person
import java.time.LocalDate

class RawTableData(val group:      Group?,
                   val peopleData: List<Person>,
                   val daysData:   List<LocalDate>,
                   val cellsData:  List<List<CellData?>>)
