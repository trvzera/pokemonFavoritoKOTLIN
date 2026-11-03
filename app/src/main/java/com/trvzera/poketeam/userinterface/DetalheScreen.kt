package com.trvzera.poketeam.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trvzera.poketeam.data.MembroEquipe
import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.viewmodel.DetalheViewModel
import com.trvzera.poketeam.viewmodel.PokemonUiState
import java.util.Locale

@Composable
fun DetalheScreen(
    pokemonId: Int,
    equipe: List<MembroEquipe>,
    salvando: Boolean,
    onVoltar: () -> Unit,
    onAdicionar: (Pokemon) -> Unit,
    onEquipe: () -> Unit,
    viewModel: DetalheViewModel = viewModel()
) {
    LaunchedEffect(pokemonId) { viewModel.carregar(pokemonId) }
    val estado = viewModel.estado
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Voltar") }
                Text("Detalhes do Pokémon", style = MaterialTheme.typography.titleLarge)
            }
        }
        when (estado) {
            PokemonUiState.Carregando -> item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator()
                    Text("Carregando Pokémon...")
                }
            }
            is PokemonUiState.Erro -> item {
                Text(estado.mensagem)
                Button(onClick = viewModel::tentarNovamente) { Text("Tentar novamente") }
            }
            is PokemonUiState.Resultado -> {
                val pokemon = estado.pokemon
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NumeroPokemon(pokemon.id)
                        Text(NomePokemon(pokemon.name), style = MaterialTheme.typography.headlineLarge)
                        ImagemPokemon(
                            pokemon.sprites.other?.arteOficial?.imagem ?: pokemon.sprites.imagem,
                            pokemon.name, Modifier.size(220.dp)
                        )
                        TipoBadge(pokemon.types.sortedBy { it.slot }.joinToString(" / ") { it.type.name })
                    }
                }
                item {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Informações", style = MaterialTheme.typography.titleMedium)
                            Text("Altura: ${medida(pokemon.height)} m")
                            Text("Peso: ${medida(pokemon.weight)} kg")
                            Text("Habilidades: " + pokemon.abilities.orEmpty().joinToString(" / ") { NomePokemon(it.ability.name) })
                        }
                    }
                }
                item {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Atributos", style = MaterialTheme.typography.titleMedium)
                            pokemon.stats.orEmpty().forEach { atributo ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${nomeAtributo(atributo.stat.name)}: ${atributo.valor}")
                                    LinearProgressIndicator(
                                        progress = { (atributo.valor / 255f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    val naEquipe = equipe.any { it.pokemonId == pokemon.id }
                    val cheia = equipe.size >= 6
                    Button(
                        onClick = { onAdicionar(pokemon) },
                        enabled = !naEquipe && !cheia && !salvando,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(when {
                            naEquipe -> "Na equipe"
                            cheia -> "Equipe cheia"
                            else -> "Adicionar à equipe (${equipe.size}/6)"
                        })
                    }
                    TextButton(onClick = onEquipe, modifier = Modifier.fillMaxWidth()) { Text("Ver minha equipe") }
                }
            }
            is PokemonUiState.Lista -> Unit
        }
    }
}

private fun medida(valor: Int): String = String.format(Locale("pt", "BR"), "%.1f", valor / 10.0)

private fun nomeAtributo(nome: String): String = when (nome) {
    "hp" -> "HP"
    "attack" -> "Ataque"
    "defense" -> "Defesa"
    "special-attack" -> "Ataque especial"
    "special-defense" -> "Defesa especial"
    "speed" -> "Velocidade"
    else -> NomePokemon(nome)
}
