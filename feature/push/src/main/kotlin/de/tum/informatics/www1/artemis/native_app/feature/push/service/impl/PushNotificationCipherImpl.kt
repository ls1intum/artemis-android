package de.tum.informatics.www1.artemis.native_app.feature.push.service.impl

import android.util.Base64
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationCipher
import de.tum.informatics.www1.artemis.native_app.feature.push.service.PushNotificationConfigurationService
import kotlinx.coroutines.runBlocking
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class PushNotificationCipherImpl(
    private val pushNotificationConfigurationService: PushNotificationConfigurationService
) : PushNotificationCipher {

    private companion object {
        private const val ALGORITHM = "AES/CBC/PKCS7Padding"

        private val cipher: Cipher? = try {
            Cipher.getInstance(ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: NoSuchPaddingException) {
            null
        }
    }

    override fun decipherPushNotification(ciphertext: String, iv: String): String? = runBlocking {
        val key = pushNotificationConfigurationService.getCurrentAESKey() ?: return@runBlocking null
        val cipher = cipher ?: return@runBlocking null

        val ivAsBytes = Base64.decode(iv.toByteArray(Charsets.ISO_8859_1), Base64.DEFAULT)

        cipher.decrypt(ciphertext, key, ivAsBytes) ?: return@runBlocking null
    }

    private fun Cipher.decrypt(ciphertext: String, key: SecretKey, iv: ByteArray): String? {
        return try {
            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

            val cipherTextBytes = ciphertext.toByteArray(Charsets.ISO_8859_1)
            val textBytes = doFinal(Base64.decode(cipherTextBytes, Base64.DEFAULT))

            String(textBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
