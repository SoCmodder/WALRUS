package com.socmodder.android.walrus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.util.Set;

public class Main extends Activity {


    //Message types sent from the BluetoothChatService
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    //You need to edit this for your particular devices, yo
    public static String ardAddress = "00:12:04:05:94:18";

    //Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    //Intent Requstion Codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter bluetooth = null;
    private ConnectThread connectThread = null;
    private ConnectedThread connectedThread = null;

    private byte locker[] = new byte[1];

    ImageView status;
    TextView tStatus;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

       // Button findDevices = (Button)findViewById(R.id.find_devices_button);
        //findDevices.setVisibility(View.INVISIBLE);
        Button UnlockButton = (Button)findViewById(R.id.unlock_button);
        Button LockButton = (Button)findViewById(R.id.lock_button);
        //ListView DeviceListview = (ListView)findViewById(R.id.device_listview);
        status = (ImageView)findViewById(R.id.status_image);
        tStatus = (TextView)findViewById(R.id.status_textview);
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);

        bluetooth = BluetoothAdapter.getDefaultAdapter();

        if(bluetooth != null){
            //continue with bluetooth setup
            if(!bluetooth.isEnabled()){
                bluetooth.enable();
            }
            while(!bluetooth.isEnabled()){}
            queryPairedDevices();
        }


        UnlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locker[0] = 1;
                connectedThread.write(locker);
            }
        });

        LockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locker[0] = 0;
                connectedThread.write(locker);
            }
        });

        /*DeviceListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
                bluetooth.cancelDiscovery();

                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectDevice(address);
            }
        });   */
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                Toast.makeText(getApplicationContext(), "Device Found", Toast.LENGTH_SHORT).show();
                queryPairedDevices();
            }
            else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                //Device Connected
                status.setImageResource(R.drawable.green_dot);
                tStatus.setText("Online");
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(getApplicationContext(), "Discovery Finished", Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)){
                Toast.makeText(getApplicationContext(), "Disconnect Requested", Toast.LENGTH_SHORT).show();
            }
            else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
               //Device Disconnected
                status.setImageResource(R.drawable.red_dot);
                tStatus.setText("Offline");
            }
        }
    };

    private void queryPairedDevices(){
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        //ListView DeviceListview = (ListView)findViewById(R.id.device_listview);

        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        else{
            final BroadcastReceiver mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //To change body of implemented methods use File | Settings | File Templates.
                    String action = intent.getAction();
                    //When discovery finds a device
                    if(BluetoothDevice.ACTION_FOUND.equals(action)){
                        //Get the bluetooth object from the intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        //Add the name and address to an array adapter to show in a listview
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                }
            };
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter); //Don't forget to unregister during onDestroy
        }
       // DeviceListview.setAdapter(mArrayAdapter);
    }

    private void connectDevice(String address){
        BluetoothDevice device = bluetooth.getRemoteDevice(address);
        Toast.makeText(getApplicationContext(), address, Toast.LENGTH_LONG).show();
        connectThread = new ConnectThread(device);
        connectThread.start();
        connectedThread =  new ConnectedThread(connectThread.getSocket());
        connectedThread.start();
    }

    @Override
    public void onBackPressed(){
        if(connectedThread != null){
            connectedThread.cancel();
        }
        BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
        if(bta.isEnabled()){
            bta.disable();
        }
        finish();
    }
}