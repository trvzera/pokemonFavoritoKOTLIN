package com.trvzera.poketeam.data

interface SessaoStore {
    fun lerUsuarioId(): Long?
    fun salvarUsuarioId(id: Long)
    fun limpar()
}
