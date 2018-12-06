package com.example.one.soundrecorder;

import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class RecordAdapter extends BaseAdapter {

    private LinkedList<Record> mData;
    private Context mContext;

    public RecordAdapter(LinkedList<Record> mData, Context mContext){

        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        ImageView img = (ImageView)convertView.findViewById(R.id.record_img);
        TextView tv_fileName = (TextView)convertView.findViewById(R.id.tv_fileName);
        TextView tv_duration = (TextView)convertView.findViewById(R.id.tv_duration);
        TextView tv_createTime = (TextView)convertView.findViewById(R.id.tv_createTime);
        TextView tv_size = (TextView)convertView.findViewById(R.id.tv_size);

        img.setImageResource(R.drawable.record_img);
        tv_fileName.setText(mData.get(position).getFileName());
        tv_duration.setText(mData.get(position).getDuration());
        tv_createTime.setText(mData.get(position).getCreateTime());
        tv_size.setText(mData.get(position).getSize());

        return convertView;
    }
}
