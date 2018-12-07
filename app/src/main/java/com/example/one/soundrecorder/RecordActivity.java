package com.example.one.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class RecordActivity extends AppCompatActivity {

    private Vector<String> entries;

    private DatabaseHelper dbHelper;
    SQLiteDatabase db;

    private List<Record> mData = null;
    private List<Record> selectList = null;
    private Context mContext;
    private RecordAdapter mAdapter = null;
    private ListView data_list;

    private static boolean isShow;

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        dbHelper = new DatabaseHelper(this,1);

        mContext = RecordActivity.this;
        data_list = (ListView)findViewById(R.id.data_list);

        mData = new LinkedList<Record>();
        selectList = new LinkedList<Record>();

        showRecord();


    }

    //显示记录
    private void showRecord(){

        db = dbHelper.getReadableDatabase();

        String sql = "select * from recorder_info";
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String fileName = cursor.getString(cursor.getColumnIndex("filename"));
                String filePath = cursor.getString(cursor.getColumnIndex("filepath"));
                String createTime = cursor.getString(cursor.getColumnIndex("createtime"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                float size = cursor.getFloat(cursor.getColumnIndex("size"));
                float kbSize = size/1000;
                float mbSize = size/1000000;
                String strSize = "0.00KB";
                if(size < 819200){
                    strSize = String.valueOf((float)(Math.round(kbSize*100))/(100)) + "KB";
                }
                else{
                    strSize = String.valueOf((float)(Math.round(mbSize*100))/(100)) + "MB";

                }
                mData.add(new Record(fileName, createTime, duration, strSize));

                Toast.makeText(this,fileName,Toast.LENGTH_SHORT).show();



            } while (cursor.moveToNext());
            cursor.close();
        }

        mAdapter = new RecordAdapter((LinkedList<Record>) mData, mContext);
        data_list.setAdapter(mAdapter);
        //mAdapter.setOnShowItemClickListener(this);

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
        linearLayout.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.operate_in);
        linearLayout.setAnimation(animation);

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
                    //dismissOperate();
                }
            }
        });


    }

    //加载刷新列表
    public void create_list() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entries);
        data_list.setAdapter(mAdapter);
    }
}
