package com.example.gameswiper.common

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun saveToJsonFile(context: Context, fileName: String, jsonContent: String){
    try{
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { outputStream ->
            outputStream.write(jsonContent.toByteArray())
        }

        println("Plik zapisany w lokalizacji ${file.absolutePath}")
    } catch (e: Exception){
        e.printStackTrace()
        println("Błąd przy zapisywaniu pliku json")
    }
}