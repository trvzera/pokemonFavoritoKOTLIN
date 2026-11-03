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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.viewmodel.PokemonUiState
import com.trvzera.poketeam.viewmodel.PokemonViewModel

@Composable
fun PokedexScreen(onDetalhe: (Int) -> Unit, viewModel: PokemonViewModel = viewModel()) {
    val teclado = LocalSoftwareKeyboardController.current
    val estado = viewModel.estado
    val buscar = {
        teclado?.hide()
        viewModel.buscar()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                Button(
                    onClick = buscar,
                    enabled = viewModel.busca.isNotBlank(),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Buscar")
                }
                TextButton(
                    onClick = {
                        teclado?.hide()
                        viewModel.mostrarLista()
                    },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
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
                        Button(
                            onClick = viewModel::tentarNovamente,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
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
                            onDetalhe(pokemon.numero)
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
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
                            Column(modifier = Modifier.weight(1f)) {
                                NumeroPokemon(pokemon.numero)
                                Text(NomePokemon(pokemon.name), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            is PokemonUiState.Resultado -> item {
                ResultadoBusca(estado.pokemon) {
                    teclado?.hide()
                    onDetalhe(estado.pokemon.id)
                }
            }
        }
    }
}

@Composable
private fun ResultadoBusca(pokemon: Pokemon, onDetalhe: () -> Unit) {
    OutlinedCard(
        onClick = onDetalhe,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImagemPokemon(pokemon.sprites.imagem, pokemon.name, Modifier.size(88.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumeroPokemon(pokemon.id)
                    Text(NomePokemon(pokemon.name), style = MaterialTheme.typography.titleLarge)
                    TipoBadge(pokemon.types.sortedBy { it.slot }.joinToString(" / ") { it.type.name })
                }
            }
            Text(
                "Ver detalhes",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
