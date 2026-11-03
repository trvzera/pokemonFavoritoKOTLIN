package com.trvzera.poketeam.data

import android.database.sqlite.SQLiteConstraintException
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(
    private val dao: UsuarioDao,
    private val sessao: SessaoStore,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val emailValido = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@.]+$")

    suspend fun restaurarSessao(): Usuario? = withContext(dispatcher) {
        val id = sessao.lerUsuarioId() ?: return@withContext null
        dao.buscarPorId(id).also { if (it == null) sessao.limpar() }
    }

    suspend fun cadastrar(nome: String, email: String, senha: String) = withContext(dispatcher) {
        val nomeLimpo = nome.trim()
        val emailNormalizado = normalizarEmail(email)
        require(nomeLimpo.isNotEmpty()) { "Informe seu nome." }
        require(emailValido.matches(emailNormalizado)) { "Informe um e-mail válido." }
        require(senha.length >= 6 && senha.isNotBlank()) { "A senha deve ter pelo menos 6 caracteres." }
        require(dao.buscarPorEmail(emailNormalizado) == null) { "Este e-mail já está cadastrado." }

        val salt = SenhaUtils.gerarSalt()
        val usuario = Usuario(
            nome = nomeLimpo,
            email = emailNormalizado,
            senhaHash = SenhaUtils.gerarHash(senha, salt),
            salt = salt
        )
        try {
            dao.inserir(usuario)
        } catch (_: SQLiteConstraintException) {
            // O índice único também protege contra cadastros simultâneos do mesmo e-mail.
            throw IllegalArgumentException("Este e-mail já está cadastrado.")
        }
        Unit
    }

    suspend fun entrar(email: String, senha: String): Usuario = withContext(dispatcher) {
        val emailNormalizado = normalizarEmail(email)
        require(emailValido.matches(emailNormalizado)) { "E-mail ou senha inválidos." }
        val usuario = dao.buscarPorEmail(emailNormalizado)
        require(usuario != null && SenhaUtils.verificar(senha, usuario.senhaHash, usuario.salt)) {
            "E-mail ou senha inválidos."
        }
        sessao.salvarUsuarioId(usuario.id)
        usuario
    }

    suspend fun sair() = withContext(dispatcher) { sessao.limpar() }

    private fun normalizarEmail(email: String): String = email.trim().lowercase(Locale.ROOT)
}
