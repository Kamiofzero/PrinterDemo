package com.jolimark.printerdemo.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.View
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.adapter.BaseAdapter
import com.jolimark.printerdemo.databinding.ActivitySelectFileBinding
import com.jolimark.printerdemo.databinding.ItemDirectoryBinding
import com.jolimark.printerdemo.databinding.ItemFileBinding
import com.jolimark.printerdemo.databinding.PopupSortBinding
import com.jolimark.printerdemo.util.LocalFileUtil


class SelectFileActivity2 : BaseActivity<ActivitySelectFileBinding>() {

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

    override fun initData() {
//        var file = Environment.getExternalStorageDirectory()
//        selectFile(file)

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            var uri = data?.data
            LogUtil.i(TAG, "uri:$uri")
            if (uri != null) {
                DocumentFile.fromTreeUri(context, uri)?.let { tree(it) }
            }
        } else {
            finish()
        }
    }

    private fun tree(documentFile: DocumentFile) {
        if (documentFile.isDirectory) {
            var tlist = mutableListOf<DocumentFile>()
            documentFile.listFiles()?.forEach {
                LogUtil.i(TAG, "name:${it.name}")
                if (it.isDirectory) tlist.add(it)
                else {
                    LogUtil.i(TAG, "name:${it.name}")
                    if (it.name?.run {
                            endsWith(".prn") ||
                                    endsWith(".txt") ||
                                    endsWith(".png", true) ||
                                    endsWith(".jpg", true) ||
                                    endsWith(".jpeg", true)
                        } == true) {
                        tlist.add(it)
                    }
                }
            }
            sort(tlist)
            fileAdapter.setList(tlist)
            directoryList.add(documentFile)
            directoryAdapter.setList(directoryList)
            vb.rvDirectory.apply {
                postDelayed({ smoothScrollToPosition(directoryAdapter.itemCount) }, 50)
            }
        } else {
            setResult(RESULT_OK, intent.apply {
                var filePath = LocalFileUtil.getFilePath(context, documentFile.uri)
                LogUtil.i(TAG, "select file:$filePath")
                putExtra("file", filePath)
            })
            finish()
        }
    }


    private var directoryList = mutableListOf<DocumentFile>()


//    private fun selectFile(file: File) {
//        LogUtil.i(TAG, "select -> ${file.name}")
//        if (file.isDirectory) {
//            LogUtil.i(TAG, "isDirectory")
//            var tlist = mutableListOf<DocumentFile>()
//            var l = file.listFiles()
//            LogUtil.i(TAG, "listFiles:${l.size}")
//
//            file.listFiles()?.toMutableList()?.forEach {
//                LogUtil.i(TAG, "name:${it.name}")
//                if (it.isDirectory) tlist.add(it)
//                else {
//                    LogUtil.i(TAG, "name:${it.name}")
//                    if (it.name.run {
//                            endsWith(".prn") ||
//                                    endsWith(".txt") ||
//                                    endsWith(".png", true) ||
//                                    endsWith(".jpg", true) ||
//                                    endsWith(".jpeg", true)
//                        }) {
//                        tlist.add(it)
//                    }
//                }
//            }
//            sort(tlist)
//            fileAdapter.setList(tlist)
//            directoryList.add(file)
//            directoryAdapter.setList(directoryList)
//            vb.rvDirectory.apply {
//                postDelayed({ smoothScrollToPosition(directoryAdapter.itemCount) }, 50)
//            }
//        } else {
//            setResult(RESULT_OK, intent.apply {
//                putExtra("file", file.absolutePath)
//            })
//            finish()
//        }
//    }

    private fun selectDirectory(file: DocumentFile, position: Int) {
        var tList = directoryList.toTypedArray().let {
            it.copyOfRange(0, position).toMutableList()
        }
        directoryList.apply {
            clear()
            addAll(tList)
            directoryAdapter.setList(this)
        }
        //显示路径对应文件夹的内容
        tree(file)
    }


    private fun sort(list: MutableList<DocumentFile>) {
        if (sortType == SORT_NAME) {
            list.sortWith(Comparator { o1, o2 ->
                if (o1.isDirectory && o2.isFile) return@Comparator -1
                if (o1.isFile && o2.isDirectory) 1
                else {
                    o1.name!!.compareTo(o2.name!!, true).let {
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
        BaseAdapter<ItemDirectoryBinding, DocumentFile>(context) {
        override fun onBind(holder: VpHolder, list: List<DocumentFile>, position: Int) {
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

        override fun onItemClick(item: DocumentFile, position: Int) {
            super.onItemClick(item, position)
            selectDirectory(item, position)
        }
    }


    private inner class FileAdapter(context: Context) :
        BaseAdapter<ItemFileBinding, DocumentFile>(context) {
        override fun onBind(holder: VpHolder, list: List<DocumentFile>, position: Int) {
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
                    if (file.name!!.endsWith(".prn")) {
                        drawable = getDrawable(R.drawable.print_prn)
                    } else if (file.name!!.endsWith(".text")) {
                        drawable = getDrawable(R.drawable.print_text)
                    } else if (file.name!!.endsWith(".png", true)
                        || file.name!!.endsWith(".jpg", true)
                        || file.name!!.endsWith(".jpeg", true)
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

        override fun onItemClick(item: DocumentFile, position: Int) {
            super.onItemClick(item, position)
            tree(item)
        }
    }

}