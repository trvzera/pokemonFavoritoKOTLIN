package com.trvzera.poketeam.userinterface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AcessoScreen(
    cadastro: Boolean,
    carregando: Boolean,
    erro: String?,
    onEnviar: (String, String, String) -> Unit,
    onAlternar: () -> Unit
) {
    var nome by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // A senha não entra no estado salvo da Activity.
    var senha by androidx.compose.runtime.remember { mutableStateOf("") }
    var mostrarSenha by rememberSaveable { mutableStateOf(false) }
    val teclado = LocalSoftwareKeyboardController.current
    val enviar = {
        teclado?.hide()
        onEnviar(nome, email, senha)
    }

    LazyColumn(
        modifier = Modifier.imePadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text("pokéteam", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(32.dp))
            Text(
                if (cadastro) "Crie seu perfil" else "Bem-vindo, treinador!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(12.dp))
            Text("Monte seu time de favoritos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (cadastro) item {
            OutlinedTextField(
                value = nome, onValueChange = { nome = it },
                label = { Text("Nome de treinador") }, singleLine = true,
                enabled = !carregando,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }
        item {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("E-mail") }, singleLine = true,
                enabled = !carregando,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
        }
        item {
            OutlinedTextField(
                value = senha, onValueChange = { senha = it },
                label = { Text("Senha") }, singleLine = true,
                enabled = !carregando,
                supportingText = if (cadastro) ({ Text("Mínimo de 6 caracteres") }) else null,
                visualTransformation = if (mostrarSenha) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { mostrarSenha = !mostrarSenha }) {
                        Icon(
                            if (mostrarSenha) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (mostrarSenha) "Ocultar senha" else "Mostrar senha"
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (!carregando) enviar() })
            )
        }
        if (erro != null) item {
            Text(erro, color = MaterialTheme.colorScheme.error)
        }
        item {
            Button(
                onClick = enviar,
                enabled = !carregando,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(if (carregando) "Aguarde..." else if (cadastro) "Criar conta" else "Entrar")
            }
            if (carregando) CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            TextButton(onClick = onAlternar, enabled = !carregando, modifier = Modifier.fillMaxWidth()) {
                Text(if (cadastro) "Já tenho conta · Entrar" else "Ainda não tem conta? Cadastre-se")
            }
        }
    }
}
