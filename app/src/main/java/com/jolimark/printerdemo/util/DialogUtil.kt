package com.jolimark.printerdemo.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.jolimark.printerdemo.R

object DialogUtil {
    fun showDialog(context: Context, message: String) {
        createDialog(context, message, null, null).show()
    }

    fun showDialog(context: Context, message: String, callback: Callback?) {
        createDialog(context, message, callback, null).show()
    }

    fun showDialog(context: Context, message: String, callback1: Callback?, callback2: Callback?) {
        createDialog(context, message, callback1, callback2).show()
    }

    private fun createDialog(
        context: Context,
        message: String,
        callback1: Callback?,
        callback2: Callback?
    ): Dialog {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(message)
            setPositiveButton(context.getString(R.string.confirm)) { dialogInterface, i ->
                dialogInterface.dismiss()
                callback1?.onClick(dialogInterface)
            }
            if (callback2 != null)
                setNegativeButton(context.getString(R.string.cancel)) { dialogInterface, _ ->
                    callback2?.onClick(dialogInterface)
                }

        }.create()
    }

    interface Callback {
        fun onClick(dialog: DialogInterface)
    }
}