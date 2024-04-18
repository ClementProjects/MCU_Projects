package com.example.bigproject3;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private BluetoothAdapter bluetoothAdapter;
    public BluetoothSocket mSocket;
    private BroadcastReceiver receiver;

    private ListView listView;
    private ArrayAdapter<String> deviceAdapter;
    private List<String> listDevices;

    private TextView textData;

    private Button buttonReturn;
    private Button btnOpen;
    private Button btnSearch;
    private Button btnGo;
    private String macAddress;

    private EditText editSend;
    private Button btnSend;

    private TextView textStatus;

    private ComThread mThread = null;

    private final int CONNECTED = 1000;
    private final int DISCONNECTED = 1001;
    private int state = DISCONNECTED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setTitle("Bluetooth Activity");

        listView = (ListView) this.findViewById(R.id.listView);
        listDevices = new ArrayList<String>();
        deviceAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listDevices);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(this);

        editSend = (EditText) findViewById(R.id.editSend);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        buttonReturn = (Button) findViewById(R.id.buttonReturn);
        buttonReturn.setOnClickListener(this);
        btnOpen = (Button) findViewById(R.id.btnOpen);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnOpen.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

        textStatus = (TextView) findViewById(R.id.textStatus);

        btnGo = (Button) findViewById(R.id.button1);
        btnGo.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            btnOpen.setText("Close Bluetooth");
        }

        if (Build.VERSION.SDK_INT >= 23) {
            boolean isLocationEnabled = isLocationOpen(getApplicationContext());
            Toast.makeText(getApplicationContext(), "isLocationEnabled: " + isLocationEnabled, Toast.LENGTH_SHORT).show();

            if (!isLocationEnabled) {
                Intent enableLocation = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(enableLocation, 1);
            }
        }

        receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String str = device.getName() + "|" + device.getAddress();
                    if (listDevices.indexOf(str) == -1)
                        listDevices.add(str);
                    if (deviceAdapter != null) {
                        deviceAdapter.notifyDataSetChanged();
                    }
                }
            }
        };
    }

    private static boolean isLocationOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProviderEnabled || isNetworkProviderEnabled;
    }

    @Override
    protected void onDestroy() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(receiver);
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOpen:
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                    Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    enable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(enable);
                    btnOpen.setText("Close Bluetooth");
                } else {
                    bluetoothAdapter.disable();
                    btnOpen.setText("");
                    if (mSocket != null) {
                        try {
                            mSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case R.id.btnSearch:
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(getApplicationContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
                } else {
                    listView.setVisibility(View.VISIBLE);
                    if (listDevices != null) {
                        listDevices.clear();
                        if (deviceAdapter != null) {
                            deviceAdapter.notifyDataSetChanged();
                        }
                    }
                    bluetoothAdapter.startDiscovery();
                    Toast.makeText(getApplicationContext(), "Start discovery", Toast.LENGTH_SHORT).show();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(receiver, filter);
                }
                break;
            case R.id.btnSend:
                if (state == CONNECTED && mThread != null) {
                    String info = editSend.getText().toString();
                    textStatus.setText("Send: " + info);
                    mThread.write(info.getBytes());
                }
                break;
            case R.id.button1:
                Intent intent = new Intent(this, MainActivity2.class);
                intent.putExtra("macAddress", macAddress);
                startActivityForResult(intent, 1);
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonReturn:
                Intent intent1 = new Intent(BluetoothActivity.this, loginactivity.class);
                startActivity(intent1);
                break;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            bluetoothAdapter.cancelDiscovery();
            String str = listDevices.get(position);
            macAddress = str.split("\\|")[1];
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
            try {
                Method clientMethod = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                mSocket = (BluetoothSocket) clientMethod.invoke(device, 1);
                try {
                    mSocket.connect();
                    if (mSocket.isConnected()) {
                        textStatus.setText("Bluetooth is opened");
                        Toast.makeText(getApplicationContext(), "Bluetooth connected successfully", Toast.LENGTH_SHORT).show();
                        listView.setVisibility(View.GONE);
                        textData.setVisibility(View.VISIBLE);
                        mThread = new ComThread(mSocket);
                        mThread.start();
                        state = CONNECTED;
                    } else {
                        textStatus.setText("Bluetooth is closed");
                        Toast.makeText(getApplicationContext(), "Failed to connect to Bluetooth", Toast.LENGTH_SHORT).show();
                        mSocket.close();
                        listView.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
 

    public static final int MESSAGE_READ = 0;
    public static final int DEBUG = 1;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    textData.setText("Receive: " + (String) msg.obj + "\n");
                    break;
                case DEBUG:
                    textStatus.setText((String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    class ComThread extends Thread {

        private BluetoothSocket socket;
        private boolean exitFlag = false;

        public ComThread(BluetoothSocket socket) {
            this.socket = socket;
        }

        public synchronized boolean getFlag() {
            return exitFlag;
        }

        public synchronized void setFlag(boolean value) {
            exitFlag = value;
        }

        private InputStream inputStream;
        private OutputStream outputStream;

        public void write(byte[] bytes) {
            try {
                outputStream = socket.getOutputStream();
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                inputStream = socket.getInputStream();
                int len = 0;
                String result = "";
                exitFlag = false;
                while (len != -1) {
                    if (inputStream.available() <= 0) {
                        Thread.sleep(1000);
                        continue;
                    } else {
                        try {
                            Thread.sleep(500);
                            byte[] data = new byte[1024];
                            len = inputStream.read(data);
                            result = URLDecoder.decode(new String(data, "utf-8"));

                            final String message = result;
                            textData.post(new Runnable() {
                                public void run() {
                                    textData.setText("Receive: " + message + "\n");
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                inputStream.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}