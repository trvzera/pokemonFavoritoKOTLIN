package com.trvzera.poketeam.userinterface

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trvzera.poketeam.data.EquipeRepository
import com.trvzera.poketeam.data.PoketeamDatabase
import com.trvzera.poketeam.data.SessaoLocal
import com.trvzera.poketeam.data.UsuarioRepository
import com.trvzera.poketeam.userinterface.theme.PoketeamTheme
import com.trvzera.poketeam.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val banco = PoketeamDatabase.getInstance(applicationContext)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(
                UsuarioRepository(banco.usuarioDao(), SessaoLocal(applicationContext)),
                EquipeRepository(banco.equipeDao())
            ) as T
        }
        setContent {
            PoketeamTheme {
                val app: AppViewModel = viewModel(factory = factory)
                PoketeamApp(app)
            }
        }
    }
}
