package com.jolimark.printerdemo.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.ArrayAdapter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.ble.BleUtil
import com.jolimark.printer.trans.ble.listener.BleDeviceDiscoveryListener
import com.jolimark.printer.trans.ble.listener.BluetoothStateListener
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityBleDevicesBinding
import com.jolimark.printerdemo.db.PrinterBean
import com.jolimark.printerdemo.db.PrinterTableDao
import com.jolimark.printerdemo.util.PermissionUtil

class BleDevicesActivity : BaseActivity<ActivityBleDevicesBinding>(), BluetoothStateListener,
    BleDeviceDiscoveryListener {

    private lateinit var bleUtil: BleUtil
    private lateinit var newDevicesArrayAdapters: ArrayAdapter<String>
    private val newDevices = ArrayList<BluetoothDevice>()

    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_enable -> {
                bleUtil.enableBluetooth()
            }

            R.id.btn_search -> {
                newDevices.clear()
                newDevicesArrayAdapters.clear()
                bleUtil.apply {
                    stopScanBle()
                    scanBle()
                }
            }
        }
    }

    override fun initView() {
        vb.pb.visibility = View.INVISIBLE
    }

    override fun initData() {
        PermissionUtil.checkAndRequestPermissions(
            context, arrayOf(
                PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN,
                PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION
            ), 1
        )
        bleUtil = BleUtil()
        if (bleUtil.isBluetoothEnabled) {
            vb.llBt.visibility = View.VISIBLE
            vb.btnEnable.visibility = View.GONE
        } else {
            vb.llBt.visibility = View.GONE
            vb.btnEnable.visibility = View.VISIBLE
        }
        newDevicesArrayAdapters = ArrayAdapter<String>(this, R.layout.item_bt)
        vb.newDevices.adapter = newDevicesArrayAdapters
        vb.newDevices.setOnItemClickListener { parent, view, position, id ->
            var device = newDevices[position]
            var printer = JmPrinter.getBlePrinter(applicationContext, device.address)
            PrinterTableDao.INSTANCE.insert(PrinterBean(printer))
            setResult(RESULT_OK, intent)
            finish()
        }

        bleUtil.apply {
            registerBluetoothReceiver(context)
            setBluetoothStateListener(this@BleDevicesActivity)
            setBLEDeviceDiscoveryListener(this@BleDevicesActivity)
        }
    }

    override fun onBluetoothEnabled() {
        toast(getString(R.string.tip_bt_on))
        vb.llBt.visibility = View.VISIBLE
        vb.btnEnable.visibility = View.GONE
    }

    override fun onBluetoothDisabled() {
        toast(getString(R.string.tip_bt_off))
        vb.llBt.visibility = View.GONE
        vb.btnEnable.visibility = View.VISIBLE
        bleUtil.stopScanBle()
        vb.pb.visibility = View.INVISIBLE
        newDevices.clear()
        newDevicesArrayAdapters.clear()
    }

    @SuppressLint("MissingPermission")
    override fun onDeviceFound(device: BluetoothDevice?) {
        if (device != null && !newDevices.contains(device) && device.name != null) {
            newDevices.add(device)
            newDevicesArrayAdapters!!.add(
                """
                    ${device.name}
                    ${device.address}
                    """.trimIndent()
            ) // 添加找到的蓝牙设备
            newDevicesArrayAdapters!!.notifyDataSetChanged()
        }
    }

    override fun onSearchEnd() {
        vb.pb.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        bleUtil.unregisterBluetoothReceiver(context)
    }
}