package com.example.one.soundrecorder;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    private Button play;
    private ProgressBar progressBar;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String filePath;
    private TextView titleTextView;
    private TextView playTextView;
    private TextView durationTextView;
    private TextView processTextView;
    private Button btn_play;

    private float duration;

    File file;

    Handler handler = new Handler();
    Runnable changeProgressBarThread = new Runnable() {
        public void run() {
            // 获得歌曲现在播放位置并设置成播放进度条的值
            if (mediaPlayer != null) {
                progressBar.setProgress((int)(mediaPlayer.getCurrentPosition() / duration * 100));
                //更新播放时间
                processTextView.setText(TimeStyleHelper.showTimeCount(mediaPlayer.getCurrentPosition()/1000));
                if(mediaPlayer.getCurrentPosition() >= duration){
                    btn_play.setBackgroundResource(R.drawable.prepare);
                    playTextView.setText("播放");
                }
                // 每10毫秒刷新一次
                handler.postDelayed(changeProgressBarThread, 10);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        btn_play = findViewById(R.id.btn_play);
        titleTextView = findViewById(R.id.tv_fileName);
        playTextView = findViewById(R.id.tv_play);
        processTextView = findViewById(R.id.processTextView);
        durationTextView = findViewById(R.id.durationTextVIew);

        Intent intent = getIntent();
        String fileName = intent.getStringExtra("fileName");
        Log.d("4test", "is"+fileName);

        titleTextView.setText(fileName);

        filePath = "/sounds/"+fileName+".amr";

        play = (Button)findViewById(R.id.btn_play);
        progressBar = (ProgressBar) findViewById(R.id.bar_progress);

        play.setOnClickListener(PlayActivity.this);

        initMediaPlayer(fileName);
    }

    private void initMediaPlayer(String fileName){
        try {
            file = new File(Environment.getExternalStorageDirectory(),filePath);
            Log.d("4test", file.getAbsolutePath());
            android.util.Log.i("文件是否存在？", "file.exists()="+ file.exists());

            //网上说居然要这样用。。我也是醉了。。
            FileInputStream fis = new FileInputStream(new File(file.getAbsolutePath()));
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();  //让Mediaplayer进入到准备状态

            duration = mediaPlayer.getDuration();   //获取录音文件的长度，用于显示进度
            durationTextView.setText(TimeStyleHelper.showTimeCount((int) duration / 1000));

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
                    btn_play.setBackgroundResource(R.drawable.start);
                    playTextView.setText("正在播放");
                    changeProgressBarThread.run();
                }else if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    btn_play.setBackgroundResource(R.drawable.pause);
                    playTextView.setText("已暂停");
                    handler.removeCallbacks(changeProgressBarThread);
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
            handler.removeCallbacks(changeProgressBarThread);
        }
    }

    public void shareBtnOnClick(View v){
        //分享按钮点击事件
        if (file.exists() && file.isFile()) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("audio/amr");
            Uri u = Uri.fromFile(file);
            intent.putExtra(Intent.EXTRA_STREAM, u);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(Intent.createChooser(intent, "分享此录音"));
        }
    }
}
