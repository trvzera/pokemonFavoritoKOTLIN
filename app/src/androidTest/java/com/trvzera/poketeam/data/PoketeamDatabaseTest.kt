package com.trvzera.poketeam.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PoketeamDatabaseTest {
    private lateinit var database: PoketeamDatabase

    @Before
    fun abrirBanco() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), PoketeamDatabase::class.java
        ).build()
    }

    @After
    fun fecharBanco() { database.close() }

    @Test
    fun indiceUnicoImpedeEmailDuplicado() = runBlocking {
        val usuario = Usuario(nome = "Ana", email = "ana@exemplo.com", senhaHash = "hash", salt = "salt")
        database.usuarioDao().inserir(usuario)
        try {
            database.usuarioDao().inserir(usuario.copy(email = "ANA@EXEMPLO.COM"))
            fail("O índice único deveria impedir o e-mail duplicado.")
        } catch (_: SQLiteConstraintException) {
            assertNotNull(database.usuarioDao().buscarPorEmail(usuario.email))
        }
    }

    @Test
    fun falhaNaSubstituicaoFazRollbackEPreservaOutroUsuario() = runBlocking {
        val usuarios = database.usuarioDao()
        val anaId = usuarios.inserir(Usuario(nome = "Ana", email = "ana@exemplo.com", senhaHash = "h", salt = "s"))
        val biaId = usuarios.inserir(Usuario(nome = "Bia", email = "bia@exemplo.com", senhaHash = "h", salt = "s"))
        val dao = database.equipeDao()
        val ana = MembroEquipe(anaId, 25, "pikachu", null, "electric", 0)
        val bia = MembroEquipe(biaId, 1, "bulbasaur", null, "grass", 0)
        dao.substituir(anaId, listOf(ana))
        dao.substituir(biaId, listOf(bia))

        try {
            dao.substituir(anaId, listOf(ana.copy(pokemonId = 4), ana.copy(pokemonId = 4, posicao = 1)))
            fail("Pokémon duplicado deveria falhar no banco.")
        } catch (_: SQLiteConstraintException) {
            assertEquals(listOf(ana), dao.carregar(anaId))
            assertEquals(listOf(bia), dao.carregar(biaId))
        }
    }
}
