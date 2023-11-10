package com.jolimark.printerdemo.activity

import android.content.Context
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.jolimark.printer.callback.Callback
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.adapter.BaseAdapter
import com.jolimark.printerdemo.bean.ImgItem
import com.jolimark.printerdemo.databinding.ActivityPrintImgBinding
import com.jolimark.printerdemo.databinding.ItemVpBinding
import com.jolimark.printerdemo.printContent.PrintContent

class PrintImgActivity : PrintBaseActivity<ActivityPrintImgBinding>() {


    private lateinit var tabTitles: MutableList<String>

    private var items = mutableListOf<ImgItem>()
    override fun onPrinterSelect(printer: BasePrinter) {

        showProgress(getString(R.string.tip_printing))
        printer.printImg(items[vb.vp.currentItem].printData, object : Callback {
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
            "${getString(R.string.demo)}3",
        )
        TabLayoutMediator(vb.tl, vb.vp) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun initData() {
        items.apply {
            add(ImgItem(R.mipmap.img1, PrintContent.getBitmap(context, "img1")!!))
            add(ImgItem(R.mipmap.img2, PrintContent.getBitmap(context, "img2")!!))
            add(ImgItem(R.mipmap.img3, PrintContent.getBitmap(context, "img3")!!))
        }.also { mAdapter.setList(it) }
    }

    private inner class VpAdapter(context: Context) : BaseAdapter<ItemVpBinding, ImgItem>(context) {
        override fun onBind(holder: VpHolder, list: List<ImgItem>, position: Int) {
            holder.vb.iv.setImageResource(list[position].resourceId)
        }

    }

}