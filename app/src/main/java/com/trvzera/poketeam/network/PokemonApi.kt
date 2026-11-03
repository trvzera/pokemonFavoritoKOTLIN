package com.trvzera.poketeam.network

import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.data.PokemonLista
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {
    @GET("pokemon")
    suspend fun listarPokemons(@Query("limit") limite: Int = 20): PokemonLista

    @GET("pokemon/{nomeOuNumero}")
    suspend fun buscarPokemon(@Path("nomeOuNumero") nomeOuNumero: String): Pokemon
}
