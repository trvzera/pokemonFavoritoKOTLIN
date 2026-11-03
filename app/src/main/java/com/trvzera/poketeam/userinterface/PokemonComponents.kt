package com.trvzera.poketeam.userinterface

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trvzera.poketeam.R
import java.util.Locale

@Composable
fun ImagemPokemon(url: String?, nome: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = "Imagem de ${NomePokemon(nome)}",
        placeholder = painterResource(R.drawable.ic_poketeam),
        error = painterResource(R.drawable.ic_poketeam),
        fallback = painterResource(R.drawable.ic_poketeam),
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

fun NomePokemon(nome: String): String =
    nome.replace('-', ' ').replaceFirstChar { it.uppercase() }

fun NomeTipo(tipo: String): String = when (tipo.trim().lowercase(Locale.ROOT)) {
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
    else -> NomePokemon(tipo.trim())
}

@Composable
fun TipoBadge(tipos: String, modifier: Modifier = Modifier) {
    val listaTipos = tipos.split('/', ',', '|').map { it.trim() }.filter { it.isNotEmpty() }
    if (listaTipos.isEmpty()) return

    val (fundo, texto) = when (listaTipos.first().lowercase(Locale.ROOT)) {
        "grass", "planta", "bug", "inseto" -> Color(0xFFE5F4EA) to Color(0xFF246B3D)
        "water", "água", "ice", "gelo" -> Color(0xFFE5F0FE) to Color(0xFF24579B)
        "electric", "elétrico" -> Color(0xFFFFF6CF) to Color(0xFF796000)
        "fire", "fogo", "fighting", "lutador" -> Color(0xFFFFEBDD) to Color(0xFF974816)
        "poison", "veneno", "ghost", "fantasma", "psychic", "psíquico" -> Color(0xFFEDE7FB) to Color(0xFF644391)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = fundo, contentColor = texto) {
        Text(
            text = listaTipos.joinToString(" / ") { NomeTipo(it) },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun NumeroPokemon(numero: Int) {
    Text(
        text = "#${numero.toString().padStart(3, '0')}",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium
    )
}
