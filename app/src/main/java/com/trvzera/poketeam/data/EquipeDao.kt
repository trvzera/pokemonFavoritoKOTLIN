package com.trvzera.poketeam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class EquipeDao {
    @Query("SELECT * FROM membros_equipe WHERE usuarioId = :usuarioId ORDER BY posicao")
    abstract suspend fun carregar(usuarioId: Long): List<MembroEquipe>

    @Query("DELETE FROM membros_equipe WHERE usuarioId = :usuarioId")
    abstract suspend fun remover(usuarioId: Long)

    @Insert
    abstract suspend fun inserir(equipe: List<MembroEquipe>)

    // A remoção e a inserção só são confirmadas juntas; uma falha mantém a equipe anterior.
    @Transaction
    open suspend fun substituir(usuarioId: Long, equipe: List<MembroEquipe>) {
        remover(usuarioId)
        inserir(equipe)
    }
}
