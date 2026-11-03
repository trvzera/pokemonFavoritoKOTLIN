package com.trvzera.poketeam.userinterface

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.trvzera.poketeam.data.EquipeDao
import com.trvzera.poketeam.data.EquipeRepository
import com.trvzera.poketeam.data.MembroEquipe
import com.trvzera.poketeam.data.SessaoStore
import com.trvzera.poketeam.data.Usuario
import com.trvzera.poketeam.data.UsuarioDao
import com.trvzera.poketeam.data.UsuarioRepository
import com.trvzera.poketeam.userinterface.theme.PoketeamTheme
import com.trvzera.poketeam.viewmodel.AppViewModel
import kotlinx.coroutines.CompletableDeferred
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AcessoNavigationTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun voltarDuranteCadastroMantemTelaEAposConcluirAbreLogin() {
        val usuarios = CadastroPendenteDao()
        val app = AppViewModel(
            UsuarioRepository(usuarios, SessaoCadastroFake()),
            EquipeRepository(EquipeCadastroDaoFake())
        )
        compose.setContent { PoketeamTheme { PoketeamApp(app) } }
        compose.waitUntil(timeoutMillis = 10_000) { !app.iniciando }

        try {
            compose.onNodeWithText("Ainda não tem conta? Cadastre-se").performScrollTo().performClick()
            compose.onNodeWithText("Nome de treinador").performScrollTo().performTextInput("Treinadora Teste")
            compose.onNodeWithText("E-mail").performScrollTo().performTextInput("treinadora@exemplo.com")
            compose.onNodeWithText("Senha").performScrollTo().performTextInput("senha123")
            compose.onNodeWithText("Criar conta").performScrollTo().performClick()
            compose.waitUntil(timeoutMillis = 10_000) { usuarios.insercaoIniciada.isCompleted }

            // O retorno deve ser consumido enquanto a gravação ainda está pendente.
            compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithText("Já tenho conta · Entrar").performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("Aguarde...").assertIsDisplayed()

            usuarios.liberarInsercao.complete(Unit)
            compose.waitUntil(timeoutMillis = 10_000) { !app.autenticando && !app.cadastroConcluido }
            compose.onNodeWithText("Bem-vindo, treinador!").assertIsDisplayed()
            compose.onNodeWithText("Entrar").assertIsDisplayed()
            compose.onNodeWithText("Ainda não tem conta? Cadastre-se").assertIsDisplayed()
        } finally {
            usuarios.liberarInsercao.complete(Unit)
        }
    }
}

private class CadastroPendenteDao : UsuarioDao {
    val insercaoIniciada = CompletableDeferred<Unit>()
    val liberarInsercao = CompletableDeferred<Unit>()
    private var usuario: Usuario? = null

    override suspend fun inserir(usuario: Usuario): Long {
        insercaoIniciada.complete(Unit)
        liberarInsercao.await()
        this.usuario = usuario.copy(id = 1L)
        return 1L
    }

    override suspend fun buscarPorId(id: Long): Usuario? = usuario?.takeIf { it.id == id }
    override suspend fun buscarPorEmail(email: String): Usuario? = usuario?.takeIf { it.email == email }
}

private class SessaoCadastroFake : SessaoStore {
    private var usuarioId: Long? = null
    override fun lerUsuarioId(): Long? = usuarioId
    override fun salvarUsuarioId(id: Long) { usuarioId = id }
    override fun limpar() { usuarioId = null }
}

private class EquipeCadastroDaoFake : EquipeDao() {
    override suspend fun carregar(usuarioId: Long): List<MembroEquipe> = emptyList()
    override suspend fun remover(usuarioId: Long) = Unit
    override suspend fun inserir(equipe: List<MembroEquipe>) = Unit
}
