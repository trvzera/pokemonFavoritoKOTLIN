package com.trvzera.poketeam.userinterface

import kotlinx.serialization.Serializable

@Serializable data object LoginRota
@Serializable data object CadastroRota
@Serializable data object PokedexRota
@Serializable data object EquipeRota
@Serializable data object PerfilRota
@Serializable data class DetalheRota(val pokemonId: Int)
