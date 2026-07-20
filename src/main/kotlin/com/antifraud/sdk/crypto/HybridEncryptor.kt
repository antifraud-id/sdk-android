package com.antifraud.sdk.crypto

import android.util.Base64
import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

object HybridEncryptor {

    fun encryptPayload(publicKeyPEM: String, plaintextJSON: String): String {
        val cleanPEM = publicKeyPEM
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val decodedKeyBytes = Base64.decode(cleanPEM, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(decodedKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val secureRandom = SecureRandom()
        val aesKey = ByteArray(32)
        secureRandom.nextBytes(aesKey)

        val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        val oaepSpec = OAEPParameterSpec(
            "SHA-256",
            "MGF1",
            MGF1ParameterSpec.SHA256,
            PSource.PSpecified.DEFAULT
        )
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec)
        val rsaCiphertext = rsaCipher.doFinal(aesKey)

        val nonce = ByteArray(12)
        secureRandom.nextBytes(nonce)

        val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, nonce)
        val secretKey = SecretKeySpec(aesKey, "AES")
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val aesCiphertextBody = aesCipher.doFinal(plaintextJSON.toByteArray(Charsets.UTF_8))

        val aesCiphertext = ByteArray(nonce.size + aesCiphertextBody.size)
        System.arraycopy(nonce, 0, aesCiphertext, 0, nonce.size)
        System.arraycopy(aesCiphertextBody, 0, aesCiphertext, nonce.size, aesCiphertextBody.size)

        val buffer = ByteBuffer.allocate(4 + rsaCiphertext.size + aesCiphertext.size)
        buffer.putInt(rsaCiphertext.size)
        buffer.put(rsaCiphertext)
        buffer.put(aesCiphertext)

        return Base64.encodeToString(buffer.array(), Base64.NO_WRAP)
    }
}
