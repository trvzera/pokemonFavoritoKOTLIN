package com.trvzera.poketeam.data

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["email"], unique = true)])
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val email: String,
    val senhaHash: String,
    val salt: String
)
