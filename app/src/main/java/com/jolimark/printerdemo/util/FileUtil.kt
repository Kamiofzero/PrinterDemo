package com.jolimark.printerdemo.util

import android.content.Context
import java.io.IOException
import java.io.InputStream

object FileUtil {


    fun getAssetsData(context: Context, fileName: String): ByteArray? {
        var bytes: ByteArray? = null
        var `is`: InputStream? = null
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