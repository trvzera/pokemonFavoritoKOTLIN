package com.trvzera.poketeam.viewmodel

import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.data.PokemonResumo

sealed interface PokemonUiState {
    data object Carregando : PokemonUiState
    data class Lista(val pokemons: List<PokemonResumo>) : PokemonUiState
    data class Resultado(val pokemon: Pokemon) : PokemonUiState
    data class Erro(val mensagem: String) : PokemonUiState
}
