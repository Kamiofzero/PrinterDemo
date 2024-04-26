package com.jolimark.printerdemo.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.adapter.BaseAdapter
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
            mAdapter = DeviceAdapter(context)
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

            }, object : DialogUtil.Callback {
                override fun onClick(dialog: DialogInterface) {

                }

            }
        )
    }

    inner class DeviceAdapter(context: Context) :
        BaseAdapter<ItemDeviceBinding, BasePrinter>(context) {

        var deleteMode: Boolean = false
            private set

        fun deleteModeEnable(enable: Boolean) {
            deleteMode = enable
            notifyDataSetChanged()
        }

        override fun onBind(holder: VpHolder, list: List<BasePrinter>, position: Int) {
            var basePrinter = list[position]
            holder.vb.ivDelete.apply {
                visibility = if (deleteMode) View.VISIBLE else View.INVISIBLE
                setOnClickListener {
                    removeDevice(basePrinter)
                }
            }

            holder.vb.tvType.text = when (basePrinter.transtype) {
                TransType.WIFI -> "WiFi Printer"
                TransType.BLUETOOTH -> "Bluetooth Printer"
                TransType.USB -> "USB Printer"
                TransType.BLE->"Ble Printer"
            }
            holder.vb.tvInfo.text = basePrinter.deviceInfo
        }
    }
}