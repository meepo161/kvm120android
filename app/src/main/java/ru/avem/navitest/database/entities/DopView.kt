package ru.avem.navitest.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable


object DopView : IntIdTable() {
    val rmsDop = bool("rmsDop")
    val avrDop = bool("avrDop")
    val ampDop = bool("ampDop")
    val freqDop = bool("freqDop")
    val coefAmpDop = bool("coefAmpDop")
    val coefDop = bool("coefDop")
}

class Dop(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Dop>(DopView)
    var rmsDop by DopView.rmsDop
    var avrDop by DopView.avrDop
    var ampDop by DopView.ampDop
    var freqDop by DopView.freqDop
    var coefAmpDop by DopView.coefAmpDop
    var coefDop by DopView.coefDop

    override fun toString(): String {
        return id.toString()
    }
}
