package edu.sjtu.dean.carcontroler;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by dean on 12/12/2016.
 */

public class ListDetailFragment extends Fragment implements WifiP2pManager.ConnectionInfoListener {
    private View mContentView = null;
    private WifiP2pDevice device;
    public static WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    public String client = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setClient(String client) {
        this.client = client;
        Log.d(MainActivity.TAG, "set client ip" + client);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                config.groupOwnerIntent = 13;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true);
                ((MainActivityFragment.DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((MainActivityFragment.DeviceActionListener) getActivity()).disconnect();
                    }
                });


        return mContentView;
    }




    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;

        if (info.groupFormed && info.isGroupOwner) {
            Toast.makeText(getActivity(),
                    "I'm the group owner!", Toast.LENGTH_LONG).show();
//            not useful now that the group owner is always stream server
//            new GetClientAsyncTask(getActivity(), getActivity().findViewById(R.id.my_status))
//                    .execute();

            // accept messages at 9988
            new MessageServerAsyncTask(getActivity(), getActivity().findViewById(R.id.my_status))
                    .execute();
            Log.d(MainActivity.TAG, "Intent----------- start stream server.");
            Intent intent = new Intent(getActivity(), VideoStreamingActivity.class);
            startActivity(intent);
            // todo need to connect to bluetooth somehow

        }
        else if (info.groupFormed) {
            Toast.makeText(getActivity(),
                    "I'm a client!", Toast.LENGTH_LONG).show();

            // start playing activity
            Intent intent = new Intent(getActivity(), VideoPlayingActivity.class);
            Bundle mbundle = new Bundle();
            mbundle.putSerializable("cursor",((MainActivity)getActivity()).bAction);
            intent.putExtras(mbundle);
            startActivity(intent);

        } else {
            Toast.makeText(getActivity(),
                    "Where the fuck is the group?", Toast.LENGTH_LONG).show();
        }

        return;
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        this.getView().setVisibility(View.GONE);
    }


    public class MessageServerAsyncTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private TextView statusText;
        public static final String SET_CLIENT_IP = "setClientIP";

        /**
         * @param context
         * @param statusText
         */
        public MessageServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(9988);
                while (true) {
                    // todo not even sure if this is the right way to do it
                    Log.d(MainActivity.TAG, "Server: Socket opened");
                    Socket client = serverSocket.accept();
                    Log.d(MainActivity.TAG, "Server: connection done");
                    ObjectInputStream objectInputStream = new ObjectInputStream(client.getInputStream());
                    Log.d(MainActivity.TAG, "server: copying string");
                    String message = null;
                    try {
                        message = (String) objectInputStream.readObject();
                    } catch (ClassNotFoundException e) {
                        Log.e(MainActivity.TAG, "" + e);
                    }
                    objectInputStream.close();
                    client.close();
                    // todo transfer to BLUETOOTH here
                }
            } catch (IOException e) {
                Log.e(MainActivity.TAG, e.getMessage());
            } finally {
                return null;
            }
        }
    }
}

