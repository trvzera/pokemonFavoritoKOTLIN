package com.trvzera.poketeam.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trvzera.poketeam.data.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetalheViewModel(
    private val repository: PokemonRepository = PokemonRepository()
) : ViewModel() {
    var estado: PokemonUiState by mutableStateOf(PokemonUiState.Carregando)
        private set
    private var consulta: Job? = null
    private var id: Int? = null

    fun carregar(pokemonId: Int, forcar: Boolean = false) {
        if (!forcar && id == pokemonId && estado is PokemonUiState.Resultado) return
        id = pokemonId
        consulta?.cancel()
        estado = PokemonUiState.Carregando
        consulta = viewModelScope.launch {
            try {
                estado = PokemonUiState.Resultado(repository.buscarPokemon(pokemonId.toString()))
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: IOException) {
                estado = PokemonUiState.Erro("Não foi possível carregar o Pokémon. Confira sua conexão e tente de novo.")
            } catch (_: Exception) {
                estado = PokemonUiState.Erro("Não foi possível carregar os detalhes deste Pokémon.")
            }
        }
    }

    fun tentarNovamente() { id?.let { carregar(it, forcar = true) } }
}
