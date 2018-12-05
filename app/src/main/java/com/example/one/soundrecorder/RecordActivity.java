package com.example.one.soundrecorder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Vector;

public class RecordActivity extends AppCompatActivity {

    private ListView data_list;
    private Vector<String> entries;

    private DatabaseHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        dbHelper = new DatabaseHelper(this,1);
        entries = new Vector<String>();
        data_list = (ListView)findViewById(R.id.data_list);
        create_list();
        showRecord();
    }

    //显示记录
    private void showRecord(){
        entries.clear();

        db = dbHelper.getReadableDatabase();

        String sql = "select * from recorder_info";
        Cursor cursor = db.rawQuery(sql,null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String createtime = cursor.getString(cursor.getColumnIndex("createtime"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                String filename = cursor.getString(cursor.getColumnIndex("filename"));
                String filepath = cursor.getString(cursor.getColumnIndex("filepath"));
                String size = cursor.getString(cursor.getColumnIndex("size"));
                entries.add(id+":\n创建时间：" + createtime + ",\n文件长度：" + duration+ ",\n文件名：" + filename+ ",\n存储位置：" + filepath+ ",\n文件大小：" + size + "B");
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    //加载刷新列表
    public void create_list() {
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entries);
        data_list.setAdapter(adapter);
    }
}
