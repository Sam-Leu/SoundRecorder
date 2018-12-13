package com.example.one.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static android.os.Environment.getExternalStorageDirectory;

public class RecordActivity extends AppCompatActivity implements RecordAdapter.OnShowItemClickListener{

    private DatabaseHelper dbHelper;
    SQLiteDatabase db;

    private List<Record> mData = null;
    private List<Record> selectList = null;
    private Context mContext;
    private RecordAdapter mAdapter = null;
    private ListView data_list;

    private static boolean isShow;

    private LinearLayout linearLayout;
    private String filePath = "/storage/emulated/0/sounds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        dbHelper = new DatabaseHelper(this,1);
        linearLayout = (LinearLayout)findViewById(R.id.linearlayout);

        mContext = RecordActivity.this;
        data_list = (ListView)findViewById(R.id.data_list);

        mData = new LinkedList<Record>();
        selectList = new LinkedList<Record>();

        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + "recorder_info");
        getFileInfo(new File(filePath));
        showRecord();

    }


    //显示记录
    private void showRecord(){

        db = dbHelper.getReadableDatabase();

        String sql = "select * from recorder_info order by createtime ASC";
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String fileName = cursor.getString(cursor.getColumnIndex("filename"));
                String filePath = cursor.getString(cursor.getColumnIndex("filepath"));
                String tempCreateTime = cursor.getString(cursor.getColumnIndex("createtime"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                String size = cursor.getString(cursor.getColumnIndex("size"));

                String createTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Long.parseLong(tempCreateTime)));
                mData.add(new Record(fileName, createTime, duration, size));

            } while (cursor.moveToNext());
            cursor.close();
        }

        mAdapter = new RecordAdapter((LinkedList<Record>) mData, mContext);
        data_list.setAdapter(mAdapter);
        mAdapter.setOnShowItemClickListener(this);

        data_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(RecordActivity.this,"111",Toast.LENGTH_SHORT).show();
                if(isShow){
                    return false;
                }
                else {
                    isShow = true;
                    for(Record record : mData){
                        record.setShow(true);
                    }
                    mAdapter.notifyDataSetChanged();
                    showOperate();
                    data_list.setLongClickable(false);
                }
                return false;
            }
        });

    }

    private void showOperate() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.operate_in);
        linearLayout.setAnimation(animation);
        linearLayout.setVisibility(View.VISIBLE);

        TextView tv_back = (TextView)findViewById(R.id.operate_back);
        TextView tv_select = (TextView)findViewById(R.id.operate_select);
        TextView tv_invert_select = (TextView)findViewById(R.id.invert_select);
        TextView tv_delete = (TextView)findViewById(R.id.operate_delete);

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShow){
                    selectList.clear();
                    for (Record record:mData){
                        record.setChecked(false);
                        record.setShow(false);
                    }

                    mAdapter.notifyDataSetChanged();
                    isShow = false;
                    data_list.setLongClickable(true);
                    dismissOperate();
                }
            }
        });

        tv_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Record record:mData){
                    if(!record.isChecked()){
                        record.setChecked(true);
                        if(!selectList.contains(record)){
                            selectList.add(record);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        tv_invert_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Record record : mData){
                    if(!record.isChecked()){
                        record.setChecked(true);
                        if (!selectList.contains(record)){
                            selectList.add(record);
                        }
                    }
                    else {
                        record.setChecked(false);
                        if (!selectList.contains(record)){
                            selectList.remove(record);
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectList != null && selectList.size()>0){
                    mData.removeAll(selectList);
                    mAdapter.notifyDataSetChanged();

                    for(Iterator<Record> dd = selectList.iterator(); dd.hasNext();){
                        Record str = dd.next();
                        String strFileName = str.getFileName();
                        String pathName = "/sounds/"+strFileName+".amr";
                        String aa = "/storage/emulated/0/sounds/"+strFileName+".amr";
                        deletefile(pathName);

                    }

                    selectList.clear();
                }
                else {
                    Toast.makeText(RecordActivity.this,"请选择条目",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void dismissOperate() {
        Animation animation = AnimationUtils.loadAnimation(RecordActivity.this,R.anim.operate_out);
        linearLayout.setAnimation(animation);
        linearLayout.setVisibility(View.GONE);

    }


    @Override
    public void onShowItemClick(Record record) {
        if(record.isChecked() && !selectList.contains(record)){
            selectList.add(record);
        }
        else if(!record.isChecked() && selectList.contains(record)){
            selectList.remove(record);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(isShow){
            selectList.clear();
            for(Record record : mData){
                record.setChecked(false);
                record.setShow(false);
            }
            mAdapter.notifyDataSetChanged();
            isShow = false;
            data_list.setLongClickable(true);
            dismissOperate();
        }
        else {
            super.onBackPressed();
        }
    }


    //将秒数格式化成时分秒格式
    public static String showTimeCount(int time) {

        long hours = time / (1000 * 60 * 60);
        long minutes = (time-hours*(1000 * 60 * 60 ))/(1000* 60);
        long seconds = (time-hours*(1000 * 60 * 60 )-minutes*(1000* 60))/1000;

        String strHour = "00";
        String strMinute = "00";
        String strSecond = "00";

        if(hours < 10){
            strHour = "0"+hours;
        }
        else {
            strHour = ""+hours;
        }

        if(minutes<10){
            strMinute = "0"+minutes;

        }
        else{
            strMinute = ""+minutes;
        }

        if (seconds<10){
            strSecond = "0"+seconds;

        }
        else {
            strSecond = ""+seconds;
        }

        return strHour+":"+strMinute+":"+strSecond;

    }

    /**
     * 删除文件事件处理
     * @param fileName
     */
    public static void deletefile(String fileName) {
        try {
            // 找到文件所在的路径并删除该文件
            File file = new File(getExternalStorageDirectory(), fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getFileInfo(File dir) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        if(!dir.exists()){
            Log.d("4test","目录不存在");
            return;
        }

        if(!dir.isDirectory()){
            Log.d("4test","不是一个目录");
            return;
        }

        File files[] = dir.listFiles();
        if(files != null && files.length > 0){
            for(File file : files){
                try {

                    String fileName = "";
                    String fileDuration = "00:00:00";
                    String strSize = "0.00KB";
                    String createTime = "0000-00-00 00:00:00";

                    String tempName = String.valueOf(file.getName());
                    fileName = tempName.substring(0,tempName.length()-4);

                    int mDuration = 0;
                    mDuration = getAmrDuration(file)*1000;
                    fileDuration = showTimeCount(mDuration);

                    int length = (int)file.length();
                    float kbSize = length/1000;
                    float mbSize = length/1000000;

                    if(length < 819200){
                        strSize = String.valueOf((float)(Math.round(kbSize*100))/(100)) + "KB";
                    }
                    else{
                        strSize = String.valueOf((float)(Math.round(mbSize*100))/(100)) + "MB";

                    }

                    createTime = String.valueOf(file.lastModified());//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                    db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();

                    values.put("createtime",createTime);
                    values.put("duration",fileDuration);
                    values.put("filename",fileName);
                    values.put("filepath",filePath);
                    values.put("size",strSize);
                    db.insert("recorder_info", null, values);


                }
                catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
    }


    public static int getAmrDuration(File file) throws IOException {
        long duration = -1;
        int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0,
                0, 0 };
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // 文件的长度
            long length = file.length();
            // 设置初始位置
            int pos = 6;
            // 初始帧数
            int frameCount = 0;
            int packedPos = -1;
            // 初始数据值
            byte[] datas = new byte[1];
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }
            // 帧数*20
            duration += frameCount * 20;
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return (int)((duration/1000)+1)/3;
    }

}
