package com.trvzera.poketeam.data

data class PokemonLista(val results: List<PokemonResumo>)

data class PokemonResumo(
    val name: String,
    val url: String
) {
    // A listagem retorna o endereço do Pokémon, por exemplo: .../pokemon/25/.
    val numero: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()

    val imagem: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$numero.png"
}
