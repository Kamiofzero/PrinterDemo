package com.jolimark.printerdemo.activity

import android.content.Context
import android.util.Log
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jolimark.printer.callback.Callback
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.adapter.BaseAdapter
import com.jolimark.printerdemo.bean.TextItem
import com.jolimark.printerdemo.databinding.ActivityPrintTextBinding
import com.jolimark.printerdemo.databinding.ItemVpBinding
import com.jolimark.printerdemo.printContent.PrintContent


class PrintTextActivity : PrintBaseActivity<ActivityPrintTextBinding>() {


    private lateinit var tabTitles: MutableList<String>

    private var items = mutableListOf<TextItem>()
    override fun onPrinterSelect(printer: BasePrinter) {
        showProgress(getString(R.string.tip_printing))
        printer.printText(items[vb.vp.currentItem].printData, object : Callback {
            override fun onSuccess() {
                hideProgress()
            }

            override fun onFail(code: Int, msg: String) {
                hideProgress()
                if (printer.isAntiMode) {
                    showAntiLossRetryDialog(printer, msg)
                } else {
                    toast(msg)
                }
            }
        })
    }

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_print -> {
                selectPrinter()
            }
        }
    }

    private lateinit var mAdapter: VpAdapter
    override fun initView() {
        mAdapter = VpAdapter(context)
        vb.vp.apply {
            adapter = mAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
        tabTitles = mutableListOf(
            "${getString(R.string.demo)}1",
            "${getString(R.string.demo)}2",
        )
        TabLayoutMediator(vb.tl, vb.vp) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun initData() {

        items.apply {
            add(TextItem(R.mipmap.text1, PrintContent.getText(context, "text1")!!))
            add(TextItem(R.mipmap.text2, PrintContent.getText(context, "text2")!!))
        }.also { mAdapter.setList(it) }

        Log.i("tag",items[0].printData)
        Log.i("tag",items[1].printData)
    }


    private inner class VpAdapter(context: Context) :
        BaseAdapter<ItemVpBinding, TextItem>(context) {

        override fun onBind(holder: VpHolder, list: List<TextItem>, position: Int) {
            holder.vb.iv.setImageResource(list[position].resourceId)

        }

    }


}