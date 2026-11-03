package com.trvzera.poketeam.data

import android.content.Context

class SessaoLocal(context: Context) : SessaoStore {
    private val preferencias = context.applicationContext.getSharedPreferences(
        "sessao_poketeam", Context.MODE_PRIVATE
    )

    override fun lerUsuarioId(): Long? =
        preferencias.getLong("usuario_id", 0).takeIf { it > 0 }

    override fun salvarUsuarioId(id: Long) {
        require(id > 0) { "Usuário inválido." }
        check(preferencias.edit().putLong("usuario_id", id).commit()) {
            "Não foi possível salvar sua sessão. Tente novamente."
        }
    }

    override fun limpar() {
        check(preferencias.edit().remove("usuario_id").commit()) {
            "Não foi possível encerrar sua sessão. Tente novamente."
        }
    }
}
