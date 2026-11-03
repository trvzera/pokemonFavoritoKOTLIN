package com.trvzera.poketeam.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "membros_equipe",
    primaryKeys = ["usuarioId", "pokemonId"],
    indices = [Index(value = ["usuarioId", "posicao"], unique = true)],
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MembroEquipe(
    val usuarioId: Long,
    val pokemonId: Int,
    val nome: String,
    val imagem: String?,
    val tipos: String,
    val posicao: Int
)
