package com.trvzera.poketeam.data

import com.google.gson.annotations.SerializedName

// Estes nomes correspondem aos campos retornados pela PokéAPI.
data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: PokemonSprites,
    val types: List<PokemonTipo>,
    val abilities: List<PokemonHabilidade>? = null,
    val stats: List<PokemonAtributo>? = null
)

data class PokemonSprites(
    @SerializedName("front_default") val imagem: String?,
    val other: PokemonOutrasImagens? = null
)

data class PokemonOutrasImagens(
    @SerializedName("official-artwork") val arteOficial: PokemonArte?
)

data class PokemonArte(@SerializedName("front_default") val imagem: String?)

data class PokemonHabilidade(val ability: Tipo)

data class PokemonAtributo(
    @SerializedName("base_stat") val valor: Int,
    val stat: Tipo
)

data class PokemonTipo(
    val slot: Int,
    val type: Tipo
)

data class Tipo(val name: String)
