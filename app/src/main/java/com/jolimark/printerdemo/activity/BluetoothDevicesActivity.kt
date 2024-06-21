package com.jolimark.printerdemo.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.ArrayAdapter
import com.jolimark.printer.printer.JmPrinter
import com.jolimark.printer.trans.bluetooth.BluetoothUtil
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceBondListener
import com.jolimark.printer.trans.bluetooth.listener.BTDeviceDiscoveryListener
import com.jolimark.printer.trans.bluetooth.listener.BluetoothStateListener
import com.jolimark.printer.util.LogUtil
import com.jolimark.printerdemo.R
import com.jolimark.printerdemo.databinding.ActivityBluetoothDevicesBinding
import com.jolimark.printerdemo.db.PrinterBean
import com.jolimark.printerdemo.db.PrinterTableDao
import com.jolimark.printerdemo.util.DialogUtil
import com.jolimark.printerdemo.util.PermissionUtil
import kotlin.concurrent.thread

@SuppressLint("MissingPermission")

class BluetoothDevicesActivity : BaseActivity<ActivityBluetoothDevicesBinding>(),
    BluetoothStateListener, BTDeviceDiscoveryListener, BTDeviceBondListener {

    private lateinit var newDevicesArrayAdapters: ArrayAdapter<String>
    private lateinit var pairDevicesArrayAdapters: ArrayAdapter<String>

    private val newDevices = ArrayList<BluetoothDevice>()
    private val pairDevices = ArrayList<BluetoothDevice>()

    private var selectDevice: BluetoothDevice? = null


    override fun onViewClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }

            R.id.btn_enable -> {
                if (PermissionUtil.checkPermissions(
                        context,
                        arrayOf(PERMISSION_BLUETOOTH, PERMISSION_BLUETOOTH_CONNECT)
                    )
                )
                    bluetoothUtil.enableBluetooth()
                else
                    DialogUtil.showDialog(context, getString(R.string.tip_bt_p_connect))
            }

            R.id.btn_search -> {
                if (PermissionUtil.checkPermissions(
                        context,
                        arrayOf(
                            PERMISSION_BLUETOOTH_SCAN,
                            PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION
                        )
                    )
                ) {
                    newDevices.clear()
                    newDevicesArrayAdapters.clear()
                    bluetoothUtil.apply {
                        stopDiscoveryBTDevice()
                        startDiscoveryBTDevice()
                    }
                } else
                    DialogUtil.showDialog(context, getString(R.string.tip_bt_p_scan))
            }
        }
    }

    private lateinit var bluetoothUtil: BluetoothUtil

    override fun initView() {

        pairDevicesArrayAdapters = ArrayAdapter<String>(this, R.layout.item_bt)
        vb.pairDevices.adapter = pairDevicesArrayAdapters
        vb.pairDevices.setOnItemClickListener { adapterView, view, i, l ->
            val device = pairDevices[i]
//            var printer = JmPrinter.createPrinter(
//                TransType.BLUETOOTH,
//                "Jolimark[${device.address}]"
//            ) as BluetoothPrinter
//            printer.mac = device.address
            var printer = JmPrinter.getBluetoothPrinter(device.address)
            PrinterTableDao.INSTANCE.insert(PrinterBean(printer))
            setResult(RESULT_OK, intent)
            finish()
        }
        newDevicesArrayAdapters = ArrayAdapter<String>(this, R.layout.item_bt)
        vb.newDevices.adapter = newDevicesArrayAdapters
        vb.newDevices.setOnItemClickListener { parent, view, position, id ->
            val device = newDevices[position]
            if (device.bondState == BluetoothDevice.BOND_NONE) {
                selectDevice = device
                showProgress(getString(R.string.bonding))
                thread { bluetoothUtil.bondDevice(device.address) }
            }
        }

        vb.pb.visibility = View.INVISIBLE
    }

    override fun initData() {
        bluetoothUtil = BluetoothUtil()

        if (bluetoothUtil.isBluetoothEnabled) {
            vb.llBt.visibility = View.VISIBLE
            vb.btnEnable.visibility = View.GONE
        } else {
            vb.llBt.visibility = View.GONE
            vb.btnEnable.visibility = View.VISIBLE
        }

        PermissionUtil.checkAndRequestPermissions(
            context, arrayOf(
                PERMISSION_BLUETOOTH,
                PERMISSION_BLUETOOTH_CONNECT, PERMISSION_BLUETOOTH_SCAN,
                PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION
            ), 1
        ).let {
            if (it) {
                updateBondDevices()
            }
        }

        bluetoothUtil.apply {
            registerBluetoothReceiver(context)
            setBluetoothStateListener(this@BluetoothDevicesActivity)
            setBTDeviceDiscoveryListener(this@BluetoothDevicesActivity)
            setBTDeviceBondListener(this@BluetoothDevicesActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (PermissionUtil.checkPermissions(
                context,
                arrayOf(
                    PERMISSION_BLUETOOTH_SCAN,
                    PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION
                )
            )
        ) {
            bluetoothUtil.stopDiscoveryBTDevice()
        }
        bluetoothUtil.unregisterBluetoothReceiver(context)
    }


    @SuppressLint("MissingPermission")
    private fun updateBondDevices() {
        pairDevicesArrayAdapters!!.clear()
        pairDevices.clear()
        for (device in bluetoothUtil.bondDevices) {
            val str = """
            ${device.name}
            ${device.address}
            """.trimIndent()
            pairDevicesArrayAdapters!!.add(str)
            pairDevices.add(device)
        }
        pairDevicesArrayAdapters!!.notifyDataSetChanged()
    }

    override fun onBluetoothEnabled() {
        toast(getString(R.string.tip_bt_on))
        vb.llBt.visibility = View.VISIBLE
        vb.btnEnable.visibility = View.GONE
        updateBondDevices()
    }

    override fun onBluetoothDisabled() {
        toast(getString(R.string.tip_bt_off))
        vb.llBt.visibility = View.GONE
        vb.btnEnable.visibility = View.VISIBLE
        bluetoothUtil.stopDiscoveryBTDevice()
        vb.pb.visibility = View.INVISIBLE
        newDevices.clear()
        newDevicesArrayAdapters.clear()
    }

    override fun onDeviceStart() {
        vb.pb.visibility = View.VISIBLE
    }

    override fun onDeviceFound(device: BluetoothDevice?) {
        if (device != null && !newDevices.contains(device) && !pairDevices.contains(device) && device.name != null) {
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

    override fun onDeviceFinish() {
        vb.pb.visibility = View.INVISIBLE
    }


    override fun onBTDeviceBonding(device: BluetoothDevice?) {
        showProgress(getString(R.string.bonding))
    }

    override fun onBTDeviceBonded(device: BluetoothDevice?) {
        updateBondDevices()
        toast(getString(R.string.bonded))
        hideProgress()
//        var printer = JmPrinter.createPrinter(
//            TransType.BLUETOOTH,
//            "Jolimark[${device?.address}]"
//        ) as BluetoothPrinter
//        printer.mac = device?.address
        var printer = JmPrinter.getBluetoothPrinter(device?.address)
        PrinterTableDao.INSTANCE.insert(PrinterBean(printer))
        LogUtil.i(TAG, "select device [${printer.mac}]")
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onBTDeviceBondNone(device: BluetoothDevice?) {
        hideProgress()
        toast(getString(R.string.bond_fail))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtil.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults,
            object : PermissionUtil.RequestPermissionsCallback {
                override fun onPermissionResult(
                    requestCode: Int,
                    grantPermissions: Array<String>,
                    deniedPermissions: Array<String>
                ) {

                    grantPermissions.find { it == PERMISSION_BLUETOOTH_CONNECT }
                        ?.also { updateBondDevices() }

                    deniedPermissions.filter { it == PERMISSION_BLUETOOTH_CONNECT || it == PERMISSION_BLUETOOTH }
                        ?.also {
                            if (it.isNotEmpty())
                                DialogUtil.showDialog(context, getString(R.string.tip_bt_p_connect))
                        }

                    deniedPermissions.filter(fun(s: String): Boolean { return s == PERMISSION_BLUETOOTH_SCAN || s == PERMISSION_ACCESS_FINE_LOCATION || s == PERMISSION_ACCESS_COARSE_LOCATION })
                        ?.also {
                            if (it.isNotEmpty())
                                DialogUtil.showDialog(context, getString(R.string.tip_bt_p_scan))
                        }

                }
            })
    }

}