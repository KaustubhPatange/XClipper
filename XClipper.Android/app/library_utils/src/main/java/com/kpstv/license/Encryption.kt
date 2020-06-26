package com.kpstv.license

object Encryption {
    private var phrase = "kpstv13"

    private var keyPhrase = "kpstv13"

    /**
     * This will set the decrypt password
     */
    fun setPassword(password: String) {
        phrase = password
    }

    fun getPassword() : String = phrase

    fun String.Decrypt(): String {
        return AES.decrypt(this, phrase)
    }
    fun String.DecryptPref(): String = AES.decrypt(this, keyPhrase)

    fun String.Encrypt(): String = AES.encrypt(this, phrase)

    fun String.EncryptPref(): String = AES.encrypt(this, keyPhrase)
}