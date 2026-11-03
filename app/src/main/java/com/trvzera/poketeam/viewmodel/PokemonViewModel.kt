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
import retrofit2.HttpException

class PokemonViewModel(
    private val repository: PokemonRepository = PokemonRepository()
) : ViewModel() {
    var busca by mutableStateOf("")
        private set

    var estado: PokemonUiState by mutableStateOf(PokemonUiState.Carregando)
        private set

    private var ultimaConsulta = ""
    private var consulta: Job? = null

    init {
        consultar("")
    }

    fun atualizarBusca(texto: String) {
        busca = texto
    }

    fun buscar() {
        consultar(busca.trim())
    }

    fun mostrarLista() {
        busca = ""
        consultar("")
    }

    fun tentarNovamente() {
        consultar(ultimaConsulta)
    }

    private fun consultar(termo: String) {
        // Uma nova busca cancela a anterior para não mostrar uma resposta antiga.
        consulta?.cancel()
        ultimaConsulta = termo
        estado = PokemonUiState.Carregando

        consulta = viewModelScope.launch {
            try {
                estado = if (termo.isBlank()) {
                    PokemonUiState.Lista(repository.listarPokemons())
                } else {
                    PokemonUiState.Resultado(repository.buscarPokemon(termo))
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: HttpException) {
                estado = PokemonUiState.Erro(
                    if (erro.code() == 404) {
                        "Pokémon não encontrado. Confira o nome ou o número informado."
                    } else {
                        "A PokéAPI está indisponível. Tente novamente em instantes."
                    }
                )
            } catch (erro: IOException) {
                estado = PokemonUiState.Erro(
                    "Não foi possível carregar os Pokémon. Confira sua conexão e tente de novo."
                )
            } catch (erro: Exception) {
                estado = PokemonUiState.Erro("Não foi possível ler os dados dos Pokémon.")
            }
        }
    }
}
