package com.jolimark.printerdemo.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {
    const val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    const val PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    const val PERMISSION_BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT
    const val PERMISSION_BLUETOOTH_SCAN= Manifest.permission.BLUETOOTH_SCAN
    fun checkPermissions(context: Context?, requestPermissions: Array<String>): Boolean {
        var flag = true
        val permissionList = ArrayList<String>()
        for (i in requestPermissions.indices) {
            val permission = requestPermissions[i]
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(permission)
                flag = false
            }
        }
        return flag
    }

    fun requestPermissions(context: Context?, requestPermissions: Array<String>, requestCode: Int) {
        val permissionNeedRequestList = ArrayList<String>()
        for (permission in requestPermissions) {
            if (permission === "") continue
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionNeedRequestList.add(permission)
            }
        }
        ActivityCompat.requestPermissions(
            (context as Activity?)!!,
            permissionNeedRequestList.toTypedArray(),
            requestCode
        )
    }

    fun checkAndRequestPermissions(
        context: Context,
        requestPermissions: Array<String>,
        requestCode: Int
    ): Boolean {
        var flag = true
        val permissionList = ArrayList<String>()
        for (i in requestPermissions.indices) {
            val permission = requestPermissions[i]
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionList.add(permission)
                flag = false
            }
        }
        val realPermissions = permissionList.toTypedArray()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (realPermissions.size > 0) (context as Activity).requestPermissions(
                realPermissions,
                requestCode
            )
        }
        return flag
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        callback: RequestPermissionsCallback?
    ) {
        if (callback != null) {
            val permissionsGrantList = ArrayList<String>()
            val permissionDeniedList = ArrayList<String>()
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGrantList.add(permissions[i])
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionDeniedList.add(permissions[i])
                }
            }
            callback.onPermissionResult(
                requestCode,
                permissionsGrantList.toTypedArray(),
                permissionDeniedList.toTypedArray()
            )
        }
    }

    interface RequestPermissionsCallback {
        fun onPermissionResult(
            requestCode: Int,
            grantPermissions: Array<String>,
            deniedPermissions: Array<String>
        )
    }
}