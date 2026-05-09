package com.arsdevstudio.memoflow.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

/**
 * Utility class to manage database encryption keys using Android KeyStore.
 */
object SecurityUtils {

    private const val KEY_ALIAS = "memoflow_db_key_v3"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val AES_ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEYSTORE_ALGORITHM = "AES/GCM/NoPadding"

    /**
     * Gera uma chave estável a partir do Firebase UID do usuário para o Backup.
     * Isso permite que o backup seja restaurado em diferentes aparelhos.
     */
    private fun generateBackupKey(seed: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(seed.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(bytes, "AES")
    }

    fun encryptBackup(data: String, seed: String): String {
        val key = generateBackupKey(seed)
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decryptBackup(encryptedData: String, seed: String): String {
        val combined = Base64.decode(encryptedData, Base64.DEFAULT)
        val iv = combined.sliceArray(0 until 16)
        val encrypted = combined.sliceArray(16 until combined.size)
        
        val key = generateBackupKey(seed)
        val cipher = Cipher.getInstance(AES_ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    /**
     * Gets or creates a stable passphrase for database encryption.
     * Uses Android KeyStore to encrypt the actual passphrase stored in SharedPreferences.
     */
    fun getDatabasePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val encrypted = prefs.getString("encrypted_db_pass_v3", null)
        val iv = prefs.getString("db_pass_iv_v3", null)

        if (encrypted == null || iv == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            savePassphrase(context, randomBytes)
            return randomBytes
        }

        return try {
            decryptPassphrase(encrypted, iv)
        } catch (e: Exception) {
            // Em caso de erro catastrófico na KeyStore, gera uma nova
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            savePassphrase(context, randomBytes)
            randomBytes
        }
    }

    /**
     * Encrypts and saves a passphrase to SharedPreferences using the KeyStore Master Key.
     */
    fun savePassphrase(context: Context, passphrase: ByteArray) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }

        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(passphrase)
        
        context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE).edit().apply {
            putString("encrypted_db_pass_v3", Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
            putString("db_pass_iv_v3", Base64.encodeToString(iv, Base64.NO_WRAP))
            apply()
        }
    }

    private fun decryptPassphrase(encryptedBase64: String, ivBase64: String): ByteArray {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        
        val cipher = Cipher.getInstance(KEYSTORE_ALGORITHM)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return cipher.doFinal(encryptedBytes)
    }

}
