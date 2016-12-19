package edu.sjtu.dean.carcontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.*;

import java.io.Serializable;
import java.util.*;
import java.lang.*;
import edu.sjtu.dean.carcontroler.MainActivityFragment.DeviceActionListener;



public class MainActivity extends AppCompatActivity
        implements WifiP2pManager.ChannelListener, DeviceActionListener{

    public static final String TAG = "wifidirectdemo";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean isWifiP2pConnected = false;
    private boolean retryChannel = false;


    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    private BluetoothDevice mdevice;
    public final bluetoothActivity bAction = new bluetoothActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isWifiP2pEnabled) {
                    Snackbar.make(view, R.string.p2p_off_warning, Snackbar.LENGTH_LONG)
                            .setAction("Setting", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                                }
                            }).show();
                    return;
                }
                if (!isWifiP2pConnected) {
                    final MainActivityFragment fragment = (MainActivityFragment) getFragmentManager()
                            .findFragmentById(R.id.frag_list);
                    fragment.onInitiateDiscovery();
                    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Snackbar.make(MainActivity.this.findViewById(R.id.fab), "Discovery Initiated", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Snackbar.make(MainActivity.this.findViewById(R.id.fab),
                                    "Discovery Failed : " + reasonCode, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                }
                else {
                    disconnect();
                }

            }
        });
        //bluetooth
        FloatingActionButton bl = (FloatingActionButton) findViewById(R.id.bl);
        bl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if (mBluetoothAdapter == null){
                    Snackbar.make(MainActivity.this.findViewById(R.id.bl),
                            "bluetooth service not supported" , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                if (!mBluetoothAdapter.isEnabled()){
                    mBluetoothAdapter.enable();
                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0){
                    for (BluetoothDevice device:pairedDevices){
                        if (device.getName().equals("HC-06")){
                            mdevice = device;
                            Snackbar.make(MainActivity.this.findViewById(R.id.bl),
                                    "bluetooth connected successfully" , Snackbar.LENGTH_LONG)
                                    .setAction("Action", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            bAction.getSocket(mdevice,mBluetoothAdapter);
                                        }
                                    }).show();
                        }
                    }
                }
                else{
                    Snackbar.make(MainActivity.this.findViewById(R.id.bl),
                            "no devices found" , Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public void setIsWifiP2pConnected(boolean isWifiP2pConnected) {
        this.isWifiP2pConnected = isWifiP2pConnected;
    }

    public void resetData() {
        MainActivityFragment fragmentList = (MainActivityFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);

        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        ListDetailFragment fragment = (ListDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
//                Snackbar.make(MainActivity.this.findViewById(R.id.fab),
//                        "Connect success.", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
            }

            @Override
            public void onFailure(int reason) {
//                Snackbar.make(MainActivity.this.findViewById(R.id.fab),
//                        "Connect failed. Retry.", Snackbar.LENGTH_SHORT)
//                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                final MainActivityFragment fragment = (MainActivityFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                //TextView view = (TextView) fragment.getView().findViewById(R.id.frag_title);
                //view.setText(R.string.noconnection);
                Log.d(TAG, "Disconnect success.");
            }

        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Snackbar.make(MainActivity.this.findViewById(R.id.fab),
                    "Channel lost. Trying again", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Snackbar.make(MainActivity.this.findViewById(R.id.fab),
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}