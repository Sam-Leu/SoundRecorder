package com.example.one.soundrecorder;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

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
    private Date createTime = null; //录音停止时为录音创建时间
    private AlertDialog.Builder dialogBuilder;  //显示录音结束时重命名的弹窗

    private Thread thread;

    @SuppressLint("HandlerLeak")
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaRecorder==null) return;
            double ratio = (double) mediaRecorder.getMaxAmplitude() / 100;
            double db = 0;// 分贝
            //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
            //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
            //同时，也可以配置灵敏度sensibility
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            //只要有一个线程，不断调用这个方法，就可以使波形变化
            //主要，这个方法必须在ui线程中调用
            voiceLineView.setVolume((int) (db));
        }
    };

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //录音文件创建时间的时间格式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtils.isGrantExternalRW(this,1);

        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setEnabled(false);
        timeTextView = findViewById(R.id.timeTextView);
        recordTextView = findViewById(R.id.recordTextView);
        stopRecordTextView = findViewById(R.id.stopRecordTextView);

        voiceLineView = findViewById(R.id.voicLine);

        dbHelper = new DatabaseHelper(this,1);

        historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RecordActivity.class);
                startActivity(intent);
                //点击前往录音列表页面后，录音历史记录列表按钮样式复位
                historyBtn.setBackgroundResource(R.drawable.history2);
            }
        });
    }

    //录音文件数据录入数据库
    public void insert(int timeCount, String fileName, String filePath, int fileLength){
        database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("createtime",dateFormat.format(createTime));
        values.put("duration",showTimeCount(timeCount));
        values.put("filename",fileName.substring(0, fileName.lastIndexOf(".amr")));
        values.put("filepath",filePath);
        values.put("size",fileLength);  //存的单位是Byte
        database.insert("recorder_info", null, values);
    }

    Runnable runnable=new Runnable(){
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
    public void startBtnOnClick(View v){
        if(!recording){
            startRecord();
            recording = true;
            stopBtn.setEnabled(true);
            stopBtn.setBackgroundResource(R.drawable.stop);
            stopRecordTextView.setText("完成");
            startBtn.setBackgroundResource(R.drawable.pause);
            recordTextView.setText("暂停");
            thread = new Thread(this);
            thread.start();
            runnable.run();
        }else{
            mediaRecorder.pause();
            handler.removeCallbacks(runnable);
            startBtn.setBackgroundResource(R.drawable.start);
            recordTextView.setText("录音");
            recording = false;
            thread.interrupt();
        }
    }

    //将秒数格式化成时分秒格式
    public String showTimeCount(int time) {
        int second = time % 60; //格式化的秒，而time是原始的秒
        if(time < 60){
            return "00:00:" + (second<10 ? "0"+String.valueOf(second) : String.valueOf(second));
        }else{
            int original_minute = time / 60;  //原始的分
            int minute = original_minute % 60;  //格式化的分
            if(original_minute < 60){
                return "00:" + (minute<10 ? "0"+String.valueOf(minute) : String.valueOf(minute)) + ":" +(second<10 ? "0"+String.valueOf(second) : String.valueOf(second));
            }else{
                int original_hour = time / 3600;    //原始的时，上升到天数是时才会用到
                int hour = original_hour % 24;  //格式化的时
                return (hour<10 ? "0" + String.valueOf(hour) : String.valueOf(hour)) + ":" + (minute<10 ? "0"+String.valueOf(minute) : String.valueOf(minute)) + ":" +(second<10 ? "0"+String.valueOf(second) : String.valueOf(second));
            }
        }
    }

    //开始录制
    private void startRecord(){
        if(mediaRecorder == null){
            // 存放录音文件的文件夹sounds
            File soundsFolder = new File(Environment.getExternalStorageDirectory(), "sounds");
            if(!soundsFolder.exists()){
                soundsFolder.mkdirs();
            }
            // 获取当前时间作为文件名创建一个以.amr为后缀的录音文件
            soundFile = new File(soundsFolder,"temp_record.amr");
            if(!soundFile.exists()){
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //停止录制，资源释放
    @SuppressLint("NewApi")
    public void stopRecord(View v){
        if(mediaRecorder != null){
            mediaRecorder.stop();
            handler.removeCallbacks(runnable);

            //录音文件重命名弹窗
            dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setTitle("为该录音起个名字");
            @SuppressLint("SimpleDateFormat")
            final EditText setNameText = new EditText(MainActivity.this);
            createTime = new Date();
            setNameText.setText(String.valueOf(dateFormat.format(createTime)));
            dialogBuilder.setView(setNameText);
            dialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    soundFile = reName(soundFile, String.valueOf(setNameText.getText()));
                    if(soundFile != null){
                        insert(timeCount, soundFile.getName(), soundFile.getAbsolutePath(), (int) soundFile.length());
                        timeCount = 0;
                    }else{
                        System.out.println("应该是名字重复了。");
                    }
                }
            } );
            dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    soundFile = reName(soundFile, String.valueOf(setNameText.getText()));
                    if(soundFile != null){
                        insert(timeCount, soundFile.getName(), soundFile.getAbsolutePath(), (int) soundFile.length());
                        timeCount = 0;
                    }else{
                        System.out.println("应该是名字重复了。");
                    }
                }
            });
            dialogBuilder.show();

            timeTextView.setText(R.string.initialZero); //时间显示组件复位为00:00:00
            mediaRecorder.release();
            mediaRecorder = null;
            //按钮样式变动
            startBtn.setBackgroundResource(R.drawable.prepare);
            recordTextView.setText("录音");
            stopBtn.setBackgroundResource(R.drawable.stop2);
            stopBtn.setEnabled(false);  //录音已经停止，使停止按钮不能点击
            historyBtn.setBackgroundResource(R.drawable.history);   //完成录音，录音历史列表里有新录音，使历史列表按钮变色
            recording = false;
            thread.interrupt();
        }
    }

    /**
     * @param f 需要修改名称的文件
     * @param newFileName 新名称
     * @return
     */
    private File reName(File f, String newFileName) {
        String filePath = f.getAbsolutePath();
        if (!f.exists()) { // 判断原文件是否存在
            System.out.println("原文件不存在。");
            return null;
        }
        newFileName = newFileName.trim();
        if ("".equals(newFileName)){ // 文件名不能为空
            System.out.println("新文件名为空");
            return null;
        }
        String newFilePath;
        if (f.isDirectory()) { // 判断是否为文件夹
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/")) + "/" + newFileName;
        } else {
            newFilePath = filePath.substring(0, filePath.lastIndexOf("/"))+ "/"  + newFileName + filePath.substring(filePath.lastIndexOf("."));
        }
        File newFile = new File(newFilePath);
        if (!f.exists()) { // 判断需要修改为的文件是否存在（防止文件名冲突）
            System.out.println("新文件创建不成功。");
            return null;
        }else{
            System.out.println("新文件："+newFile.getAbsolutePath());
        }
        try {
            f.renameTo(newFile); // 修改文件名
            return newFile;
        } catch(Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public void aboutBtnOnClick(View v){
        Intent intent = new Intent(MainActivity.this,AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public void run() {
        while (recording) {
            handler.sendEmptyMessage(0);
            try {
                thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
