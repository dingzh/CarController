package edu.sjtu.dean.carcontroler;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by dell on 2016/12/18.
 */

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BluetoothAdapter mmBluetoothAdapter;

    public ConnectThread(BluetoothDevice device,BluetoothAdapter adapter) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        mmBluetoothAdapter = adapter;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        mmBluetoothAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("BT connect error", "" + closeException);
            }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

        //write(MainActivity.lastInstr.getBytes());

    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
    // call it from main activity
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e("BT write error", "" + e);
            run();
        }
    }
}
