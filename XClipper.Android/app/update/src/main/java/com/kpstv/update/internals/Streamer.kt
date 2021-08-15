package com.kpstv.update.internals

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

internal class Streamer (
    private val onProgressChange: (currentBytes: Long, totalBytes: Long) -> Unit,
    private val onComplete: () -> Unit
) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    fun write(uri: String, destination: File) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(uri)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val fileReader = ByteArray(4096)
            val fileSize = connection.contentLength.toLong()
            var currentSize: Long = 0
            inputStream = connection.inputStream
            outputStream = FileOutputStream(destination)
            while (true) {
                val read: Int = inputStream?.read(fileReader) ?: 0
                if (read == -1) {
                    break
                }
                outputStream?.write(fileReader, 0, read)
                currentSize += read.toLong()

                onProgressChange.invoke(currentSize, fileSize)
                if (currentSize == fileSize)
                    onComplete.invoke()
            }
            outputStream?.flush()
        } catch (e: IOException) {
        } finally {
            inputStream?.close()
            outputStream?.close()
            connection?.disconnect()
        }
    }

    fun stop() {
        try {
            inputStream?.close()
            outputStream?.flush()
            outputStream?.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}