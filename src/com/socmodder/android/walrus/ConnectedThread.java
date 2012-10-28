package com.socmodder.android.walrus;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: socmodder
 * Date: 7/30/12
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;


    public ConnectedThread(BluetoothSocket socket){
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try{
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        }   catch(IOException e){}

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public BluetoothSocket getSocket(){
        return mmSocket;
    }

    public void run(){
        byte[] buffer = new byte[1024]; // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the input stream until an exception occurs
        while(true){
            try{
                //read from the iStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
                //TODO: add mHandler
            }   catch(IOException e){
                break;
            }
        }
    }

    public void write(byte[] bytes){
        try{
            mmOutStream.write(bytes);
        }   catch(IOException e){}
    }

    public void cancel(){
        try{
            mmSocket.close();
        }   catch(IOException e){}
    }
}
