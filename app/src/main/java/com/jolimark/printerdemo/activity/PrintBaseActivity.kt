package com.jolimark.printerdemo.activity

import android.app.Dialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.jolimark.printer.callback.Callback
import com.jolimark.printer.printer.BasePrinter
import com.jolimark.printer.printer.BlePrinter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.DialogSelectPrinterBinding
import com.jolimark.printerdemo.databinding.ItemDeviceBinding
import com.jolimark.printerdemo.util.DialogUtil
import com.jolimark.printerdemo.util.SettingUtil

abstract class PrintBaseActivity<T : ViewBinding> : BaseActivity<T>() {

    private var selectDialog: Dialog? = null

    protected fun selectPrinter() {
        selectDialog = Dialog(context).apply {
            var vb = DialogSelectPrinterBinding.inflate(layoutInflater)
            setContentView(vb.root)
            vb.rv.apply {
                layoutManager = LinearLayoutManager(context)
                itemAnimator = DefaultItemAnimator()
                var mAdapter = DevicesAdapter()
                adapter = mAdapter
                mAdapter.setList(JmPrinter.getPrinters())
                show()
            }
        }
    }

    protected abstract fun onPrinterSelect(printer: BasePrinter)
    private fun onSelectPrinter(printer: BasePrinter) {
        LogUtil.i(TAG, "select printer: ${printer.deviceInfo}")
        selectDialog?.dismiss()
        printer.apply {
            enableVerification(SettingUtil.connectVerify)
            enableAntiLossMode(SettingUtil.antiLost)
            when (transtype) {
                TransType.WIFI -> {
                    setPackageSize(SettingUtil.wifiPrinterPackageSize)
                    setSendDelay(SettingUtil.wifiPrinterSendDelay)
                }

                TransType.BLUETOOTH -> {
                    setPackageSize(SettingUtil.bluetoothPrinterPackageSize)
                    setSendDelay(SettingUtil.bluetoothPrinterSendDelay)
                }

                TransType.USB -> {
                    setPackageSize(SettingUtil.usbPrinterPackageSize)
                    setSendDelay(SettingUtil.usbPrinterSendDelay)
                }

                TransType.BLE -> {
                    setPackageSize(SettingUtil.blePackageSize)
                    setSendDelay(SettingUtil.bleSendDelay)
                    (printer as BlePrinter).setMtu(SettingUtil.bleMtu)
                }
            }
        }
        onPrinterSelect(printer)
    }

    private fun resumePrintInAntiMode(printer: BasePrinter) {
        printer.resumePrint(object : Callback {
            override fun onSuccess() {
                toast(getString(R.string.tip_printSuccess))
            }

            override fun onFail(code: Int, msg: String) {
                showAntiLossRetryDialog(printer, msg)
            }
        })
    }

    protected fun showAntiLossRetryDialog(basePrinter: BasePrinter, msg: String) {
        var string = "${getString(R.string.tip_printFail)}: $msg"
        DialogUtil.showDialog(
            context,
            string,
            getString(R.string.resumePrint),
            object : DialogUtil.Callback {
                override fun onClick(dialog: DialogInterface) {
                    resumePrintInAntiMode(basePrinter)
                }
            },
            getString(R.string.rePrint),
            object : DialogUtil.Callback {
                override fun onClick(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            },
            getString(R.string.cancel),
            object : DialogUtil.Callback {
                override fun onClick(dialog: DialogInterface) {
                    dialog.dismiss()
                }
            })
    }

    inner class DevicesAdapter : RecyclerView.Adapter<DevicesAdapter.DeviceHolder>() {
        private var dataList = mutableListOf<BasePrinter>()

        fun setList(list: MutableList<BasePrinter>) {
            dataList.clear()
            if (list.size != 0) dataList.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DeviceHolder {
            var vb = ItemDeviceBinding.inflate(LayoutInflater.from(context), parent, false)
            return DeviceHolder(vb.root, vb)
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            holder.vb.ivDelete.visibility = View.INVISIBLE
            var printer = dataList[position]
            holder.vb.tvType.text = when (printer.transtype) {
                TransType.WIFI -> "WiFi Printer"
                TransType.BLUETOOTH -> "Bluetooth Printer"
                TransType.USB -> "USB Printer"
                TransType.BLE -> "Ble Printer"
            }
            holder.vb.tvInfo.text = printer.deviceInfo
            holder.vb.root.setOnClickListener {
                onSelectPrinter(printer)
            }
        }

        inner class DeviceHolder(
            itemView: View,
            var vb: ItemDeviceBinding
        ) : RecyclerView.ViewHolder(itemView)

    }

}