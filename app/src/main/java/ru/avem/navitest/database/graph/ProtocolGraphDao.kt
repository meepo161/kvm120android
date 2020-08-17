package ru.avem.navitest.database.graph

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProtocolGraphDao {
    @Query("SELECT * FROM protocol_graph WHERE id = :id")
    fun get(id: Long): ProtocolGraph

    @Query("SELECT * FROM protocol_graph")
    fun getAll(): List<ProtocolGraph>

    @Query("SELECT * FROM protocol_graph")
    fun getAllAsync(): LiveData<List<ProtocolGraph>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg protocolGraph: ProtocolGraph)

    @Query("DELETE FROM protocol_graph WHERE id = :id")
    fun deleteById(id: Long)
}
