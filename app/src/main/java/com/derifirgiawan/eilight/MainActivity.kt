package com.derifirgiawan.eilight

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private var successConnected: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnOn: Button = findViewById(R.id.btn_on)
        val btnOff: Button = findViewById(R.id.btn_off)
        val btnConnect: Button = findViewById(R.id.btn_connect)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        btnConnect.setOnClickListener {
            connectToBluetoothDevice()
        }

        btnOn.setOnClickListener {
            sendData("1")
        }


        btnOff.setOnClickListener {
            sendData("0")
        }

        if (successConnected) {
            btnConnect.visibility = View.GONE
        } else {
            btnConnect.visibility = View.VISIBLE
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectToBluetoothDevice() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            showToast("Not Permission")
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.BLUETOOTH_CONNECT)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                    9)

                // REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            val pairedDevice: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
            val text: TextView = findViewById(R.id.textView3)

            val device: BluetoothDevice? = pairedDevice.find { it.name == "HC-05" } // Ganti "NamaPerangkatBluetooth" dengan nama perangkat Bluetooth Anda

            if (device == null) {
                showToast("Perangkat Bluetooth tidak ditemukan.")
                successConnected = false
                return
            }

            val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID untuk koneksi serial SPP Bluetooth
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket.connect()
                outputStream = bluetoothSocket.outputStream
                showToast("Terhubung dengan perangkat Bluetooth.")
                successConnected = true
                text.setText(R.string.success_connected)
                text.setTextColor(Color.parseColor("#42B72B"))
            } catch (e: IOException) {
                text.setText(R.string.failed_connected)
                text.setTextColor(Color.parseColor("#F44336"))
                successConnected = false
                showToast("Gagal terhubung dengan perangkat Bluetooth.")
                e.printStackTrace()
            }
        }



    }
    private fun sendData(data: String) {
        if (::outputStream.isInitialized) {
            try {
                outputStream.write(data.toByteArray())
                showToast("Data terkirim: $data")
            } catch (e: IOException) {
                showToast("Gagal mengirim data.")
                e.printStackTrace()
            }
        } else {
            showToast("Belum terhubung dengan perangkat Bluetooth.")
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}