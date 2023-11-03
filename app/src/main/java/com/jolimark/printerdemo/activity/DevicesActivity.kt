package com.jolimark.printerdemo.activity

import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityDevicesBinding
import com.jolimark.printerdemo.databinding.ItemDeviceBinding
import com.jolimark.printerdemo.db.PrinterBean
import com.jolimark.printerdemo.db.PrinterTableDao
import com.jolimark.printerdemo.util.DialogUtil

class DevicesActivity : BaseActivity<ActivityDevicesBinding>() {

    private lateinit var mAdapter: DeviceAdapter
    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_addDevice -> {
                launchActivityForResult(DeviceAddActivity::class.java, 1)
            }

            R.id.btn_deleteDevice -> {
                if (JmPrinter.isDevicesEmpty()) {
                    toast(getString(R.string.tip_devices_empty))
                    return
                }
                mAdapter.apply {
                    deleteModeEnable(!mAdapter.deleteMode)
                }
            }

            R.id.btn_back -> {
                finish()
            }
        }
    }

    override fun initView() {
        vb.rvDevices.apply {
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
//            addItemDecoration(
//                DividerItemDecoration(
//                    context, LinearLayoutManager.VERTICAL
//                )
//            )
            mAdapter = DeviceAdapter()
            adapter = mAdapter
        }
    }

    override fun initData() {
        mAdapter.setList(JmPrinter.getPrinters())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mAdapter.setList(JmPrinter.getPrinters())
        }
    }

    private fun removeDevice(basePrinter: BasePrinter) {
        DialogUtil.showDialog(
            context,
            getString(R.string.tip_removeDevice),
            object : DialogUtil.Callback {
                override fun onClick(dialog: DialogInterface) {
                    JmPrinter.removePrinter(basePrinter)
                    PrinterTableDao.INSTANCE.delete(PrinterBean(basePrinter))
                    mAdapter.setList(JmPrinter.getPrinters())
                }

            }, null
        )
    }

    inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder<ItemDeviceBinding>>() {

        private var dataList = mutableListOf<BasePrinter>()

        var deleteMode: Boolean = false
            private set

        fun deleteModeEnable(enable: Boolean) {
            deleteMode = enable
            notifyDataSetChanged()
        }

        fun setList(list: MutableList<BasePrinter>) {
            dataList.clear()
            if (list.size != 0) dataList.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DeviceHolder<ItemDeviceBinding> {

            var vb = ItemDeviceBinding.inflate(LayoutInflater.from(context), parent, false)
            return DeviceHolder(vb.root, vb)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: DeviceHolder<ItemDeviceBinding>, position: Int) {
            var basePrinter = dataList[position]
            holder.vb.ivDelete.apply {
                visibility = if (deleteMode) View.VISIBLE else View.INVISIBLE
                setOnClickListener {
                    removeDevice(basePrinter)
                }
            }
            holder.vb.root.setOnClickListener {

            }

            holder.vb.tvType.text = when (basePrinter.transtype) {
                TransType.WIFI -> "WiFi Printer"
                TransType.BLUETOOTH -> "Bluetooth Printer"
                TransType.USB -> "USB Printer"
            }
            holder.vb.tvInfo.text = basePrinter.deviceInfo

        }

    }

    class DeviceHolder<T : ViewBinding> : RecyclerView.ViewHolder {
        var vb: T

        constructor(view: View, vb: T) : super(view) {
            this.vb = vb
        }
    }

}