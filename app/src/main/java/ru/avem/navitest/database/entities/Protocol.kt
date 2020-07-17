package ru.avem.navitest.database.entities

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Protocol : RealmObject() {
    @PrimaryKey
    var id: Long = 0
    var date: String = ""
    var time: String = ""
    var typeOfValue: String = ""
    var values: String = ""

}

