package edu.sjtu.dean.carcontroler;

/**
 * Created by dell on 2016/12/18.
 */
import android.bluetooth.*;
import android.util.Log;

import java.io.Serializable;
import java.util.*;
import java.lang.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import edu.sjtu.dean.carcontroler.ConnectThread;

public class bluetoothActivity {

    public static BluetoothAdapter mBluetoothAdapter;
    public ConnectThread threadForConnection;
    public String string = "1";

    bluetoothActivity(){}

    public void getSocket(BluetoothDevice device,BluetoothAdapter adapter) {
        mBluetoothAdapter = adapter;
        // run the thread
        threadForConnection = new ConnectThread(device,adapter);
        Log.i("1", "after run");
        threadForConnection.run();
        //threadForConnection.write(string.getBytes());
        Log.i("i", "after write");
    }
}
