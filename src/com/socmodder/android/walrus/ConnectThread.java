package com.socmodder.android.walrus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private ConnectedThread connectedThread = null;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread(BluetoothDevice device){
        BluetoothSocket tmp = null;
        mmDevice = device;

        try{
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        }   catch(IOException e) {}
        mmSocket = tmp;
    }

    public BluetoothSocket getSocket(){
        return mmSocket;
    }

    public void run(){
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        bluetooth.cancelDiscovery();

        try{
            mmSocket.connect();

        }   catch(IOException connectException){
            try{
                mmSocket.close();
            }   catch (IOException closeException){}
            return;
        }
        //connected(mmSocket);
    }

    public void cancel(){
        try{
            mmSocket.close();
        }   catch(IOException e){}
    }

    private void connected(BluetoothSocket socket){
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public ConnectedThread getConnectedThread(){
        return connectedThread;
    }
}
