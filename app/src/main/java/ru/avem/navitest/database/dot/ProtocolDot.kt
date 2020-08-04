package ru.avem.navitest.database.dot

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "protocol_dot")
data class ProtocolDot(
    var dateDot: String = "",
    var timeDot: String = "",
    var rms: String = "",
    var avr: String = "",
    var amp: String = "",
    var freq: String = "",
    var coefamp: String = "",
    var coefform: String = ""
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    override fun toString() = "$dateDot $timeDot"
}
