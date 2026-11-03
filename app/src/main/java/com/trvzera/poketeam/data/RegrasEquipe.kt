package com.trvzera.poketeam.data

object RegrasEquipe {
    fun validar(equipe: List<MembroEquipe>) {
        require(equipe.size in 1..6) { "A equipe deve ter de 1 a 6 Pokémon." }
        require(equipe.map { it.pokemonId }.distinct().size == equipe.size) {
            "Não é permitido repetir Pokémon na equipe."
        }
        require(equipe.all { it.pokemonId > 0 && it.nome.isNotBlank() }) {
            "A equipe contém um Pokémon inválido."
        }
    }
}
