package com.trvzera.poketeam.userinterface

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CatchingPokemon
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.trvzera.poketeam.viewmodel.AppViewModel

@Composable
fun PoketeamApp(app: AppViewModel) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(app.mensagem) {
        app.mensagem?.let {
            snackbar.showSnackbar(it)
            if (app.mensagem == it) app.consumirMensagem()
        }
    }
    if (app.iniciando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (app.usuario == null) {
        val acesso = rememberNavController()
        LaunchedEffect(app.cadastroConcluido) {
            if (app.cadastroConcluido) {
                acesso.navigate(LoginRota) {
                    popUpTo<LoginRota> { inclusive = true }
                    launchSingleTop = true
                }
                app.consumirCadastro()
            }
        }
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { margens ->
            NavHost(acesso, startDestination = LoginRota, modifier = Modifier.padding(margens)) {
                composable<LoginRota> {
                    AcessoScreen(
                        cadastro = false, carregando = app.autenticando, erro = app.erroAcesso,
                        onEnviar = { _, email, senha -> app.entrar(email, senha) },
                        onAlternar = { app.limparErro(); acesso.navigate(CadastroRota) }
                    )
                }
                composable<CadastroRota> {
                    BackHandler {
                        if (!app.autenticando) {
                            app.limparErro()
                            acesso.popBackStack()
                        }
                    }
                    AcessoScreen(
                        cadastro = true, carregando = app.autenticando, erro = app.erroAcesso,
                        onEnviar = app::cadastrar,
                        onAlternar = { app.limparErro(); acesso.popBackStack() }
                    )
                }
            }
        }
    } else {
        // Uma nova conta recebe um NavController e ViewModels novos.
        key(app.usuario!!.id) { AreaAutenticada(app, snackbar) }
    }
}

@Composable
private fun AreaAutenticada(app: AppViewModel, snackbar: SnackbarHostState) {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val destino = entrada?.destination
    val contexto = LocalContext.current
    var acaoSaida: (() -> Unit)? by remember { mutableStateOf(null) }
    fun solicitarSaida(acao: () -> Unit) {
        if (app.alterada) acaoSaida = acao else acao()
    }
    fun abrirAba(rota: Any) {
        nav.navigate(rota) {
            popUpTo(nav.graph.findStartDestination().id)
            launchSingleTop = true
        }
    }
    BackHandler(enabled = destino?.hasRoute<PokedexRota>() == true) {
        solicitarSaida { (contexto as? Activity)?.finish() }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = destino?.hasRoute<PokedexRota>() == true || destino?.hasRoute<DetalheRota>() == true,
                    onClick = { abrirAba(PokedexRota) },
                    icon = { Icon(Icons.Outlined.CatchingPokemon, null) }, label = { Text("Pokédex") }
                )
                NavigationBarItem(
                    selected = destino?.hasRoute<EquipeRota>() == true,
                    onClick = { abrirAba(EquipeRota) },
                    icon = { Icon(Icons.Outlined.Groups, null) }, label = { Text("Minha equipe") }
                )
                NavigationBarItem(
                    selected = destino?.hasRoute<PerfilRota>() == true,
                    onClick = { abrirAba(PerfilRota) },
                    icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("Perfil") }
                )
            }
        }
    ) { margens ->
        NavHost(nav, startDestination = PokedexRota, modifier = Modifier.padding(margens)) {
            composable<PokedexRota> {
                PokedexScreen(onDetalhe = { nav.navigate(DetalheRota(it)) })
            }
            composable<EquipeRota> {
                EquipeScreen(
                    equipe = app.equipe, alterada = app.alterada, salvando = app.salvando,
                    onRemover = app::remover, onExplorar = { abrirAba(PokedexRota) },
                    onSalvar = { app.salvarEquipe() }, onDetalhe = { nav.navigate(DetalheRota(it)) }
                )
            }
            composable<PerfilRota> {
                app.usuario?.let { usuario ->
                    PerfilScreen(
                        usuario, app.equipeSalva,
                        onEditar = { abrirAba(EquipeRota) },
                        onSair = { solicitarSaida { app.sair() } },
                        onDetalhe = { nav.navigate(DetalheRota(it)) }
                    )
                }
            }
            composable<DetalheRota> { rota ->
                val detalhe = rota.toRoute<DetalheRota>()
                DetalheScreen(
                    pokemonId = detalhe.pokemonId, equipe = app.equipe, salvando = app.salvando,
                    onVoltar = { nav.popBackStack() }, onAdicionar = app::adicionar,
                    onEquipe = { abrirAba(EquipeRota) }
                )
            }
        }
    }
    if (acaoSaida != null) {
        AlertDialog(
            onDismissRequest = { if (!app.salvando) acaoSaida = null },
            title = { Text("Salvar alterações da equipe?") },
            text = { Text("Sua equipe tem alterações que ainda não foram salvas no perfil.") },
            confirmButton = {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !app.salvando && app.equipe.isNotEmpty(),
                        onClick = {
                            app.salvarEquipe {
                                val sair = acaoSaida
                                acaoSaida = null
                                sair?.invoke()
                            }
                        }
                    ) { Text(if (app.salvando) "Salvando..." else "Salvar e sair") }
                    TextButton(
                        enabled = !app.salvando,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            app.descartarAlteracoes()
                            val sair = acaoSaida
                            acaoSaida = null
                            sair?.invoke()
                        }
                    ) { Text("Descartar e sair") }
                    TextButton(
                        enabled = !app.salvando,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { acaoSaida = null }
                    ) { Text("Continuar editando") }
                }
            }
        )
    }
}
