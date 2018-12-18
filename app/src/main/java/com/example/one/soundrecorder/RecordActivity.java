package com.example.one.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaMetadataRetriever;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
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

import static android.os.Environment.getExternalStorageDirectory;

public class RecordActivity extends AppCompatActivity implements RecordAdapter.OnShowItemClickListener {

    private DatabaseHelper dbHelper;
    SQLiteDatabase db;

    private List<Record> mData = null;
    private List<Record> selectList = null;
    private Context mContext;
    private RecordAdapter mAdapter = null;
    private ListView data_list;
    private Button btn_rename = null;
    private CheckBox cb_click = null;

    private static boolean isShow;

    private LinearLayout topLayout;
    private LinearLayout bottomLayout;
    private String filePath = "/storage/emulated/0/sounds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);


        dbHelper = new DatabaseHelper(this, 1);

        topLayout = (LinearLayout) findViewById(R.id.toplayout);
        bottomLayout = (LinearLayout) findViewById(R.id.bottomlayout);
        btn_rename = (Button) findViewById(R.id.operate_rename);

        mContext = RecordActivity.this;
        data_list = (ListView) findViewById(R.id.data_list);

        mData = new LinkedList<Record>();
        selectList = new LinkedList<Record>();

        db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + "recorder_info");
        getFileInfo(new File(filePath));
        showRecord();

    }


    //显示记录
    private void showRecord() {

        db = dbHelper.getReadableDatabase();

        String sql = "select * from recorder_info order by createtime ASC";
        Cursor cursor = db.rawQuery(sql, null);

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

        data_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Record recordClick = mData.get(position);
                String clickName = recordClick.getFileName();
                Log.d("4test", clickName);
                Intent intent = new Intent(RecordActivity.this,PlayActivity.class);
                intent.putExtra("fileName",clickName);
                Log.i("发送filename:",clickName);
                startActivity(intent);
            }
        });

        data_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                btn_rename.setEnabled(true);
                if (isShow) {
                    return false;
                } else {
                    isShow = true;
                    for (Record record : mData) {
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
        Animation animation_bottom = AnimationUtils.loadAnimation(this, R.anim.operate_bottom_in);
        bottomLayout.setAnimation(animation_bottom);
        bottomLayout.setVisibility(View.VISIBLE);

        Animation animation_top = AnimationUtils.loadAnimation(this, R.anim.operate_top_in);
        topLayout.setAnimation(animation_top);
        topLayout.setVisibility(View.VISIBLE);

        Button btn_back = (Button) findViewById(R.id.operate_back);
        Button btn_select = (Button) findViewById(R.id.operate_select);
        Button btn_invert_select = (Button) findViewById(R.id.invert_select);
        Button btn_delete = (Button) findViewById(R.id.operate_delete);
        cb_click = (CheckBox)findViewById(R.id.cb_select);


        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow) {
                    selectList.clear();
                    for (Record record : mData) {
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

        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Record record : mData) {
                    if (!record.isChecked()) {
                        record.setChecked(true);
                        if (!selectList.contains(record)) {
                            selectList.add(record);
                        }
                    }

                }

                mAdapter.notifyDataSetChanged();
            }
        });

        btn_invert_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Record record : mData) {
                    if (!record.isChecked()) {
                        record.setChecked(true);
                        if (!selectList.contains(record)) {
                            selectList.add(record);
                        }

                    }
                    else {
                        record.setChecked(false);
                        if (!selectList.contains(record)) {
                            selectList.remove(record);
                        }
                    }

                }

                mAdapter.notifyDataSetChanged();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectList != null && selectList.size() > 0) {

                    AlertDialog.Builder builder=new AlertDialog.Builder(RecordActivity.this);
                    builder.setMessage("确定删除?");
                    builder.setTitle("提示");

                    //添加AlertDialog.Builder对象的setPositiveButton()方法
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                                Log.d("4test", "确定");
                                for (Iterator<Record> dd = selectList.iterator(); dd.hasNext(); ) {
                                    Record str = dd.next();
                                    String strFileName = str.getFileName();
                                    String pathName = "/sounds/" + strFileName + ".amr";
                                    String aa = "/storage/emulated/0/sounds/" + strFileName + ".amr";

                                    deletefile(pathName);
                                }

                                mData.removeAll(selectList);
                                mAdapter.notifyDataSetChanged();
                                selectList.clear();
                        }
                    });

                    //添加AlertDialog.Builder对象的setNegativeButton()方法
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("4test", "取消");
                        }
                    });

                    builder.create().show();


                } else {
                    Toast.makeText(RecordActivity.this, "请选择条目", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btn_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strFileName = null;
                if (selectList.size() != 1){
                    Toast.makeText(RecordActivity.this,"请只选择一个项目",Toast.LENGTH_SHORT).show();
                    Log.d("4test", "rename_click");
                }
                else{
                    for (Iterator<Record> dd = selectList.iterator(); dd.hasNext(); ) {
                        Record str = dd.next();
                        strFileName = str.getFileName();
                    }
                    Toast.makeText(RecordActivity.this,"重命名成功",Toast.LENGTH_SHORT).show();
                    FileUtils.renameDialog(RecordActivity.this, filePath+"/"+strFileName+".amr", strFileName);

                    Log.d("4test", "rename");
                }
            }
        });
    }

    /**
     * 隐藏操作选项
     */
    private void dismissOperate() {
        Animation animation_bottom = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.operate_bottom_out);
        bottomLayout.setAnimation(animation_bottom);
        bottomLayout.setVisibility(View.GONE);

        Animation animation_top = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.operate_top_out);
        topLayout.setAnimation(animation_top);
        topLayout.setVisibility(View.GONE);

    }


    @Override
    public void onShowItemClick(Record record) {
        if (record.isChecked() && !selectList.contains(record)) {
            selectList.add(record);
        } else if (!record.isChecked() && selectList.contains(record)) {
            selectList.remove(record);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (isShow) {
            selectList.clear();
            for (Record record : mData) {
                record.setChecked(false);
                record.setShow(false);
            }
            mAdapter.notifyDataSetChanged();
            isShow = false;
            data_list.setLongClickable(true);
            dismissOperate();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * 将秒数格式化成时分秒格式
     *
     * @param time
     * @return
     */
    public static String showTimeCount(int time) {

        long hours = time / (1000 * 60 * 60);
        long minutes = (time - hours * (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (time - hours * (1000 * 60 * 60) - minutes * (1000 * 60)) / 1000;

        String strHour = "00";
        String strMinute = "00";
        String strSecond = "00";

        if (hours < 10) {
            strHour = "0" + hours;
        } else {
            strHour = "" + hours;
        }

        if (minutes < 10) {
            strMinute = "0" + minutes;

        } else {
            strMinute = "" + minutes;
        }

        if (seconds < 10) {
            strSecond = "0" + seconds;

        } else {
            strSecond = "" + seconds;
        }

        return strHour + ":" + strMinute + ":" + strSecond;

    }

    /**
     * 删除文件事件处理
     *
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


    /**
     * 扫描指定文件夹并入库
     * @param dir
     */
    public void getFileInfo(File dir) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        if (!dir.exists()) {
            Log.d("4test", "目录不存在");
            return;
        }

        if (!dir.isDirectory()) {
            Log.d("4test", "不是一个目录");
            return;
        }

        File files[] = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {

                    String fileName = "";
                    String fileDuration = "00:00:00";
                    String strSize = "0.00KB";
                    String createTime = "0000-00-00 00:00:00";

                    String tempName = String.valueOf(file.getName());
                    fileName = tempName.substring(0, tempName.length() - 4);

                    int mDuration = 0;
                    mDuration = getAmrDuration(file) * 1000;
                    fileDuration = showTimeCount(mDuration);

                    int length = (int) file.length();
                    float kbSize = length / 1000;
                    float mbSize = length / 1000000;

                    if (length < 819200) {
                        strSize = String.valueOf((float) (Math.round(kbSize * 100)) / (100)) + "KB";
                    } else {
                        strSize = String.valueOf((float) (Math.round(mbSize * 100)) / (100)) + "MB";

                    }

                    createTime = String.valueOf(file.lastModified());//new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

                    db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();

                    values.put("createtime", createTime);
                    values.put("duration", fileDuration);
                    values.put("filename", fileName);
                    values.put("filepath", filePath);
                    values.put("size", strSize);
                    db.insert("recorder_info", null, values);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 获取amr文件播放时长
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static int getAmrDuration(File file) throws IOException {
        long duration = -1;
        int[] packedSize = {12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0,
                0, 0};
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
        return (int) ((duration / 1000) + 1) / 3;
    }
}
