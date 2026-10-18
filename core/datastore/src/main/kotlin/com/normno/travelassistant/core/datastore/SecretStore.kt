package com.normno.travelassistant.core.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.normno.travelassistant.core.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Хранилище ключей к облачным моделям.
 *
 * Ключ пользователя — это его деньги, поэтому в открытом виде он не лежит нигде.
 * Шифрование делает сам Android Keystore: ключ шифрования не покидает защищённое
 * хранилище и не экспортируется даже этим приложением, а в DataStore попадает только
 * шифртекст. Сборка резервной копии такой ключ не восстановит — это правильно:
 * перенести чужой API-ключ на другое устройство не должно получаться автоматически.
 *
 * Библиотека `androidx.security:security-crypto` намеренно не используется: она
 * решает ту же задачу через собственный слой, который Google больше не развивает.
 */
@Singleton
class SecretStore @Inject constructor(
    private val store: DataStore<Preferences>,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    suspend fun put(id: String, secret: String) = withContext(io) {
        val payload = encrypt(secret)
        store.edit { it[key(id)] = payload }
    }

    suspend fun get(id: String): String? = withContext(io) {
        val payload = store.data.first()[key(id)] ?: return@withContext null
        runCatching { decrypt(payload) }.getOrNull()
    }

    suspend fun has(id: String): Boolean = get(id) != null

    suspend fun remove(id: String) = withContext(io) {
        store.edit { it.remove(key(id)) }
    }

    private fun key(id: String) = stringPreferencesKey("secret_$id")

    /* ---------------- шифрование ---------------- */

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val body = cipher.doFinal(plain.toByteArray())
        // IV хранится рядом с данными: он не секрет, но без него не расшифровать.
        return encode(cipher.iv) + SEPARATOR + encode(body)
    }

    private fun decrypt(payload: String): String {
        val (ivPart, bodyPart) = payload.split(SEPARATOR, limit = 2)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(TAG_BITS, decode(ivPart)),
        )
        return String(cipher.doFinal(decode(bodyPart)))
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private fun encode(bytes: ByteArray) = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val ALIAS = "travelassistant.secrets"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val SEPARATOR = ":"
    }
}
