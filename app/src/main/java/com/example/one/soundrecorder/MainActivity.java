package com.example.one.soundrecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//实现ActivityCompat.OnRequestPermissionsResultCallback接口，运行时检测权限
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    private Boolean isStart = false;
    private MediaRecorder mediaRecorder = null;
    private Button startBtn;
    private Button stopBtn;
    private int timeCount = 0;
    private TextView timeTextView;
    private Handler handler=new Handler();
    private File soundsFolder = null;
    private File soundFile = null;
    private Date createTime = null;


    //需要进行检测的权限数组
    protected String[] needPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//存储卡写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE,//存储卡读取权限
            Manifest.permission.RECORD_AUDIO,   //录音权限
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,  //允许装载和卸载文件系统权限，这个一定要加，否则手机即使赋予了权限也不会记录下来
            Manifest.permission.READ_PHONE_STATE//读取手机状态权限
    };
    private static final int PERMISSON_REQUESTCODE = 0;
    //判断是否需要检测，防止不停的弹框
    private boolean isNeedCheck = true;

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheck) {
            checkPermissions(needPermissions);
        }
    }
    //检查权限
    private void checkPermissions(String... permissions) { List<String> needRequestPermissonList = findDeniedPermissions(permissions);
        if (null != needRequestPermissonList
                && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    PERMISSON_REQUESTCODE);
        }
    }
    //获取权限集中需要申请权限的列表
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, perm)) {
                needRequestPermissonList.add(perm);
            }
        }
        return needRequestPermissonList;
    }
    //检测是否有的权限都已经授权
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
        }
    }
    //弹出对话框, 提示用户手动授权
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notifyTitle);
        builder.setMessage(R.string.notifyMsg);
        // 拒绝授权 退出应用
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        //同意授权
        builder.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    //启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //以上是动态获取权限的方法**********************************************************************


    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startBtn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setEnabled(false);
        timeTextView = findViewById(R.id.timeTextView);

        dbHelper = new DatabaseHelper(this,1);

        Button historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RecordActivity.class);
                startActivity(intent);
            }
        });
    }

    //录音文件数据录入数据库
    public void insert(int timeCount, String fileName, String filePath, int fileLength){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("createtime",dateFormat.format(createTime));
        values.put("duration",showTimeCount(timeCount));
        values.put("filename",fileName);
        values.put("filepath",filePath);
        values.put("size",fileLength);  //存的单位是Byte
        db.insert("recorder_info", null, values);
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
        if(!isStart){
            startRecord();
            startBtn.setBackgroundResource(R.drawable.start2);
            isStart = true;
            stopBtn.setEnabled(true);
            stopBtn.setBackgroundResource(R.drawable.stop);
            runnable.run();
        }else{
            mediaRecorder.pause();
            handler.removeCallbacks(runnable);
            startBtn.setBackgroundResource(R.drawable.pause2);
            isStart = false;
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
            soundsFolder = new File(Environment.getExternalStorageDirectory(),"sounds");
            if(!soundsFolder.exists()){
                soundsFolder.mkdirs();
            }
            // 获取当前时间作为文件名创建一个以.amr为后缀的录音文件
            soundFile = new File(soundsFolder,dateFormat.format((createTime = new Date()))+".amr");
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

            insert(timeCount, soundFile.getName(), soundFile.getAbsolutePath(), (int) soundFile.length());

            timeCount = 0;
            timeTextView.setText(R.string.initialZero); //时间复位为0

            mediaRecorder.release();
            mediaRecorder = null;
            startBtn.setBackgroundResource(R.drawable.prepare);
            stopBtn.setBackgroundResource(R.drawable.stop2);
            isStart = false;
        }
    }
}
