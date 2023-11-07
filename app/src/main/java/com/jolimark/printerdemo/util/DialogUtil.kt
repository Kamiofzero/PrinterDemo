package com.jolimark.printerdemo.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import com.jolimark.printerdemo.R

object DialogUtil {
    fun showDialog(context: Context, message: String) {
        createDialog(context, message, null, null, null, null, null, null).show()
    }

    fun showDialog(context: Context, message: String, callback: Callback?) {
        createDialog(context, message, null, callback, null, null, null, null).show()
    }

    fun showDialog(context: Context, message: String, callback1: Callback?, callback2: Callback?) {
        createDialog(context, message, null, callback1, null, callback2, null, null).show()
    }

    fun showDialog(
        context: Context,
        message: String,
        buttonText1: String?,
        callback1: Callback?,
        buttonText2: String?,
        callback2: Callback?,
        buttonText3: String?,
        callback3: Callback?
    ) {
        createDialog(
            context,
            message,
            buttonText1,
            callback1,
            buttonText2,
            callback2,
            buttonText3,
            callback3
        ).show()
    }


    private fun createDialog(
        context: Context,
        message: String,
        buttonText1: String?,
        callback1: Callback?,
        buttonText2: String?,
        callback2: Callback?,
        buttonText3: String?,
        callback3: Callback?
    ): Dialog {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(message)
            setPositiveButton(
                buttonText1 ?: context.getString(R.string.confirm)
            ) { dialogInterface, i ->
                dialogInterface.dismiss()
                callback1?.onClick(dialogInterface)
            }
            if (callback2 != null)
                setNegativeButton(
                    buttonText2 ?: context.getString(R.string.cancel)
                ) { dialogInterface, _ ->
                    callback2?.onClick(dialogInterface)
                }
            if (callback3 != null)
                setNeutralButton(buttonText3 ?: "") { dialogInterface, _ ->
                    callback3?.onClick(dialogInterface)
                }

        }.create()
    }

    interface Callback {
        fun onClick(dialog: DialogInterface)
    }
}