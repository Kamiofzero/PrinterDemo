package com.jolimark.printerdemo.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.LinearLayout.LayoutParams
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.jolimark.printerdemo.databinding.DialogProgressBinding
import com.jolimark.printerdemo.util.DialogUtil
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType


abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(), OnClickListener {

    protected val TAG = javaClass.simpleName

    protected lateinit var context: Context

    private lateinit var progressDialog: AlertDialog
    private lateinit var progressText: TextView
    protected lateinit var vb: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(initViewBinding())
        context = this
        initProgressDialog()
        initStatusBar()
        initView()
        initData()
    }

    private fun initStatusBar() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.statusBarColor = Color.TRANSPARENT;
    }

    private fun initViewBinding(): View {
        val superclass = javaClass.genericSuperclass
        val aClass = (superclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        try {
            val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
            vb = method.invoke(null, layoutInflater) as T
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return vb.root
    }


    private fun initProgressDialog() {
//        val llPadding = 30
//        val ll = LinearLayout(context)
//        ll.orientation = LinearLayout.HORIZONTAL
//        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
//        ll.gravity = Gravity.CENTER
//        var llParam = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        llParam.gravity = Gravity.CENTER
//        ll.layoutParams = llParam
//
//        val progressBar = ProgressBar(context)
//        progressBar.isIndeterminate = true
//        progressBar.setPadding(0, 0, llPadding, 0)
//        progressBar.layoutParams = llParam
//
//        llParam = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        llParam.gravity = Gravity.CENTER
//        val tvText = TextView(context)
////        tvText.text = message
//        tvText.setTextColor(Color.parseColor("#000000"))
//        tvText.textSize = 20.toFloat()
//        tvText.layoutParams = llParam
//
//        ll.addView(progressBar)
//        ll.addView(tvText)
        var vb: DialogProgressBinding = DialogProgressBinding.inflate(layoutInflater)
        contentView = vb.root
        progressText = vb.tv

        progressDialog = AlertDialog.Builder(context).apply {
            setCancelable(true)
            setView(contentView)
        }.create().apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }


//        val dialog = builder.create()
//        val window = dialog.window
//        if (window != null) {
//            val layoutParams = WindowManager.LayoutParams()
//            layoutParams.copyFrom(dialog.window?.attributes)
//            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
//            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
//            dialog.window?.attributes = layoutParams
//        }
    }


    private lateinit var contentView: View
    fun showProgress(message: String) {
        progressText.text = message
        progressDialog.show()
    }

    fun hideProgress() {
        progressDialog.dismiss()
    }

    override fun onClick(p0: View?) {
        onViewClick(p0)
    }

    open abstract fun onViewClick(v: View?)

    open abstract fun initView()

    open abstract fun initData()


    fun <T : Activity> launchActivity(cls: Class<T>) {
        startActivity(Intent(context, cls))
    }

    fun <T : Activity> launchActivityForResult(cls: Class<T>, requestCode: Int) {
        startActivityForResult(Intent(context, cls), requestCode)
    }


    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun dialog(msg: String) {
        DialogUtil.showDialog(context, msg)
    }


    fun showPopupWindow(dependence: View, contextView: View, width: Int, height: Int): PopupWindow {
        return PopupWindow(contextView,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT).apply {
            isOutsideTouchable = true
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(Color.WHITE));
            elevation = 10f
            showAsDropDown(
                dependence,
                (0 - (width - dependence.width)),
                0
            )
        }
    }


    protected val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    protected val PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    protected val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    protected val PERMISSION_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    protected val PERMISSION_BLUETOOTH_CONNECT = Manifest.permission.BLUETOOTH_CONNECT
    protected val PERMISSION_BLUETOOTH_SCAN = Manifest.permission.BLUETOOTH_SCAN


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var grantAll = true
        grantResults.forEach {
            if (it == PackageManager.PERMISSION_DENIED) {
                grantAll = false
            }
        }
        onPermissionResult(requestCode, grantAll)
    }

    protected open fun onPermissionResult(requestCode: Int, grantAll: Boolean) {

    }


    protected fun checkAndRequestPermission(requestCode: Int, vararg permissions: String): Boolean {
        var flag = true
        val permissionList = ArrayList<String>()
        for (i in permissions.indices) {
            val permission = permissions[i]
            var ret = ContextCompat.checkSelfPermission(
                context,
                permission
            )
            if (ret != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission)
                flag = false
            }
        }
        val realPermissions = permissionList.toTypedArray()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (realPermissions.isNotEmpty())
                requestPermissions(realPermissions, requestCode)
        }
        return flag
    }


}