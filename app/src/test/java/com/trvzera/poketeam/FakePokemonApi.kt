package com.trvzera.poketeam

import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.data.PokemonLista
import com.trvzera.poketeam.data.PokemonResumo
import com.trvzera.poketeam.data.PokemonSprites
import com.trvzera.poketeam.data.PokemonTipo
import com.trvzera.poketeam.data.Tipo
import com.trvzera.poketeam.network.PokemonApi

class FakePokemonApi : PokemonApi {
    val buscas = mutableListOf<String>()
    var chamadasLista = 0
    var limiteRecebido = 0
    val lista = PokemonLista(listOf(PokemonResumo("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/")))
    val pikachu = Pokemon(
        25, "pikachu", 4, 60,
        PokemonSprites("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"),
        listOf(PokemonTipo(1, Tipo("electric")))
    )
    var responderBusca: suspend (String) -> Pokemon = { pikachu }

    override suspend fun listarPokemons(limite: Int): PokemonLista {
        chamadasLista++
        limiteRecebido = limite
        return lista
    }

    override suspend fun buscarPokemon(nomeOuNumero: String): Pokemon {
        buscas.add(nomeOuNumero)
        return responderBusca(nomeOuNumero)
    }
}
