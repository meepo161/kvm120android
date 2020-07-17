package ru.avem.navitest.utils

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

fun copyFileFromStream(_inputStream: InputStream, dest: File) {
    _inputStream.use { inputStream ->
        try {
            val fileOutputStream = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var length = inputStream.read(buffer)
            while (length > 0) {
                fileOutputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }
        } catch (e: FileNotFoundException) {
        }
    }
}