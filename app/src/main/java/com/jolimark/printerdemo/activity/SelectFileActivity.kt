package com.jolimark.printerdemo.activity

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.adapter.BaseAdapter
import com.jolimark.printerdemo.databinding.ActivitySelectFileBinding
import com.jolimark.printerdemo.databinding.ItemDirectoryBinding
import com.jolimark.printerdemo.databinding.ItemFileBinding
import java.io.File

class SelectFileActivity : BaseActivity<ActivitySelectFileBinding>() {


    override fun onViewClick(v: View?) {
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

    private var directoryList = mutableListOf<File>()


    private fun selectFile(file: File) {
        if (file.isDirectory) {
            var tlist = mutableListOf<File>()
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


    override fun initData() {
        var file = Environment.getExternalStorageDirectory()
        selectFile(file)
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