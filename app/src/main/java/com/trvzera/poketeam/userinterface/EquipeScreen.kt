package com.trvzera.poketeam.userinterface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trvzera.poketeam.data.MembroEquipe

@Composable
fun EquipeScreen(
    equipe: List<MembroEquipe>,
    alterada: Boolean,
    salvando: Boolean,
    onRemover: (Int) -> Unit,
    onExplorar: () -> Unit,
    onSalvar: () -> Unit,
    onDetalhe: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Minha equipe",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("${equipe.size}/6", modifier = Modifier.padding(12.dp))
                }
            }
        }
        item {
            Text(
                if (equipe.isEmpty()) "Sua aventura começa com um favorito." else "Seu time de favoritos",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (alterada) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Você tem alterações para salvar no perfil.",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        items(equipe, key = { it.pokemonId }) { membro ->
            OutlinedCard(
                onClick = { onDetalhe(membro.pokemonId) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ImagemPokemon(membro.imagem, membro.nome, Modifier.size(64.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NumeroPokemon(membro.pokemonId)
                        Text(NomePokemon(membro.nome), style = MaterialTheme.typography.titleMedium)
                        TipoBadge(membro.tipos)
                    }
                    IconButton(
                        onClick = { onRemover(membro.pokemonId) },
                        enabled = !salvando,
                        modifier = Modifier.semantics {
                            contentDescription = "Remover ${NomePokemon(membro.nome)} da equipe"
                        }
                    ) {
                        Text("×", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
        items((equipe.size until 6).toList(), key = { "vaga_$it" }) { vaga ->
            OutlinedCard(
                onClick = onExplorar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.background),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("+", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
                    Text("Vaga ${vaga + 1}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (equipe.isEmpty()) {
                    Text("Adicione até 6 Pokémon à sua equipe.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onExplorar, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Text("Explorar Pokédex")
                    }
                }
                Button(
                    onClick = onSalvar,
                    enabled = equipe.size in 1..6 && alterada && !salvando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text(if (salvando) "Salvando equipe..." else "Salvar equipe no perfil")
                }
                if (equipe.isNotEmpty() && !alterada) {
                    Text("Sua equipe está salva no perfil.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
