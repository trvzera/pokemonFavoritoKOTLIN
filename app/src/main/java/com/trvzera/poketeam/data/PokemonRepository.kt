package com.trvzera.poketeam.data

import com.trvzera.poketeam.network.PokemonApi
import com.trvzera.poketeam.network.RetrofitClient
import java.util.Locale

class PokemonRepository(private val api: PokemonApi = RetrofitClient.api) {
    // Cache em memória para evitar repetir consultas durante o uso da tela.
    private var lista: List<PokemonResumo>? = null
    private val pokemons = mutableMapOf<String, Pokemon>()

    suspend fun listarPokemons(): List<PokemonResumo> {
        lista?.let { return it }
        val resultado = api.listarPokemons().results
        lista = resultado
        return resultado
    }

    suspend fun buscarPokemon(nomeOuNumero: String): Pokemon {
        val texto = nomeOuNumero.trim().lowercase(Locale.ROOT)
        val consulta = texto.toIntOrNull()?.toString() ?: texto
        pokemons[consulta]?.let { return it }

        val pokemon = api.buscarPokemon(consulta)
        pokemons[pokemon.name] = pokemon
        pokemons[pokemon.id.toString()] = pokemon
        return pokemon
    }
}
