package com.example.one.soundrecorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.carlos.voiceline.mylibrary.VoiceLineView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements Runnable {
    private Boolean recording = false;  //用于判断是否正在录音
    private MediaRecorder mediaRecorder = null;
    private Button startBtn;    //开始录音按钮
    private Button stopBtn;     //停止录音按钮
    private Button historyBtn;  //录音列表按钮
    private int timeCount = 0;  //录音计时变量
    private TextView timeTextView;  //录音计时显示
    private TextView recordTextView;    //录音状态显示
    private TextView stopRecordTextView;    //完成录音状态显示
    private VoiceLineView voiceLineView;    //音波波浪线

    private File soundFile = null;  //录音文件

    private Thread thread;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mediaRecorder == null) return;

            double ratio = (double) mediaRecorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            //只要有一个线程，不断调用这个方法，就可以使波形变化
            //主要，这个方法必须在ui线程中调用
            voiceLineView.setVolume((int) (db*2));
        }
    };

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //录音文件创建时间的时间格式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtils.isGrantExternalRW(this, 1);

        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setEnabled(false);
        timeTextView = findViewById(R.id.timeTextView);
        recordTextView = findViewById(R.id.recordTextView);
        stopRecordTextView = findViewById(R.id.stopRecordTextView);

        voiceLineView = findViewById(R.id.voicLine);

        historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                startActivity(intent);
                //点击前往录音列表页面后，录音历史记录列表按钮样式复位
                historyBtn.setBackgroundResource(R.drawable.history2);
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            timeCount++;
            //刷新timeTextView视图显示计时
            String timeString = showTimeCount(timeCount);
            timeTextView.setText(timeString);
            //每1秒刷新一次
            handler.postDelayed(this, 1000);
        }
    };

    //录音按钮被按下
    @SuppressLint("NewApi")
    public void startBtnOnClick(View v) {
        if (!recording) {
            startRecord();
            recording = true;
            stopBtn.setEnabled(true);
            stopBtn.setBackgroundResource(R.drawable.stop);
            stopRecordTextView.setText("完成");
            startBtn.setBackgroundResource(R.drawable.pause);
            recordTextView.setText("暂停");
            runnable.run();
            //录音状态禁止打开历史文件列表
            historyBtn.setEnabled(false);
            historyBtn.setBackgroundResource(R.drawable.history3);
        } else {
            mediaRecorder.pause();
            handler.removeCallbacks(runnable);
            startBtn.setBackgroundResource(R.drawable.start);
            recordTextView.setText("录音");
            recording = false;
            Log.i("n","暂停录音。");
        }
    }

    //将秒数格式化成时分秒格式
    public String showTimeCount(int time) {
        int second = time % 60; //格式化的秒，而time是原始的秒
        if (time < 60) {
            return "00:00:" + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second));
        } else {
            int original_minute = time / 60;  //原始的分
            int minute = original_minute % 60;  //格式化的分
            if (original_minute < 60) {
                return "00:" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + ":" + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second));
            } else {
                int original_hour = time / 3600;    //原始的时，上升到天数是时才会用到
                int hour = original_hour % 24;  //格式化的时
                return (hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour)) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + ":" + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second));
            }
        }
    }

    //开始录制
    private void startRecord() {
        if (mediaRecorder == null) {
            // 存放录音文件的文件夹sounds
            File soundsFolder = new File(Environment.getExternalStorageDirectory(), "sounds");
            if (!soundsFolder.exists()) {
                soundsFolder.mkdirs();
            }
            // 获取当前时间作为文件名创建一个以.amr为后缀的录音文件
            soundFile = new File(soundsFolder, "temp_record.amr");
            if (!soundFile.exists()) {
                try {
                    soundFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  //音频输入源
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);   //设置输出格式
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);   //设置编码格式
            mediaRecorder.setOutputFile(soundFile.getAbsolutePath());
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();  //开始录制
                thread = new Thread(this);
                thread.start();
                Log.i("0","开始录音。");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
//            try {
//                mediaRecorder.prepare();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            Log.i("m","继续录音。");
            mediaRecorder.start();  //开始录制
        }
    }

    //停止录制，资源释放
    @SuppressLint("NewApi")
    public void stopRecord(View v) {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            handler.removeCallbacks(runnable);

            // 重命名
            FileUtils.renameDialog(this, soundFile.getAbsolutePath(), String.valueOf(dateFormat.format(new Date())));

            timeCount = 0;
            timeTextView.setText(R.string.initialZero); //时间显示组件复位为00:00:00
            mediaRecorder.release();
            mediaRecorder = null;
            //按钮样式变动
            startBtn.setBackgroundResource(R.drawable.prepare);
            recordTextView.setText("录音");
            stopBtn.setBackgroundResource(R.drawable.stop2);
            stopBtn.setEnabled(false);  //录音已经停止，使停止按钮不能点击
            historyBtn.setBackgroundResource(R.drawable.history);   //完成录音，录音历史列表里有新录音，使历史列表按钮变色
            historyBtn.setEnabled(true);    //录音已经停止，可以查看历史录音信息了
            recording = false;
            Log.i("end","结束录音。");
        }
    }

    public void aboutBtnOnClick(View v){
        Intent intent = new Intent(MainActivity.this,AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public void run() {
        while (true) {
            if(recording){
                handler.sendEmptyMessage(0);
            }
            try {
                thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
