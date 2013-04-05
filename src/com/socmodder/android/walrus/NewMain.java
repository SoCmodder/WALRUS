package com.socmodder.android.walrus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: SoCmodder
 * Date: 2/12/13
 * Time: 1:50 PM
 */
public class NewMain extends Activity {
    Button lock, unlock;
    Handler h;

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    //Might not need this
    private StringBuilder sb = new StringBuilder();

    private ConnectedThread mConnectedThread;

    private byte message[] = new byte[1];

    //SPP UUID Service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //MAC Address of the Bluetooth Module (You'll need to modify this for your device)
    public static String ardAddress = "00:12:04:05:94:18";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        lock = (Button) findViewById(R.id.lock_button);
        unlock = (Button)findViewById(R.id.unlock_button);

        btAdapter = BluetoothAdapter.getDefaultAdapter();   //get the bluetooth adapter
        turnOnBt();

        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message[0] = 0;
                mConnectedThread.write(message);
            }
        });

        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message[0] = 1;
                mConnectedThread.write(message);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();

        BluetoothDevice device = btAdapter.getRemoteDevice(ardAddress);

        try{
            btSocket = createBluetoothSocket(device);
        }catch(IOException e){
            e.printStackTrace();
        }

        btAdapter.cancelDiscovery();

        try{
            btSocket.connect();
        }catch(IOException e){
            try{
                btSocket.close();
            }catch(IOException e2){}
        }

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onPause(){
        super.onPause();

        try{
            btSocket.close();
        }catch(IOException e){}
    }

    public void turnOnBt(){
        if(btAdapter != null){
            //continue with bluetooth setup
            if(!btAdapter.isEnabled()){
                btAdapter.enable();
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {}
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }
}