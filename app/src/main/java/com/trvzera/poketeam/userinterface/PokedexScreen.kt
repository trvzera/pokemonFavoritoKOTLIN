package com.trvzera.poketeam.userinterface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.trvzera.poketeam.R
import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.viewmodel.PokemonUiState
import com.trvzera.poketeam.viewmodel.PokemonViewModel
import java.util.Locale

@Composable
fun PokedexScreen(viewModel: PokemonViewModel = viewModel()) {
    val teclado = LocalSoftwareKeyboardController.current
    val estado = viewModel.estado
    val buscar = {
        teclado?.hide()
        viewModel.buscar()
    }

    Scaffold(modifier = Modifier.imePadding()) { margens ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(margens),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "pokéteam",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text("Explore os Pokémon", style = MaterialTheme.typography.headlineMedium)
            }
            item {
                OutlinedTextField(
                    value = viewModel.busca,
                    onValueChange = viewModel::atualizarBusca,
                    label = { Text("Buscar por nome ou número") },
                    placeholder = { Text("Ex.: pikachu ou 25") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { buscar() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = buscar, enabled = viewModel.busca.isNotBlank()) {
                        Text("Buscar")
                    }
                    TextButton(onClick = {
                        teclado?.hide()
                        viewModel.mostrarLista()
                    }) {
                        Text("Ver lista")
                    }
                }
            }

            when (estado) {
                PokemonUiState.Carregando -> item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Carregando Pokémon...")
                    }
                }

                is PokemonUiState.Erro -> item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(estado.mensagem)
                            Button(onClick = viewModel::tentarNovamente) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }

                is PokemonUiState.Lista -> {
                    item {
                        Text(
                            "Toque em um Pokémon para consultar seus dados.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(estado.pokemons, key = { it.numero }) { pokemon ->
                        OutlinedCard(
                            onClick = {
                                teclado?.hide()
                                viewModel.atualizarBusca(pokemon.name)
                                viewModel.buscar()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ImagemPokemon(pokemon.imagem, pokemon.name, Modifier.size(72.dp))
                                Column {
                                    NumeroPokemon(pokemon.numero)
                                    Text(nomePokemon(pokemon.name), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }

                is PokemonUiState.Resultado -> item {
                    ResultadoPokemon(estado.pokemon)
                }
            }
        }
    }
}

@Composable
private fun ResultadoPokemon(pokemon: Pokemon) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImagemPokemon(pokemon.sprites.imagem, pokemon.name, Modifier.size(96.dp))
                Column(modifier = Modifier.weight(1f)) {
                    NumeroPokemon(pokemon.id)
                    Text(nomePokemon(pokemon.name), style = MaterialTheme.typography.titleLarge)
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = pokemon.types.sortedBy { it.slot }.joinToString(" / ") { nomeTipo(it.type.name) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Text("Informações", style = MaterialTheme.typography.titleMedium)
            // A API informa altura em decímetros e peso em hectogramas.
            Text("Altura: ${medida(pokemon.height)} m")
            Text("Peso: ${medida(pokemon.weight)} kg")
        }
    }
}

@Composable
private fun ImagemPokemon(url: String?, nome: String, modifier: Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Imagem de ${nomePokemon(nome)}",
        placeholder = painterResource(R.drawable.ic_poketeam),
        error = painterResource(R.drawable.ic_poketeam),
        fallback = painterResource(R.drawable.ic_poketeam),
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Composable
private fun NumeroPokemon(numero: Int) {
    Text(
        text = "#${numero.toString().padStart(3, '0')}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
}

private fun nomePokemon(nome: String): String =
    nome.replace('-', ' ').replaceFirstChar { it.uppercase() }

private fun medida(valor: Int): String = String.format(Locale("pt", "BR"), "%.1f", valor / 10.0)

private fun nomeTipo(tipo: String): String = when (tipo) {
    "normal" -> "Normal"
    "fire" -> "Fogo"
    "water" -> "Água"
    "electric" -> "Elétrico"
    "grass" -> "Planta"
    "ice" -> "Gelo"
    "fighting" -> "Lutador"
    "poison" -> "Veneno"
    "ground" -> "Terra"
    "flying" -> "Voador"
    "psychic" -> "Psíquico"
    "bug" -> "Inseto"
    "rock" -> "Pedra"
    "ghost" -> "Fantasma"
    "dragon" -> "Dragão"
    "dark" -> "Sombrio"
    "steel" -> "Aço"
    "fairy" -> "Fada"
    else -> nomePokemon(tipo)
}
