package com.trvzera.poketeam.data

import com.google.gson.Gson
import com.trvzera.poketeam.FakePokemonApi
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class PokemonRepositoryTest {
    @Test
    fun `lista consulta vinte itens e reaproveita resposta em memoria`() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepository(api)

        val lista = repository.listarPokemons()
        assertSame(lista, repository.listarPokemons())
        assertEquals(20, api.limiteRecebido)
        assertEquals(1, api.chamadasLista)
        assertEquals(1, lista.first().numero)
    }

    @Test
    fun `busca normaliza nome e compartilha cache com numero`() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepository(api)

        val pokemon = repository.buscarPokemon("  PIKACHU  ")
        assertSame(pokemon, repository.buscarPokemon("025"))
        assertEquals(listOf("pikachu"), api.buscas)
    }

    @Test
    fun `numero com zeros iniciais consulta numero correto`() = runTest {
        val api = FakePokemonApi()
        PokemonRepository(api).buscarPokemon("025")
        assertEquals(listOf("25"), api.buscas)
    }

    @Test
    fun `falha nao fica guardada no cache`() = runTest {
        val api = FakePokemonApi()
        val repository = PokemonRepository(api)
        api.responderBusca = { throw IOException("sem rede") }

        try {
            repository.buscarPokemon("25")
            fail("A consulta deveria falhar sem conexão")
        } catch (_: IOException) {
            // A mesma consulta deve funcionar depois que a conexão voltar.
        }
        api.responderBusca = { api.pikachu }
        assertEquals(25, repository.buscarPokemon("25").id)
        assertEquals(2, api.buscas.size)
    }

    @Test
    fun `gson le imagem e tipos com os nomes do JSON da API`() {
        val json = """
            {
              "id": 25, "name": "pikachu", "height": 4, "weight": 60,
              "sprites": {"front_default": "https://exemplo.com/25.png"},
              "types": [{"slot": 1, "type": {"name": "electric"}}]
            }
        """.trimIndent()
        val pokemon = Gson().fromJson(json, Pokemon::class.java)
        assertEquals("https://exemplo.com/25.png", pokemon.sprites.imagem)
        assertEquals("electric", pokemon.types.first().type.name)
        assertEquals(4, pokemon.height)
        assertEquals(60, pokemon.weight)
    }
}
