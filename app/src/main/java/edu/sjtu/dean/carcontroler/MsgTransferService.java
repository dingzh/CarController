package edu.sjtu.dean.carcontroler;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by dean on 12/12/2016.
 */

public class MsgTransferService extends IntentService {
    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_MESSAGE = "com.example.dean.oncarwifi.SEND_MESSAGE";
    public static final String EXTRAS_MESSAGE= "message";
    final private String host = "192.168.49.1";
    final private int port = 9988;

    public MsgTransferService(String name) {
        super(name);
    }

    public MsgTransferService() {
        super("MegTransferService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.getAction().equals(ACTION_SEND_MESSAGE)) {

            String msg = intent.getExtras().getString(EXTRAS_MESSAGE);
            Socket socket = new Socket();

            try {
                Log.e(MainActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(MainActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(msg);
                oos.close();
                os.close();
                socket.close();
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
