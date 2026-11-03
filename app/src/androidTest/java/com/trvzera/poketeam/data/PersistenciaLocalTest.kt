package com.trvzera.poketeam.data

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenciaLocalTest {
    private lateinit var contexto: Context
    private lateinit var contextoSessao: Context
    private lateinit var banco: PoketeamDatabase
    private lateinit var nomeBanco: String
    private lateinit var nomePreferencias: String

    @Before
    fun preparar() {
        contexto = ApplicationProvider.getApplicationContext()
        val identificador = UUID.randomUUID().toString()
        nomeBanco = "poketeam-teste-$identificador.db"
        nomePreferencias = "sessao-teste-$identificador"
        // SharedPreferences reais com um nome exclusivo para preservar os dados do app.
        contextoSessao = object : ContextWrapper(contexto) {
            override fun getApplicationContext(): Context = this

            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences =
                contexto.getSharedPreferences(nomePreferencias, mode)
        }
        abrirBanco()
    }

    @After
    fun limpar() {
        banco.close()
        contexto.deleteDatabase(nomeBanco)
        contexto.deleteSharedPreferences(nomePreferencias)
    }

    @Test
    fun equipeESessaoSobrevivemAReabrirBancoSemMisturarUsuarios() = runBlocking {
        val repository = usuarios()
        repository.cadastrar("Ash", "ash@exemplo.com", "senha123")
        repository.cadastrar("Misty", "misty@exemplo.com", "senha456")
        val ash = repository.entrar("ash@exemplo.com", "senha123")
        val misty = requireNotNull(banco.usuarioDao().buscarPorEmail("misty@exemplo.com"))
        val equipeAsh = (1..6).map { membro(ash.id, it) }
        val equipeMisty = listOf(membro(misty.id, 7))
        EquipeRepository(banco.equipeDao()).salvar(ash.id, equipeAsh)
        EquipeRepository(banco.equipeDao()).salvar(misty.id, equipeMisty)

        banco.close()
        abrirBanco()

        assertEquals(ash.id, usuarios().restaurarSessao()?.id)
        val equipes = EquipeRepository(banco.equipeDao())
        assertEquals((1..6).toList(), equipes.carregar(ash.id).map { it.pokemonId })
        assertEquals((0..5).toList(), equipes.carregar(ash.id).map { it.posicao })
        assertEquals(listOf(7), equipes.carregar(misty.id).map { it.pokemonId })
        usuarios().sair()
        usuarios().entrar("misty@exemplo.com", "senha456")
        assertEquals(misty.id, usuarios().restaurarSessao()?.id)
        assertEquals(listOf(7), equipes.carregar(misty.id).map { it.pokemonId })
    }

    @Test
    fun sessaoSemUsuarioExistenteEhRemovida() = runBlocking {
        val sessao = SessaoLocal(contextoSessao)
        sessao.salvarUsuarioId(999_999)
        assertNull(usuarios().restaurarSessao())
        assertNull(sessao.lerUsuarioId())
    }

    private fun abrirBanco() {
        banco = Room.databaseBuilder(contexto, PoketeamDatabase::class.java, nomeBanco).build()
    }

    private fun usuarios() = UsuarioRepository(banco.usuarioDao(), SessaoLocal(contextoSessao))

    private fun membro(usuarioId: Long, pokemonId: Int) = MembroEquipe(
        usuarioId = usuarioId,
        pokemonId = pokemonId,
        nome = "pokemon-$pokemonId",
        imagem = null,
        tipos = "normal",
        posicao = 99
    )

}
