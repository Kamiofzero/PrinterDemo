package com.jolimark.printerdemo.printContent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.jolimark.printer.util.ByteArrayUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object PrintContent {

    fun getText(context: Context, name: String): String? {
        return getAssetsData(context, "$name.txt")?.let {
           String(it)
        }
    }

    fun getBitmap(context: Context, name: String): Bitmap? {
        return BitmapFactory.decodeStream(context.assets.open("${name}.png"))
    }

    fun getPrnData(context: Context, name: String): ByteArray? {
        return getAssetsData(context, "${name}.prn")
    }

    private fun getAssetsData(context: Context, fileName: String): ByteArray? {
        var `is`: InputStream? = null
        var bytes: ByteArray? = null
        try {
            `is` = context.assets.open(fileName)
            bytes = ByteArray(`is`.available())
            `is`.read(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (`is` != null) {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        Log.i("tag", "${bytes?.size}")

        return bytes
    }

    private fun getLocalData(filePath: String): ByteArray? {
        var fis: FileInputStream? = null
        var bytes: ByteArray? = null
        try {
            fis = FileInputStream(File(filePath))
            bytes = ByteArray(fis.available())
            fis.read(bytes)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fis?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bytes
    }

    fun getLocalText(filePath: String): String? {
        var byteArray = getLocalData(filePath)
        return byteArray?.toString()
    }

    fun getLocalBitmap(filePath: String): Bitmap? {
        return BitmapFactory.decodeFile(filePath)
    }

    fun getLocalPrn(filePath: String): ByteArray? {
        return getLocalData(filePath)
    }

}