package com.jolimark.printerdemo.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.adapter.BaseAdapter
import com.jolimark.printerdemo.databinding.ActivitySelectFileBinding
import com.jolimark.printerdemo.databinding.ItemDirectoryBinding
import com.jolimark.printerdemo.databinding.ItemFileBinding
import com.jolimark.printerdemo.databinding.PopupSortBinding
import com.jolimark.printerdemo.util.DialogUtil
import java.io.File


class SelectFileActivity : BaseActivity<ActivitySelectFileBinding>() {

    private val SORT_NAME = 1
    private val SORT_CREATE = 2
    private val SORT_UP = 10
    private val SORT_DOWN = 11

    private var sortType: Int = SORT_NAME
    private var sortDirectionName: Int = SORT_UP
    private var sortDirectionCreate: Int = SORT_UP


    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_sort -> {
                PopupSortBinding.inflate(layoutInflater).apply {
                    btnName.apply {
                        setBackgroundColor(getColor(if (sortType == SORT_NAME) R.color.gray3 else android.R.color.transparent))
                        setCompoundDrawables(
                            null,
                            null,
                            getDrawable(
                                if (sortDirectionName == SORT_UP) R.mipmap.sort_up else (R.mipmap.sort_down)
                            )?.apply {
                                setBounds(0, 0, 30, 30);
                            },
                            null
                        )
                        setOnClickListener {
                            setBackgroundColor(getColor(R.color.gray3))
                            btnCreate.setBackgroundColor(getColor(android.R.color.transparent))
                            if (sortType == SORT_NAME) {
                                sortDirectionName =
                                    if (sortDirectionName == SORT_UP) SORT_DOWN else SORT_UP
                                setCompoundDrawables(
                                    null, null,
                                    getDrawable(
                                        if (sortDirectionName == SORT_UP) R.mipmap.sort_up else (R.mipmap.sort_down)
                                    )?.apply {
                                        setBounds(0, 0, 30, 30);
                                    },
                                    null
                                )
                            } else
                                sortType = SORT_NAME

                            fileAdapter.getList().apply {
                                sort(this)
                                fileAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    btnCreate.apply {
                        setBackgroundColor(getColor(if (sortType == SORT_CREATE) R.color.gray3 else android.R.color.transparent))
                        setCompoundDrawables(
                            null,
                            null,
                            getDrawable(
                                if (sortDirectionCreate == SORT_UP) R.mipmap.sort_up else (R.mipmap.sort_down)
                            )?.apply {
                                setBounds(0, 0, 30, 30);
                            },
                            null
                        )
                        setOnClickListener {
                            setBackgroundColor(getColor(R.color.gray3))
                            btnName.setBackgroundColor(getColor(android.R.color.transparent))
                            if (sortType == SORT_CREATE) {
                                sortDirectionCreate =
                                    if (sortDirectionCreate == SORT_UP) SORT_DOWN else SORT_UP
                                setCompoundDrawables(
                                    null, null, getDrawable(
                                        if (sortDirectionCreate == SORT_UP) R.mipmap.sort_up else (R.mipmap.sort_down)
                                    )?.apply {
                                        setBounds(0, 0, 30, 30);
                                    }, null
                                )
                            } else
                                sortType = SORT_CREATE
                            fileAdapter.getList().apply {
                                sort(this)
                                fileAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    showPopupWindow(v, root, 360, 300)
                }
            }
        }
    }

    private lateinit var directoryAdapter: DirectoryAdapter
    private lateinit var fileAdapter: FileAdapter

    override fun initView() {
        vb.rvDirectory.apply {
            layoutManager =
                LinearLayoutManager(context).apply { orientation = LinearLayoutManager.HORIZONTAL }
            itemAnimator = DefaultItemAnimator()
            directoryAdapter = DirectoryAdapter(context)
            adapter = directoryAdapter
        }
        vb.rvFiles.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            fileAdapter = FileAdapter(context)
            adapter = fileAdapter
        }

    }

    // 请求文件访问权限的请求码，可以是任意整数值
    private val REQUEST_MANAGE_FILES_ACCESS = 2;

    //申请所有文件访问权限
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestPermission() {
        //判断是否有管理外部存储的权限
        if (!Environment.isExternalStorageManager()) {
            var intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.data = Uri.parse("package:$packageName");
            startActivityForResult(intent, REQUEST_MANAGE_FILES_ACCESS);
        } else {
            // 已有所有文件访问权限，可直接执行文件相关操作
            showFiles()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MANAGE_FILES_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // TODO: 2023/11/22
                    // 用户已经授予文件访问权限
                    // 在这里执行创建文件夹和初始化数据库的操作
                    LogUtil.i(TAG, "用户已经授予文件访问权限")
                    showFiles()
                } else {
                    // TODO: 2023/11/22
                    // 用户尚未授予文件访问权限
                    // 可以在此处处理用户未授予权限的情况
                    LogUtil.i(TAG, "用户尚未授予文件访问权限")
                    DialogUtil.showDialog(
                        context,
                        "用户尚未授予文件访问权限，无法选择文件",
                        object : DialogUtil.Callback {
                            override fun onClick(dialog: DialogInterface) {
                                finish()
                            }

                        })
                }
            }
        }
    }

    private fun showFiles() {
        var file = Environment.getExternalStorageDirectory()
        selectFile(file)
    }

    override fun initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestPermission()
        } else {
            showFiles()
        }
    }


    private var directoryList = mutableListOf<File>()


    private fun selectFile(file: File) {
        LogUtil.i(TAG, "select -> ${file.name}")
        if (file.isDirectory) {
            LogUtil.i(TAG, "isDirectory")
            var tlist = mutableListOf<File>()
            var l = file.listFiles()
            LogUtil.i(TAG, "listFiles:${l.size}")

            file.listFiles()?.toMutableList()?.forEach {
                LogUtil.i(TAG, "name:${it.name}")
                if (it.isDirectory) tlist.add(it)
                else {
                    LogUtil.i(TAG, "name:${it.name}")
                    if (it.name.run {
                            endsWith(".prn") ||
                                    endsWith(".txt") ||
                                    endsWith(".png", true) ||
                                    endsWith(".jpg", true) ||
                                    endsWith(".jpeg", true)
                        }) {
                        tlist.add(it)
                    }
                }
            }
            sort(tlist)
            fileAdapter.setList(tlist)
            directoryList.add(file)
            directoryAdapter.setList(directoryList)
            vb.rvDirectory.apply {
                postDelayed({ smoothScrollToPosition(directoryAdapter.itemCount) }, 50)
            }
        } else {
            setResult(RESULT_OK, intent.apply {
                putExtra("file", file.absolutePath)
            })
            finish()
        }
    }

    private fun selectDirectory(file: File, position: Int) {
        var tList = directoryList.toTypedArray().let {
            it.copyOfRange(0, position).toMutableList()
        }
        directoryList.apply {
            clear()
            addAll(tList)
            directoryAdapter.setList(this)
        }
        //显示路径对应文件夹的内容
        selectFile(file)
    }


    private fun sort(list: MutableList<File>) {
        if (sortType == SORT_NAME) {
            list.sortWith(Comparator { o1, o2 ->
                if (o1.isDirectory && o2.isFile) return@Comparator -1
                if (o1.isFile && o2.isDirectory) 1 else {
                    o1.name.compareTo(o2.name, true).let {
                        if (sortDirectionName == SORT_DOWN)
                            -it
                        else
                            it
                    }
                }
            })
        } else if (sortType == SORT_CREATE) {
            list.sortWith { o1, o2 ->
                val diff: Long = o1.lastModified() - o2.lastModified()
                (if (diff > 0) 1 else if (diff == 0L) 0 else -1).let {
                    if (sortDirectionCreate == SORT_DOWN)
                        -it
                    else
                        it
                }
            }
        }
    }

    private inner class DirectoryAdapter(context: Context) :
        BaseAdapter<ItemDirectoryBinding, File>(context) {
        override fun onBind(holder: VpHolder, list: List<File>, position: Int) {
            var rootName = Environment.getExternalStorageDirectory().name
            if (list[position].name == rootName) {
                holder.vb.tvDirectory.text = context.getString(R.string.rootDirectory)
                holder.vb.iv.visibility = View.GONE
            } else {
                holder.vb.tvDirectory.text = list[position].name
                holder.vb.iv.visibility = View.VISIBLE
            }

            holder.vb.tvDirectory.background =
                if (position == itemCount - 1) getDrawable(R.drawable.bg_directory)
                else null
        }

        override fun onItemClick(item: File, position: Int) {
            super.onItemClick(item, position)
            selectDirectory(item, position)
        }
    }


    private inner class FileAdapter(context: Context) :
        BaseAdapter<ItemFileBinding, File>(context) {
        override fun onBind(holder: VpHolder, list: List<File>, position: Int) {
            holder.vb.tvFile.apply {
                text = list[position].name
                var file = list[position]
                if (file.isDirectory) setCompoundDrawables(
                    getDrawable(R.drawable.print_file)?.apply {
                        setBounds(0, 0, 60, 60);
                    },
                    null,
                    null,
                    null
                ) else {
                    var drawable: Drawable? = null
                    if (file.name.endsWith(".prn")) {
                        drawable = getDrawable(R.drawable.print_prn)
                    } else if (file.name.endsWith(".text")) {
                        drawable = getDrawable(R.drawable.print_text)
                    } else if (file.name.endsWith(".png", true)
                        || file.name.endsWith(".jpg", true)
                        || file.name.endsWith(".jpeg", true)
                    ) {
                        drawable = getDrawable(R.drawable.print_image)
                    }
                    drawable?.setBounds(0, 0, 60, 60);
                    setCompoundDrawables(
                        drawable,
                        null,
                        null,
                        null
                    )
                }
            }

        }

        override fun onItemClick(item: File, position: Int) {
            super.onItemClick(item, position)
            selectFile(item)
        }
    }

}