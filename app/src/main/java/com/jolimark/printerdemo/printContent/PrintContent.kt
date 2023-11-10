package com.jolimark.printerdemo.printContent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

object PrintContent {

    fun getText(context: Context, name: String): String? {
        return getAssetsData(context, "$name.txt").toString()
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
        return bytes
    }

}