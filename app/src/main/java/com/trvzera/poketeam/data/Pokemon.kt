package com.trvzera.poketeam.data

import com.google.gson.annotations.SerializedName

// Estes nomes correspondem aos campos retornados pela PokéAPI.
data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: PokemonSprites,
    val types: List<PokemonTipo>
)

data class PokemonSprites(
    @SerializedName("front_default") val imagem: String?
)

data class PokemonTipo(
    val slot: Int,
    val type: Tipo
)

data class Tipo(val name: String)
