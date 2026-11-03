package com.trvzera.poketeam.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SenhaUtils {
    private const val ITERACOES = 120_000

    fun gerarSalt(): String = ByteArray(16).also(SecureRandom()::nextBytes).toHex()

    // PBKDF2 é uma função própria para senhas e está disponível no Android mínimo do app.
    fun gerarHash(senha: String, salt: String): String {
        val spec = PBEKeySpec(senha.toCharArray(), salt.fromHex(), ITERACOES, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded.toHex()
        } finally {
            spec.clearPassword()
        }
    }

    fun verificar(senha: String, hash: String, salt: String): Boolean = try {
        MessageDigest.isEqual(gerarHash(senha, salt).fromHex(), hash.fromHex())
    } catch (_: IllegalArgumentException) {
        false
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun String.fromHex(): ByteArray {
        require(length % 2 == 0 && isNotEmpty()) { "Formato de hash inválido." }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
