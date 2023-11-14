package com.jolimark.printerdemo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.jolimark.printer.util.LogUtil
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

abstract class BaseAdapter<VB : ViewBinding, T> :
    RecyclerView.Adapter<BaseAdapter<VB, T>.VpHolder> {


    private var context: Context
    private var itemList = mutableListOf<T>()

    constructor(context: Context) {
        this.context = context
    }

    fun setList(list: MutableList<T>) {
        itemList.clear()
        itemList.addAll(list)
        notifyDataSetChanged()
    }

    fun getList(): List<T> {
        return itemList
    }

    inner class VpHolder(view: View, var vb: VB) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VpHolder {
        var vb = initViewBinding(parent)
        return VpHolder(vb!!.root, vb)
    }

    private fun initViewBinding(parent: ViewGroup): VB? {
        val superclass = javaClass.genericSuperclass
        val aClass = (superclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        try {
            val method = aClass.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            return method.invoke(null, LayoutInflater.from(context), parent, false) as VB
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    abstract fun onBind(holder: VpHolder, list: List<T>, position: Int)
    override fun onBindViewHolder(holder: VpHolder, position: Int) {
        onBind(holder, itemList, position)
        holder.vb.root.setOnClickListener {
            onItemClick(itemList[position], position)
        }
        holder.vb.root.setOnLongClickListener {
            onItemLongClick(itemList[position], position)
            return@setOnLongClickListener false
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    private var onItemLongClickListener: OnItemLongClickListener? = null
    fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        onItemLongClickListener = listener
    }

    open fun onItemClick(item: T, position: Int) {

    }

    open fun onItemLongClick(item: T, position: Int) {

    }

}