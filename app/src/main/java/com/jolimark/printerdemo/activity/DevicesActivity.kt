package com.jolimark.printerdemo.activity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityDevicesBinding
import com.jolimark.printerdemo.databinding.ItmDeviceBinding

class DevicesActivity : BaseActivity<ActivityDevicesBinding>() {

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_addDevice -> {
                launchActivityForResult(DeviceAddActivity::class.java, 1)
            }

            R.id.btn_deleteDevice -> {

            }

            R.id.btn_back -> {
                finish()
            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
    }

    inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder>() {

        var dataList = mutableListOf<BasePrinter>()

        fun setList(list: MutableList<BasePrinter>) {
            dataList.clear()
            if (list.size != 0) dataList.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceHolder {
        }

        override fun getItemCount(): Int {
        }

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
        }

    }

    class DeviceHolder : RecyclerView.ViewHolder {

        constructor(view: View) : super(view) {

        }
    }

}