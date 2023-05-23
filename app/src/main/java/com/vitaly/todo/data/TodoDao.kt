package com.vitaly.todo.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos")
    suspend fun getAllTodos(): List<Todo>

    @Insert
    suspend fun insert(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)
}