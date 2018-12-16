package com.example.one.soundrecorder;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    private Button play;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/sounds/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        Log.i("接收filename:",fileName);
        Log.d("4test", "is"+fileName);

        play = (Button)findViewById(R.id.btn_play);
        progressBar = (ProgressBar) findViewById(R.id.bar_progress);

        play.setOnClickListener(PlayActivity.this);

        initMediaPlayer(fileName);
    }

    private void initMediaPlayer(String fileName){
        try {
            File file = new File(Environment.getExternalStorageDirectory(),"/sounds/"+fileName+".amr");
            android.util.Log.i("TAG", "file.exists()="+ file.exists());
            Log.d("4test", file.getAbsolutePath());
//            mediaPlayer.setDataSource(String.valueOf(file));  //指定音频文件的路径

            //网上说居然要这样用。。我也是醉了。。
            FileInputStream fis = new FileInputStream(new File(file.getAbsolutePath()));
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();  //让Mediaplayer进入到准备状态
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_play:
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();    //开始播放
                    Log.d("4test", "Start play");
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
