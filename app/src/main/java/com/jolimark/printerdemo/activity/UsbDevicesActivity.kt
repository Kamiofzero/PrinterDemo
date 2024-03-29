package com.jolimark.printerdemo.activity

import android.hardware.usb.UsbDevice
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.usb.UsbUtil
import com.jolimark.printer.trans.usb.UsbUtil.UsbPermissionRequestListener
import com.jolimark.printerdemo.PrinterDemoApp
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityUsbDevicesBinding
import com.jolimark.printerdemo.db.PrinterBean
import com.jolimark.printerdemo.db.PrinterTableDao

class UsbDevicesActivity : BaseActivity<ActivityUsbDevicesBinding>() {

    private lateinit var foundDevicesArrayAdapter: ArrayAdapter<String>
    private var deviceList = mutableListOf<UsbDevice>()
    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_search -> {
                deviceList.clear()
                foundDevicesArrayAdapter.clear()
                usbUtil.getUsbDevices(context).apply {
                    deviceList.addAll(this)
                }.forEach {
                    foundDevicesArrayAdapter.add(
                        """
                    vid: ${it.vendorId}
                    pid: ${it.productId}
                    """.trimIndent()
                    )
                }

            }
        }
    }

    private lateinit var usbUtil: UsbUtil

    override fun initView() {

        foundDevicesArrayAdapter = ArrayAdapter(context, R.layout.item_bt)
        vb.foundDevices.apply {
            adapter = foundDevicesArrayAdapter
            onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    var usbDevice = deviceList[position]
                    usbUtil.requestUsbPermissionForCustomSystem(
                        context,
                        usbDevice,
                        object : UsbPermissionRequestListener {
                            override fun onRequestGranted() {
                                var printer = JmPrinter.getUsbPrinter(
                                    PrinterDemoApp.context,//用application的context
                                    usbDevice.vendorId,
                                    usbDevice.productId
                                )
                                PrinterTableDao.INSTANCE.insert(PrinterBean(printer))
                                setResult(RESULT_OK)
                                finish()
                            }

                            override fun onRequestDenied() {
                                runOnUiThread {
                                    toast(getString(R.string.tip_usbDenied))
                                }
                            }
                        })
                }
        }
    }

    override fun initData() {
        usbUtil = UsbUtil()
    }
}