package com.example.one.soundrecorder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class RecordActivity extends AppCompatActivity {

//    private ListView data_list;
    private Vector<String> entries;

    private DatabaseHelper dbHelper;
    SQLiteDatabase db;

    private List<Record> mData = null;
    private Context mContext;
    private RecordAdapter mAdapter = null;
    private ListView data_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        dbHelper = new DatabaseHelper(this,1);

        mContext = RecordActivity.this;
        data_list = (ListView)findViewById(R.id.data_list);

        entries = new Vector<String>();

        mData = new LinkedList<Record>();

//        mData.add(new Record("name", "2018-11-11", "00:12"));
//
//        mAdapter = new RecordAdapter((LinkedList<Record>) mData, mContext);
//        data_list.setAdapter(mAdapter);

        //create_list();
        showRecord();
    }

    //显示记录
    private void showRecord(){
        //mData.clear();

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
                String size = cursor.getString(cursor.getColumnIndex("size"));
                mData.add(new Record(fileName, createTime, duration));

                Toast.makeText(this,fileName,Toast.LENGTH_SHORT).show();



            } while (cursor.moveToNext());
            cursor.close();
        }

        mAdapter = new RecordAdapter((LinkedList<Record>) mData, mContext);
        data_list.setAdapter(mAdapter);


    }

    //加载刷新列表
    public void create_list() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entries);
        data_list.setAdapter(mAdapter);
    }
}
