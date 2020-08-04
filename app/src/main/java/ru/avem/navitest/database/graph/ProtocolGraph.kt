package ru.avem.navitest.database.graph

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocol_graph")
data class ProtocolGraph(
    var date: String = "",
    var time: String = "",
    var typeOfValue: String = "",
    var values: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    override fun toString() = typeOfValue
}
