package edu.sjtu.dean.carcontroler;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;
import android.view.View.OnClickListener;

/**
 * Created by dean on 12/12/2016.
 * update by LinLin on 18/12/2016
 */

//add an implements
public class VideoPlayingActivity extends AppCompatActivity  implements OnClickListener{

    private VideoView vv;
    private ProgressDialog progressDialog;

    //add 4 buttons
    private Button btn_up, btn_down, btn_right, btn_left;
    private VideoPlayingActivity vpa = new VideoPlayingActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.videoplay_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playing);
        setSupportActionBar(toolbar);
        vv = (VideoView) findViewById(R.id.videoView);


        //set Listener for button
        vpa = this;
        //init_btn();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (vv.isPlaying())
                    vv.stopPlayback();
                else {
                    progressDialog =
                            ProgressDialog.show(VideoPlayingActivity.this, "", "Buffering video...", true);
                    progressDialog.setCancelable(true);
                    startRtsp();
                }

            }
        });
    }

    public  void startRtsp() {
        // group owner's address
        String vUrl = "rtsp://192.168.49.1:7878/?h264=1500-20-1280-720&videoapi=mc";

        try {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            Uri video = Uri.parse(vUrl);
            vv.setVideoURI(video);
            vv.requestFocus();
            vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    progressDialog.dismiss();
                    vv.start();
                }
            });
        } catch (Exception e) {
            Log.e("VideoView", e.toString());
            progressDialog.dismiss();
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_retry_play) {
            return true;
        } else if (id == R.id.action_example) {
            vv.stopPlayback();
            Intent serviceIntent = new Intent(this, MsgTransferService.class);
            serviceIntent.setAction(MsgTransferService.ACTION_SEND_MESSAGE);
            serviceIntent.putExtra(MsgTransferService.EXTRAS_MESSAGE, "What you want the sever know");
            startService(serviceIntent);

            Intent intent = new Intent(this, ExampleActivity.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.gravity_control)
        {
            vv.stopPlayback();

            Intent intent = new Intent(this, SensorActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.action_facedetect) {
            vv.stopPlayback();
            Intent intent = new Intent(this, FaceDetectActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    //function belows are added by LinLin
    //Create 4 buttons to control car
    private void init_btn()
    {
        //add buttons in view
        btn_up = (Button)findViewById(R.id.create_btn_up);
        btn_up.setOnClickListener(this);

        btn_up = (Button)findViewById(R.id.create_btn_down);
        btn_up.setOnClickListener(this);

        btn_up = (Button)findViewById(R.id.create_btn_right);
        btn_up.setOnClickListener(this);

        btn_up = (Button)findViewById(R.id.create_btn_left);
        btn_up.setOnClickListener(this);
    }

    @Override
    //Please add sending message by bluetooth in every case
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.create_btn_up:
            {
                Log.i("1","fuck up");
                MainActivity.sendInstruction("00");
                break;
            }

            case R.id.create_btn_down:
            {
                Log.i("1", "fuck down");
                break;
            }

            case R.id.create_btn_right:
            {
                Log.i("1", "fuck right");
                break;
            }

            case R.id.create_btn_left:
            {
                Log.i("1", "fuck left");
                break;
            }

        }
    }
}

