package com.trvzera.poketeam.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trvzera.poketeam.data.EquipeRepository
import com.trvzera.poketeam.data.MembroEquipe
import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.data.Usuario
import com.trvzera.poketeam.data.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AppViewModel(
    private val usuarios: UsuarioRepository,
    private val equipes: EquipeRepository
) : ViewModel() {
    var iniciando by mutableStateOf(true)
        private set
    var usuario: Usuario? by mutableStateOf(null)
        private set
    var autenticando by mutableStateOf(false)
        private set
    var erroAcesso: String? by mutableStateOf(null)
        private set
    var cadastroConcluido by mutableStateOf(false)
        private set
    var equipe by mutableStateOf<List<MembroEquipe>>(emptyList())
        private set
    var equipeSalva by mutableStateOf<List<MembroEquipe>>(emptyList())
        private set
    var salvando by mutableStateOf(false)
        private set
    var mensagem: String? by mutableStateOf(null)
        private set

    val alterada: Boolean get() = equipe != equipeSalva

    init {
        viewModelScope.launch {
            try {
                usuarios.restaurarSessao()?.let { restaurado ->
                    abrirSessao(restaurado)
                }
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                erroAcesso = "Não foi possível restaurar sua sessão. Tente entrar novamente."
            } finally {
                iniciando = false
            }
        }
    }

    fun limparErro() { erroAcesso = null }
    fun consumirMensagem() { mensagem = null }
    fun consumirCadastro() { cadastroConcluido = false }

    fun cadastrar(nome: String, email: String, senha: String) {
        if (autenticando) return
        autenticando = true
        erroAcesso = null
        viewModelScope.launch {
            try {
                usuarios.cadastrar(nome, email, senha)
                cadastroConcluido = true
                mensagem = "Conta criada. Entre com seu e-mail e senha."
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                erroAcesso = erro.message
            } catch (_: Exception) {
                erroAcesso = "Não foi possível criar sua conta. Tente novamente."
            } finally {
                autenticando = false
            }
        }
    }

    fun entrar(email: String, senha: String) {
        if (autenticando) return
        autenticando = true
        erroAcesso = null
        viewModelScope.launch {
            try {
                abrirSessao(usuarios.entrar(email, senha))
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                erroAcesso = erro.message
            } catch (_: Exception) {
                erroAcesso = "Não foi possível entrar. Tente novamente."
            } finally {
                autenticando = false
            }
        }
    }

    private suspend fun abrirSessao(pessoa: Usuario) {
        val salva = equipes.carregar(pessoa.id)
        equipeSalva = salva
        equipe = salva
        usuario = pessoa
    }

    fun adicionar(pokemon: Pokemon) {
        val pessoa = usuario ?: return
        if (salvando) return
        when {
            equipe.any { it.pokemonId == pokemon.id } -> mensagem = "Este Pokémon já está na equipe."
            equipe.size >= 6 -> mensagem = "Equipe cheia. Remova um Pokémon para liberar uma vaga."
            else -> {
                equipe = equipe + MembroEquipe(
                    usuarioId = pessoa.id,
                    pokemonId = pokemon.id,
                    nome = pokemon.name,
                    imagem = pokemon.sprites.other?.arteOficial?.imagem ?: pokemon.sprites.imagem,
                    tipos = pokemon.types.sortedBy { it.slot }.joinToString(" / ") { it.type.name },
                    posicao = equipe.size
                )
                mensagem = "Pokémon adicionado. Salve a equipe para atualizar seu perfil."
            }
        }
    }

    fun remover(pokemonId: Int) {
        if (salvando) return
        equipe = equipe.filterNot { it.pokemonId == pokemonId }.mapIndexed { i, item -> item.copy(posicao = i) }
    }

    fun salvarEquipe(aposSalvar: () -> Unit = {}) {
        val pessoa = usuario ?: return
        if (salvando) return
        val rascunho = equipe.toList()
        salvando = true
        viewModelScope.launch {
            try {
                equipes.salvar(pessoa.id, rascunho)
                equipeSalva = rascunho
                mensagem = "Equipe salva no perfil."
                salvando = false
                aposSalvar()
            } catch (erro: CancellationException) {
                throw erro
            } catch (erro: IllegalArgumentException) {
                mensagem = erro.message
            } catch (_: Exception) {
                mensagem = "Não foi possível salvar sua equipe. Tente novamente."
            } finally {
                salvando = false
            }
        }
    }

    fun descartarAlteracoes() { equipe = equipeSalva }

    fun sair() {
        if (salvando || autenticando) return
        autenticando = true
        viewModelScope.launch {
            try {
                usuarios.sair()
                usuario = null
                equipe = emptyList()
                equipeSalva = emptyList()
                erroAcesso = null
                mensagem = null
            } catch (erro: CancellationException) {
                throw erro
            } catch (_: Exception) {
                mensagem = "Não foi possível encerrar a sessão. Tente novamente."
            } finally {
                autenticando = false
            }
        }
    }
}
