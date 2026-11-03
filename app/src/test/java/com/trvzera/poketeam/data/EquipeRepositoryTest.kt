package com.trvzera.poketeam.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test

class EquipeRepositoryTest {
    private val dao = EquipeDaoFake()
    private val repository = EquipeRepository(dao)

    @Test
    fun regrasAceitamDeUmASeisPokemon() {
        (1..6).forEach { quantidade -> RegrasEquipe.validar((1..quantidade).map { membro(it) }) }
    }

    @Test
    fun regrasRejeitamEquipeVaziaMaiorQueSeisERepetidos() {
        assertThrows(IllegalArgumentException::class.java) { RegrasEquipe.validar(emptyList()) }
        assertThrows(IllegalArgumentException::class.java) {
            RegrasEquipe.validar((1..7).map { membro(it) })
        }
        assertThrows(IllegalArgumentException::class.java) {
            RegrasEquipe.validar(listOf(membro(25), membro(25)))
        }
    }

    @Test
    fun salvarReindexaPosicoesNaOrdemEscolhida() = runBlocking {
        repository.salvar(1, listOf(membro(25).copy(posicao = 8), membro(1).copy(posicao = 3)))
        val equipe = repository.carregar(1)

        assertEquals(listOf(25, 1), equipe.map { it.pokemonId })
        assertEquals(listOf(0, 1), equipe.map { it.posicao })
    }

    @Test
    fun salvarEquipeDeUmUsuarioPreservaEquipeDeOutro() = runBlocking {
        repository.salvar(1, listOf(membro(25)))
        repository.salvar(2, listOf(membro(4, usuarioId = 2)))
        repository.salvar(1, listOf(membro(7)))

        assertEquals(listOf(7), repository.carregar(1).map { it.pokemonId })
        assertEquals(listOf(4), repository.carregar(2).map { it.pokemonId })
    }

    @Test
    fun equipeInvalidaNaoSubstituiEquipeSalva() = runBlocking {
        repository.salvar(1, listOf(membro(25)))
        assertErro { repository.salvar(1, listOf(membro(1), membro(1))) }
        assertErro { repository.salvar(1, emptyList()) }
        assertErro { repository.salvar(1, listOf(membro(4, usuarioId = 2))) }

        assertEquals(listOf(25), repository.carregar(1).map { it.pokemonId })
        assertEquals(1, dao.substituicoes)
    }

    private fun membro(id: Int, usuarioId: Long = 1) = MembroEquipe(
        usuarioId = usuarioId, pokemonId = id, nome = "pokemon$id",
        imagem = null, tipos = "normal", posicao = 0
    )

    private suspend fun assertErro(acao: suspend () -> Unit) {
        try {
            acao()
            fail("Era esperado um erro de validação.")
        } catch (_: IllegalArgumentException) {
            // A equipe anterior deve continuar salva, verificado pelo teste.
        }
    }
}

private class EquipeDaoFake : EquipeDao() {
    private val membros = mutableListOf<MembroEquipe>()
    var substituicoes = 0
        private set

    override suspend fun carregar(usuarioId: Long): List<MembroEquipe> =
        membros.filter { it.usuarioId == usuarioId }.sortedBy { it.posicao }

    override suspend fun remover(usuarioId: Long) { membros.removeAll { it.usuarioId == usuarioId } }
    override suspend fun inserir(equipe: List<MembroEquipe>) { membros.addAll(equipe) }
    override suspend fun substituir(usuarioId: Long, equipe: List<MembroEquipe>) {
        substituicoes++
        super.substituir(usuarioId, equipe)
    }
}
