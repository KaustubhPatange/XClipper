import android.util.Base64.decode
import android.util.Base64.encodeToString
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES {
    private const val characterEncoding = "UTF-8"
    private const val cipherTransformation = "AES/CBC/PKCS5Padding"
    private const val aesEncryptionAlgorithm = "AES"

    fun decrypt(cipherText: ByteArray?, key: ByteArray?, initialVector: ByteArray?): ByteArray? {
        var cipherTextArray = cipherText
        val cipher = Cipher.getInstance(cipherTransformation)
        val secretKeySpecy = SecretKeySpec(key, aesEncryptionAlgorithm)
        val ivParameterSpec = IvParameterSpec(initialVector)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec)
        cipherTextArray = cipher.doFinal(cipherTextArray)
        return cipherTextArray
    }

    fun encrypt(plainText: ByteArray?, key: ByteArray?, initialVector: ByteArray?): ByteArray? {
        var plainTextArray = plainText
        val cipher = Cipher.getInstance(cipherTransformation)
        val secretKeySpec = SecretKeySpec(key, aesEncryptionAlgorithm)
        val ivParameterSpec = IvParameterSpec(initialVector)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        plainTextArray = cipher.doFinal(plainTextArray)
        return plainTextArray
    }

    private fun getKeyBytes(key: String): ByteArray {
        val keyBytes = ByteArray(16)
        val parameterKeyBytes = key.toByteArray(charset(characterEncoding))
        System.arraycopy(
            parameterKeyBytes,
            0,
            keyBytes,
            0,
            Math.min(parameterKeyBytes.size, keyBytes.size)
        )
        return keyBytes
    }

    fun encrypt(plainText: String, key: String): String {
        val plainTextbytes = plainText.toByteArray(charset(characterEncoding))
        val keyBytes = getKeyBytes(key)
        return encodeToString(encrypt(plainTextbytes, keyBytes, keyBytes), 0)
    }

    fun decrypt(encryptedText: String?, key: String): String {
        val cipheredBytes = decode(encryptedText,0)
        val keyBytes = getKeyBytes(key)
        return String(decrypt(cipheredBytes, keyBytes, keyBytes)!!)
    }
}