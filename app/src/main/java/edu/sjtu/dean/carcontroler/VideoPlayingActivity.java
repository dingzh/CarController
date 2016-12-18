package edu.sjtu.dean.carcontroler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.VideoView;

/**
 * Created by dean on 12/12/2016.
 */

public class VideoPlayingActivity extends AppCompatActivity {

    private VideoView vv;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoplay_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_playing);
        setSupportActionBar(toolbar);
        vv = (VideoView) findViewById(R.id.videoView);

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
        } else if (id == R.id.action_facedetect) {
            vv.stopPlayback();
            Intent intent = new Intent(this, FaceDetectActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

