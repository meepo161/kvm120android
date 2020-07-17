package ru.avem.navitest.database.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ProtocolDot : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var dateDot: String = ""
    var timeDot: String = ""
    var rms: String = ""
    var avr: String = ""
    var amp: String = ""
    var freq: String = ""
    var coefamp: String = ""
    var coefdop: String = ""
}
