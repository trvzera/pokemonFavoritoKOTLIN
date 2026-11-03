package com.trvzera.poketeam.viewmodel

import com.trvzera.poketeam.data.EquipeDao
import com.trvzera.poketeam.data.EquipeRepository
import com.trvzera.poketeam.data.MembroEquipe
import com.trvzera.poketeam.data.Pokemon
import com.trvzera.poketeam.data.PokemonSprites
import com.trvzera.poketeam.data.PokemonTipo
import com.trvzera.poketeam.data.SenhaUtils
import com.trvzera.poketeam.data.SessaoStore
import com.trvzera.poketeam.data.Tipo
import com.trvzera.poketeam.data.Usuario
import com.trvzera.poketeam.data.UsuarioDao
import com.trvzera.poketeam.data.UsuarioRepository
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @Before
    fun configurarMain() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `restaurar sessao carrega snapshot salvo sem marcar alteracao`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val salva = listOf(membro(1, 1, 0), membro(1, 25, 1))
        cenario.equipes.gravadas[1] = salva
        val viewModel = cenario.criarViewModel()

        assertTrue(viewModel.iniciando)
        assertNull(viewModel.usuario)
        advanceUntilIdle()

        assertFalse(viewModel.iniciando)
        assertEquals(1L, viewModel.usuario?.id)
        assertEquals(salva, viewModel.equipe)
        assertEquals(salva, viewModel.equipeSalva)
        assertFalse(viewModel.alterada)
    }

    @Test
    fun `rascunho so atualiza perfil e callback depois de persistir`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val viewModel = cenario.criarViewModel()
        advanceUntilIdle()
        viewModel.adicionar(pokemon(25))
        assertEquals(listOf(25), viewModel.equipe.map { it.pokemonId })
        assertTrue(viewModel.equipeSalva.isEmpty())
        assertTrue(viewModel.alterada)

        val liberarGravacao = CompletableDeferred<Unit>()
        cenario.equipes.liberarGravacao = liberarGravacao
        var chamouCallback = false
        viewModel.salvarEquipe {
            assertEquals(listOf(25), cenario.equipes.gravadas[1]?.map { it.pokemonId })
            assertEquals(viewModel.equipe, viewModel.equipeSalva)
            chamouCallback = true
        }
        advanceUntilIdle()
        assertTrue(viewModel.salvando)
        assertFalse(chamouCallback)
        assertTrue(viewModel.equipeSalva.isEmpty())

        liberarGravacao.complete(Unit)
        advanceUntilIdle()
        assertTrue(chamouCallback)
        assertFalse(viewModel.salvando)
        assertFalse(viewModel.alterada)
    }

    @Test
    fun `equipe impede repetidos e o setimo pokemon`() = runTest {
        val viewModel = CenarioApp(StandardTestDispatcher(testScheduler)).criarViewModel()
        advanceUntilIdle()

        viewModel.adicionar(pokemon(1))
        viewModel.adicionar(pokemon(1))
        assertEquals(1, viewModel.equipe.size)
        assertTrue(viewModel.mensagem.orEmpty().contains("já está"))

        (2..6).forEach { viewModel.adicionar(pokemon(it)) }
        viewModel.adicionar(pokemon(7))
        assertEquals((1..6).toList(), viewModel.equipe.map { it.pokemonId })
        assertTrue(viewModel.mensagem.orEmpty().contains("Equipe cheia"))
    }

    @Test
    fun `remover reordena rascunho sem mudar snapshot salvo antes de salvar`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val salva = listOf(membro(1, 1, 0), membro(1, 4, 1), membro(1, 7, 2))
        cenario.equipes.gravadas[1] = salva
        val viewModel = cenario.criarViewModel()
        advanceUntilIdle()

        viewModel.remover(4)
        assertEquals(listOf(1, 7), viewModel.equipe.map { it.pokemonId })
        assertEquals(listOf(0, 1), viewModel.equipe.map { it.posicao })
        assertEquals(salva, viewModel.equipeSalva)
        assertEquals(salva, cenario.equipes.gravadas[1])

        viewModel.salvarEquipe()
        advanceUntilIdle()
        assertEquals(viewModel.equipe, cenario.equipes.gravadas[1])
        assertFalse(viewModel.alterada)
    }

    @Test
    fun `sair e entrar com outra conta nao mistura equipes nem preserva rascunho`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val equipeAna = listOf(membro(1, 7, 0))
        val equipeBia = listOf(membro(2, 133, 0))
        cenario.equipes.gravadas[1] = equipeAna
        cenario.equipes.gravadas[2] = equipeBia
        val viewModel = cenario.criarViewModel()
        advanceUntilIdle()
        viewModel.adicionar(pokemon(25))

        viewModel.sair()
        advanceUntilIdle()
        assertNull(viewModel.usuario)
        assertNull(cenario.sessao.lerUsuarioId())
        assertTrue(viewModel.equipe.isEmpty())
        assertTrue(viewModel.equipeSalva.isEmpty())

        viewModel.entrar("bia@exemplo.com", "senha123")
        advanceUntilIdle()
        assertEquals(2L, viewModel.usuario?.id)
        assertEquals(equipeBia, viewModel.equipe)
        assertEquals(equipeBia, viewModel.equipeSalva)

        viewModel.sair()
        advanceUntilIdle()
        viewModel.entrar("ana@exemplo.com", "senha123")
        advanceUntilIdle()
        assertEquals(1L, cenario.sessao.lerUsuarioId())
        assertEquals(equipeAna, viewModel.equipe)
        assertEquals(equipeBia, cenario.equipes.gravadas[2])
    }

    @Test
    fun `salvar e sair encerra sessao depois de guardar equipe`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val viewModel = cenario.criarViewModel()
        advanceUntilIdle()
        viewModel.adicionar(pokemon(25))

        viewModel.salvarEquipe { viewModel.sair() }
        advanceUntilIdle()

        assertEquals(listOf(25), cenario.equipes.gravadas[1]?.map { it.pokemonId })
        assertNull(viewModel.usuario)
        assertNull(cenario.sessao.lerUsuarioId())
        assertTrue(viewModel.equipe.isEmpty())
    }

    @Test
    fun `erro ao salvar preserva rascunho e nao executa acao de saida`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val viewModel = cenario.criarViewModel()
        advanceUntilIdle()
        viewModel.adicionar(pokemon(25))
        cenario.equipes.falhaAoSalvar = IOException("banco indisponível")
        var chamouCallback = false
        viewModel.salvarEquipe { chamouCallback = true }
        advanceUntilIdle()

        assertFalse(chamouCallback)
        assertFalse(viewModel.salvando)
        assertTrue(viewModel.alterada)
        assertTrue(viewModel.equipeSalva.isEmpty())
        assertEquals(25, viewModel.equipe.single().pokemonId)
        assertEquals(1L, viewModel.usuario?.id)
        assertTrue(viewModel.mensagem.orEmpty().contains("Não foi possível salvar"))
    }

    @Test
    fun `equipe vazia nao e persistida nem executa callback`() = runTest {
        val cenario = CenarioApp(StandardTestDispatcher(testScheduler))
        val viewModel = cenario.criarViewModel()
        advanceUntilIdle()
        var chamouCallback = false

        viewModel.salvarEquipe { chamouCallback = true }
        advanceUntilIdle()

        assertFalse(chamouCallback)
        assertEquals(0, cenario.equipes.gravacoes)
        assertTrue(viewModel.equipeSalva.isEmpty())
        assertTrue(viewModel.mensagem.orEmpty().contains("1 a 6"))
    }
}

private class CenarioApp(dispatcher: CoroutineDispatcher) {
    val sessao = SessaoAppFake()
    val equipes = EquipeAppDaoFake()
    private val usuarios = UsuarioAppDaoFake()
    private val repository = UsuarioRepository(usuarios, sessao, dispatcher)

    fun criarViewModel(): AppViewModel = AppViewModel(repository, EquipeRepository(equipes))
}

private class SessaoAppFake : SessaoStore {
    private var id: Long? = 1L
    override fun lerUsuarioId(): Long? = id
    override fun salvarUsuarioId(id: Long) { this.id = id }
    override fun limpar() { id = null }
}

private class UsuarioAppDaoFake : UsuarioDao {
    private val usuarios = mutableListOf(
        usuarioTeste(1, "Ana", "ana@exemplo.com"),
        usuarioTeste(2, "Bia", "bia@exemplo.com")
    )
    override suspend fun inserir(usuario: Usuario): Long {
        val id = usuarios.size.toLong() + 1
        usuarios.add(usuario.copy(id = id))
        return id
    }
    override suspend fun buscarPorId(id: Long): Usuario? = usuarios.find { it.id == id }
    override suspend fun buscarPorEmail(email: String): Usuario? = usuarios.find { it.email == email }
}

private class EquipeAppDaoFake : EquipeDao() {
    val gravadas = mutableMapOf<Long, List<MembroEquipe>>()
    var liberarGravacao: CompletableDeferred<Unit>? = null
    var falhaAoSalvar: Exception? = null
    var gravacoes = 0

    override suspend fun carregar(usuarioId: Long): List<MembroEquipe> =
        gravadas[usuarioId].orEmpty().sortedBy { it.posicao }

    override suspend fun remover(usuarioId: Long) { gravadas.remove(usuarioId) }
    override suspend fun inserir(equipe: List<MembroEquipe>) {
        equipe.groupBy { it.usuarioId }.forEach { (id, membros) -> gravadas[id] = membros.toList() }
    }
    override suspend fun substituir(usuarioId: Long, equipe: List<MembroEquipe>) {
        liberarGravacao?.await()
        falhaAoSalvar?.let { throw it }
        super.substituir(usuarioId, equipe)
        gravacoes++
    }
}

private fun usuarioTeste(id: Long, nome: String, email: String): Usuario {
    val salt = id.toString(16).padStart(32, '0')
    return Usuario(id, nome, email, SenhaUtils.gerarHash("senha123", salt), salt)
}

private fun membro(usuarioId: Long, pokemonId: Int, posicao: Int): MembroEquipe =
    MembroEquipe(usuarioId, pokemonId, "pokemon$pokemonId", null, "normal", posicao)

private fun pokemon(id: Int): Pokemon = Pokemon(
    id, "pokemon$id", 4, 60, PokemonSprites(null), listOf(PokemonTipo(1, Tipo("normal")))
)
