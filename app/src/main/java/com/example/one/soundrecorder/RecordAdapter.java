package com.example.one.soundrecorder;

import android.content.Context;
import android.icu.text.AlphabeticIndex;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class RecordAdapter extends BaseAdapter {

    private LinkedList<Record> mData;
    private Context mContext;
    private OnShowItemClickListener onShowItemClickListener;

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
        ViewHolder holder;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

            holder.img = (ImageView)convertView.findViewById(R.id.record_img);
            holder.tv_fileName = (TextView)convertView.findViewById(R.id.tv_fileName);
            holder.tv_duration = (TextView)convertView.findViewById(R.id.tv_duration);
            holder.tv_createTime = (TextView)convertView.findViewById(R.id.tv_createTime);
            holder.tv_size = (TextView)convertView.findViewById(R.id.tv_size);
            holder.cb_select = (CheckBox)convertView.findViewById(R.id.cb_select);
            convertView.setTag(holder);

        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Record record = mData.get(position);

        if (record.isShow()){
            holder.cb_select.setVisibility(View.VISIBLE);
        }
        else {
            holder.cb_select.setVisibility(View.GONE);
        }




        holder.img.setImageResource(R.drawable.record_img);
        holder.tv_fileName.setText(mData.get(position).getFileName());
        holder.tv_duration.setText(mData.get(position).getDuration());
        holder.tv_createTime.setText(mData.get(position).getCreateTime());
        holder.tv_size.setText(mData.get(position).getSize());
        holder.cb_select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    record.setChecked(true);
                }
                else {
                    record.setChecked(false);
                }

                onShowItemClickListener.onShowItemClick(record);
            }
        });

        holder.cb_select.setChecked(record.isChecked());

        return convertView;
    }

    static class ViewHolder{
        ImageView img;
        TextView tv_fileName;
        TextView tv_duration;
        TextView tv_createTime;
        TextView tv_size;
        CheckBox cb_select;
    }

    public interface OnShowItemClickListener{
        public void onShowItemClick(Record record);
    }

    public void setOnShowItemClickListener(OnShowItemClickListener onShowItemClickListener){
        this.onShowItemClickListener = onShowItemClickListener;
    }
}
