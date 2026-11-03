package com.trvzera.poketeam.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UsuarioRepositoryTest {
    private val dao = UsuarioDaoFake()
    private val sessao = SessaoFake()
    private val repository = UsuarioRepository(dao, sessao)

    @Test
    fun cadastroNormalizaEmailEProtegeSenha() = runBlocking {
        repository.cadastrar("  Ana  ", "  ANA@EXEMPLO.COM  ", "senha123")
        val usuario = dao.buscarPorEmail("ana@exemplo.com")!!

        assertEquals("Ana", usuario.nome)
        assertNotEquals("senha123", usuario.senhaHash)
        assertTrue(SenhaUtils.verificar("senha123", usuario.senhaHash, usuario.salt))
        assertNull(sessao.lerUsuarioId())
    }

    @Test
    fun emailDuplicadoIgnoraMaiusculasEEspacos() = runBlocking {
        repository.cadastrar("Ana", "ana@exemplo.com", "senha123")
        assertMensagem("Este e-mail já está cadastrado.") {
            repository.cadastrar("Outra Ana", " ANA@EXEMPLO.COM ", "outra123")
        }
        assertEquals(1, dao.usuarios.size)
    }

    @Test
    fun cadastroRejeitaCamposInvalidosSemGravarUsuario() = runBlocking {
        assertMensagem("Informe seu nome.") { repository.cadastrar("  ", "ana@exemplo.com", "senha123") }
        assertMensagem("Informe um e-mail válido.") { repository.cadastrar("Ana", "ana@", "senha123") }
        assertMensagem("A senha deve ter pelo menos 6 caracteres.") {
            repository.cadastrar("Ana", "ana@exemplo.com", "12345")
        }
        assertEquals(0, dao.usuarios.size)
    }

    @Test
    fun entrarRestauraSessaoENovoRepositoryMantemUsuario() = runBlocking {
        repository.cadastrar("Ana", "ana@exemplo.com", "senha123")
        val usuario = repository.entrar(" ANA@EXEMPLO.COM ", "senha123")

        assertEquals(usuario.id, sessao.lerUsuarioId())
        assertEquals(usuario, UsuarioRepository(dao, sessao).restaurarSessao())
        repository.sair()
        assertNull(UsuarioRepository(dao, sessao).restaurarSessao())
    }

    @Test
    fun loginInvalidoUsaMesmaMensagemENaoCriaSessao() = runBlocking {
        repository.cadastrar("Ana", "ana@exemplo.com", "senha123")
        listOf("ana@exemplo.com" to "errada", "outro@exemplo.com" to "senha123", "invalido" to "senha123")
            .forEach { (email, senha) ->
                assertMensagem("E-mail ou senha inválidos.") { repository.entrar(email, senha) }
            }
        assertNull(sessao.lerUsuarioId())
    }

    @Test
    fun sessaoDeUsuarioRemovidoELimpa() = runBlocking {
        sessao.salvarUsuarioId(99)
        assertNull(repository.restaurarSessao())
        assertNull(sessao.lerUsuarioId())
    }

    @Test
    fun senhasIguaisRecebemSaltsEHashesDiferentes() {
        val salt1 = SenhaUtils.gerarSalt()
        val salt2 = SenhaUtils.gerarSalt()
        val hash1 = SenhaUtils.gerarHash("senha123", salt1)
        val hash2 = SenhaUtils.gerarHash("senha123", salt2)

        assertNotEquals(salt1, salt2)
        assertNotEquals(hash1, hash2)
        assertTrue(SenhaUtils.verificar("senha123", hash1, salt1))
        assertFalse(SenhaUtils.verificar("senhaerrada", hash1, salt1))
        assertFalse(SenhaUtils.verificar("senha123", "hashcorrompido", salt1))
    }

    private suspend fun assertMensagem(mensagem: String, acao: suspend () -> Unit) {
        try {
            acao()
            fail("Era esperado um erro de validação.")
        } catch (erro: IllegalArgumentException) {
            assertEquals(mensagem, erro.message)
        }
    }
}

private class UsuarioDaoFake : UsuarioDao {
    val usuarios = mutableListOf<Usuario>()

    override suspend fun inserir(usuario: Usuario): Long {
        val id = usuarios.size.toLong() + 1
        usuarios.add(usuario.copy(id = id))
        return id
    }

    override suspend fun buscarPorId(id: Long): Usuario? = usuarios.find { it.id == id }
    override suspend fun buscarPorEmail(email: String): Usuario? = usuarios.find { it.email == email }
}

private class SessaoFake : SessaoStore {
    private var usuarioId: Long? = null
    override fun lerUsuarioId(): Long? = usuarioId
    override fun salvarUsuarioId(id: Long) { usuarioId = id }
    override fun limpar() { usuarioId = null }
}
