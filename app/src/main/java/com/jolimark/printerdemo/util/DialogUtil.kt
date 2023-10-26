package com.jolimark.printerdemo.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface

object DialogUtil {
    fun showDialog(context: Context, message: String) {
        createDialog(context, message, null).show()
    }

    fun showDialog(context: Context, message: String, callback: Callback?) {
        createDialog(context, message, callback).show()
    }

    private fun createDialog(context: Context, message: String, callback: Callback?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setMessage(message)
        builder.setPositiveButton("确定") { dialogInterface, i ->
            dialogInterface.dismiss()
            callback?.onClick(dialogInterface)
        }
        return builder.create()
    }

    interface Callback {
        fun onClick(dialog: DialogInterface)
    }
}