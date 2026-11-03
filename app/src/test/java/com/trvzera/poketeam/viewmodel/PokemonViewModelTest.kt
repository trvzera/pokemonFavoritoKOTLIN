package com.trvzera.poketeam.viewmodel

import com.trvzera.poketeam.FakePokemonApi
import com.trvzera.poketeam.data.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonViewModelTest {
    @Before
    fun configurarMain() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun restaurarMain() {
        Dispatchers.resetMain()
    }

    @Test
    fun `abertura passa de carregando para lista`() = runTest {
        val api = FakePokemonApi()
        val viewModel = PokemonViewModel(PokemonRepository(api))
        assertEquals(PokemonUiState.Carregando, viewModel.estado)
        runCurrent()
        assertEquals(PokemonUiState.Lista(api.lista.results), viewModel.estado)
    }

    @Test
    fun `404 mostra mensagem de pokemon nao encontrado`() = runTest {
        val api = FakePokemonApi()
        val viewModel = PokemonViewModel(PokemonRepository(api))
        runCurrent()
        api.responderBusca = { throw HttpException(Response.error<Any>(404, ResponseBody.create(null, ""))) }

        viewModel.atualizarBusca("inexistente")
        viewModel.buscar()
        runCurrent()
        val erro = viewModel.estado as PokemonUiState.Erro
        assertTrue(erro.mensagem.contains("Pokémon não encontrado"))
    }

    @Test
    fun `tentar novamente repete consulta que falhou mesmo apos editar campo`() = runTest {
        val api = FakePokemonApi()
        val viewModel = PokemonViewModel(PokemonRepository(api))
        runCurrent()
        api.responderBusca = { throw IOException("sem rede") }
        viewModel.atualizarBusca("25")
        viewModel.buscar()
        runCurrent()
        assertTrue((viewModel.estado as PokemonUiState.Erro).mensagem.contains("conexão"))

        viewModel.atualizarBusca("bulbasaur")
        api.responderBusca = { api.pikachu }
        viewModel.tentarNovamente()
        assertEquals(PokemonUiState.Carregando, viewModel.estado)
        runCurrent()
        assertEquals(listOf("25", "25"), api.buscas)
        assertEquals(PokemonUiState.Resultado(api.pikachu), viewModel.estado)
    }

    @Test
    fun `nova consulta cancela busca pendente sem transformar cancelamento em erro`() = runTest {
        val api = FakePokemonApi()
        val viewModel = PokemonViewModel(PokemonRepository(api))
        runCurrent()
        var cancelou = false
        api.responderBusca = { nome ->
            if (nome == "pikachu") {
                try {
                    awaitCancellation()
                } finally {
                    cancelou = true
                }
            }
            api.pikachu.copy(id = 133, name = "eevee")
        }

        viewModel.atualizarBusca("pikachu")
        viewModel.buscar()
        runCurrent()
        viewModel.atualizarBusca("eevee")
        viewModel.buscar()
        runCurrent()
        assertTrue(cancelou)
        assertEquals("eevee", (viewModel.estado as PokemonUiState.Resultado).pokemon.name)
    }
}
