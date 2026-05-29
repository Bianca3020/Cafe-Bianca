package com.biancaputri.pos.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.biancaputri.pos.R
import com.biancaputri.pos.adapter.PrinterAdapter
import java.util.UUID
import kotlin.concurrent.thread

class PrinterActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnScan: Button
    private lateinit var tvStatusPrinter: TextView

    private var bluetoothAdapter: BluetoothAdapter? = null
    private val deviceNames = mutableListOf<String>()
    private val deviceAddresses = mutableListOf<String>()
    private lateinit var adapter: PrinterAdapter

    private var pendingPrintText: String? = null
    private val requestBluetoothPermissionsCode = 101

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let {
                        if (!deviceAddresses.contains(it.address)) {
                            val name = try { it.name } catch (e: SecurityException) { null } ?: getString(R.string.unknown_device)
                            deviceNames.add(name)
                            deviceAddresses.add(it.address)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Toast.makeText(context, getString(R.string.pencarian_selesai), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        btnBack = findViewById(R.id.btnBack)
        recyclerView = findViewById(R.id.recyclerViewPrinters)
        btnScan = findViewById(R.id.btnScanPrinter)
        tvStatusPrinter = findViewById(R.id.tvStatusPrinter)

        pendingPrintText = intent.getStringExtra(EXTRA_PRINT_TEXT)
        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PrinterAdapter(deviceNames, deviceAddresses) { address ->
            if (hasBluetoothPermissions()) connectPrinter(address) else requestBluetoothPermissions()
        }
        recyclerView.adapter = adapter

        btnScan.setOnClickListener {
            if (hasBluetoothPermissions()) startScanning() else requestBluetoothPermissions()
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        registerReceiver(receiver, filter)

        if (hasBluetoothPermissions()) loadPairedDevices() else requestBluetoothPermissions()
        updateStatusUI()
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        ActivityCompat.requestPermissions(this, permissions, requestBluetoothPermissionsCode)
    }

    @SuppressLint("MissingPermission")
    private fun loadPairedDevices() {
        if (!hasBluetoothPermissions()) return
        deviceNames.clear()
        deviceAddresses.clear()
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            val name = try { device.name } catch (e: SecurityException) { getString(R.string.unknown_device) }
            deviceNames.add(getString(R.string.perangkat_terpasang, name))
            deviceAddresses.add(device.address)
        }
        adapter.notifyDataSetChanged()
    }

    private fun updateStatusUI() {
        val status = if (bluetoothSocket?.isConnected == true) getString(R.string.terhubung) else getString(R.string.tidak_terhubung)
        tvStatusPrinter.text = getString(R.string.status_printer, status)
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        if (!hasBluetoothPermissions()) return
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, getString(R.string.aktifkan_gps_scan), Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(this, getString(R.string.aktifkan_bluetooth), Toast.LENGTH_SHORT).show()
            return
        }
        loadPairedDevices()
        try {
            if (bluetoothAdapter?.isDiscovering == true) bluetoothAdapter?.cancelDiscovery()
            bluetoothAdapter?.startDiscovery()
            Toast.makeText(this, getString(R.string.mencari_perangkat), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) { }
    }

    @SuppressLint("MissingPermission")
    private fun connectPrinter(address: String) {
        val device = bluetoothAdapter?.getRemoteDevice(address)
        val deviceName = try { device?.name } catch (e: SecurityException) { null } ?: address
        tvStatusPrinter.text = getString(R.string.menghubungkan_ke, deviceName)

        thread {
            try {
                bluetoothSocket?.close()
                try {
                    bluetoothSocket = device?.createRfcommSocketToServiceRecord(MY_UUID)
                    bluetoothSocket?.connect()
                } catch (e1: Exception) {
                    val m = device?.javaClass?.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                    bluetoothSocket = m?.invoke(device, 1) as? BluetoothSocket
                    bluetoothSocket?.connect()
                }
                
                getSharedPreferences("printer_pref", MODE_PRIVATE).edit().putString("last_printer_address", address).apply()

                runOnUiThread {
                    tvStatusPrinter.text = getString(R.string.status_printer, getString(R.string.terhubung))
                    Toast.makeText(this, getString(R.string.berhasil_terhubung), Toast.LENGTH_SHORT).show()
                    
                    pendingPrintText?.let {
                        executePrint(it)
                        pendingPrintText = null
                        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1000)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.gagal_konek, e.message ?: ""), Toast.LENGTH_SHORT).show()
                    updateStatusUI()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(receiver) } catch (e: Exception) { }
    }

    companion object {
        private val MY_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket: BluetoothSocket? = null
        const val EXTRA_PRINT_TEXT = "EXTRA_PRINT_TEXT"

        @SuppressLint("MissingPermission")
        fun sendPrint(context: Context, text: String) {
            val sharedPref = context.getSharedPreferences("printer_pref", AppCompatActivity.MODE_PRIVATE)
            val lastAddress = sharedPref.getString("last_printer_address", null)

            if (bluetoothSocket?.isConnected == true) {
                executePrint(text)
                return
            }

            thread {
                val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter = bluetoothManager.adapter
                
                if (lastAddress != null && bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                    try {
                        val device = bluetoothAdapter.getRemoteDevice(lastAddress)
                        bluetoothSocket?.close()
                        try {
                            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
                            bluetoothSocket?.connect()
                        } catch (e1: Exception) {
                            val m = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                            bluetoothSocket = m.invoke(device, 1) as? BluetoothSocket
                            bluetoothSocket?.connect()
                        }
                        if (bluetoothSocket?.isConnected == true) {
                            executePrint(text)
                            return@thread
                        }
                    } catch (e: Exception) {
                        Log.e("PrinterActivity", context.getString(R.string.gagal_koneksi_terakhir, e.message ?: "-"))
                    }
                }

                val intent = Intent(context, PrinterActivity::class.java).apply {
                    putExtra(EXTRA_PRINT_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }

        private fun executePrint(text: String) {
            thread {
                try {
                    val out = bluetoothSocket?.outputStream
                    out?.write(byteArrayOf(0x1B, 0x40))
                    out?.write(text.toByteArray())
                    out?.write(byteArrayOf(0x0A, 0x0A, 0x0A))
                    out?.flush()
                } catch (e: Exception) { }
            }
        }
    }
}
