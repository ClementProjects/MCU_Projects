
package com.example.bigproject3;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity2 extends Activity {

    private static final String TAG = "BluetoothChat";

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private ConnectedThread mConnectedThread;
    private Handler mHandler;
    private TextView textHeartRate;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID

    private static final int MESSAGE_READ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        
        
        textHeartRate = (TextView) findViewById(R.id.textHeartRate);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case MESSAGE_READ:
                        String data = (String) message.obj;
                        textHeartRate.setText("Heart Rate: "+ data);
                        // Handle received data (heart rate) here
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            // Request to enable Bluetooth if it's not enabled
            // ...
        } else {
            // Get a list of paired devices
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    // Replace "DEVICE_NAME" with the name of your MSP430 Bluetooth module
                    if (device.getName().equals("DEVICE_NAME")) {
                        mDevice = device;
                        break;
                    }
                }
            }

            if (mDevice != null) {
                try {
                    mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
                    mSocket.connect();
                    mConnectedThread = new ConnectedThread(mSocket);
                    mConnectedThread.start();
                } catch (IOException e) {
                    Log.e(TAG, "Error connecting to device", e);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error creating streams", e);
            }
            mInputStream = tempIn;
            mOutputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mInputStream.read(buffer);
                    String data = new String(buffer, 0, bytes);
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, data).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from input stream", e);
                    break;
                }
            }
        }

        public void write(String data) {
            try {
                mOutputStream.write(data.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Error writing to output stream", e);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }
}