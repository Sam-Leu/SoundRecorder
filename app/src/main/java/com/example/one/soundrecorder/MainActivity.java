package com.example.one.soundrecorder;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 打开录音记录activity的小测试，可删除
        dbHelper = new DatabaseHelper(this,1);
        insert();
        Button bb = (Button)findViewById(R.id.btn);
        bb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RecordActivity.class);
                startActivity(intent);
            }
        });
    }


    // 数据插入演示
    public void insert(){

        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("createtime",2018);
        values.put("duration",10243);
        values.put("filename","name");
        values.put("filepath","/dara/data/");
        values.put("size",10);
        db.insert("recorder_info", null, values);
    }
}
