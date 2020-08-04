package ru.avem.navitest.database.dot

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProtocolDotDao {
    @Query("SELECT * FROM protocol_dot WHERE id = :id")
    fun get(id: Long): ProtocolDot

    @Query("SELECT * FROM protocol_dot")
    fun getAll(): List<ProtocolDot>

    @Query("SELECT * FROM protocol_dot")
    fun getAllAsync(): LiveData<List<ProtocolDot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg protocolDot: ProtocolDot)

    @Query("DELETE FROM protocol_dot WHERE id = :id")
    fun deleteById(id: Long)
}
