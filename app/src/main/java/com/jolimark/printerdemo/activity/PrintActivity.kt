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
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.TransType
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityPrintBinding
import com.jolimark.printerdemo.databinding.DialogSelectPrinterBinding
import com.jolimark.printerdemo.databinding.ItemDeviceBinding
import com.jolimark.printerdemo.util.DialogUtil
import com.jolimark.printerdemo.util.SettingUtil

class PrintActivity : BaseActivity<ActivityPrintBinding>() {

    private val PRINT_TEXT = 1
    private val PRINT_IMAGE = 2
    private val PRINT_PRN = 3
    private val PRINT_FILE = 4

    private var printItem: Int? = null
    private var selectDialog: Dialog? = null
    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_printText -> {
                launchActivity(PrintTextActivity::class.java)
            }

            R.id.btn_printImage -> {
                launchActivity(PrintImgActivity::class.java)
            }

            R.id.btn_printPrn -> {
                printItem = PRINT_PRN
                selectPrinter()
            }

            R.id.btn_printFile -> {
                printItem = PRINT_FILE
                selectPrinter()
            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }

    private fun selectPrinter() {
        selectDialog = Dialog(context).apply {
            var vb = DialogSelectPrinterBinding.inflate(LayoutInflater.from(context))
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
                    setPackageSize(SettingUtil.wifiPrinterPackageSize)
                    setSendDelay(SettingUtil.wifiPrinterSendDelay)
                }

                TransType.USB -> {
                    setPackageSize(SettingUtil.wifiPrinterPackageSize)
                    setSendDelay(SettingUtil.wifiPrinterSendDelay)
                }
            }
        }
        when (printItem) {
            PRINT_TEXT -> {
                printText(printer)
            }

            PRINT_IMAGE -> {
                printImage(printer)

            }

            PRINT_PRN -> {
                printPRN(printer)

            }

            PRINT_FILE -> {
                printFile(printer)

            }
        }
    }

    private fun printText(printer: BasePrinter) {
        var textBytes = "abcd"
        printer.printText(textBytes, object : Callback {
            override fun onSuccess() {
                toast(getString(R.string.tip_printSuccess))
            }

            override fun onFail(code: Int, msg: String) {
                if (printer.isAntiMode) {
                    showAntiLossRetryDialog(printer, msg)
                } else {
                    toast(msg)
                }
            }


        })
    }


    private fun printImage(printer: BasePrinter) {

    }

    private fun printPRN(printer: BasePrinter) {

    }

    private fun printFile(printer: BasePrinter) {

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

    private fun showAntiLossRetryDialog(basePrinter: BasePrinter, msg: String) {
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


    inner class DevicesAdapter : RecyclerView.Adapter<DeviceHolder<ItemDeviceBinding>>() {
        private var dataList = mutableListOf<BasePrinter>()

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
            holder.vb.ivDelete.visibility = View.INVISIBLE
            var printer = dataList[position]
            holder.vb.tvType.text = when (printer.transtype) {
                TransType.WIFI -> "WiFi Printer"
                TransType.BLUETOOTH -> "Bluetooth Printer"
                TransType.USB -> "USB Printer"
            }
            holder.vb.tvInfo.text = printer.deviceInfo
            holder.vb.root.setOnClickListener {
                onSelectPrinter(printer)
            }
        }

    }

    inner class DeviceHolder<T : ViewBinding>(
        itemView: View,
        var vb: T
    ) : RecyclerView.ViewHolder(itemView)

}