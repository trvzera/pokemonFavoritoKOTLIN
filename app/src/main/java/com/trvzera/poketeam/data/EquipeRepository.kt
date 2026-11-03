package com.trvzera.poketeam.data

class EquipeRepository(private val dao: EquipeDao) {
    suspend fun carregar(usuarioId: Long): List<MembroEquipe> {
        require(usuarioId > 0) { "Usuário inválido." }
        return dao.carregar(usuarioId)
    }

    suspend fun salvar(usuarioId: Long, equipe: List<MembroEquipe>) {
        require(usuarioId > 0 && equipe.all { it.usuarioId == usuarioId }) {
            "A equipe deve pertencer ao usuário conectado."
        }
        RegrasEquipe.validar(equipe)
        dao.substituir(usuarioId, equipe.mapIndexed { indice, membro -> membro.copy(posicao = indice) })
    }
}
