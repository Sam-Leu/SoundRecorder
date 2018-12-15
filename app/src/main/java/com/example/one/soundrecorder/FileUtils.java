package com.example.one.soundrecorder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils{
    /**
     * 重命名文件，传入原来的文件路径，然后给一个新的名称即可，不用再给路径
     * @param oldFilePath 原来的文件地址
     * @param newFileName 新的文件名
     * 返回：1-成功命名，2-命名重复，3-新文件名为空，4-找不到原文件
     * */
    public static int renameFile(String oldFilePath, String newFileName) {
        // 时间格式，用来显示日志产生时间
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat();

        File oldFile = new File(oldFilePath);
        if (!oldFile.exists()) { // 判断原文件是否存在
            Log.i(String.valueOf(dateFormat.format(new Date())), "原文件不存在。");
            return 4;
        }

        newFileName = newFileName.trim();   //去掉新文件名的前后空格
        String newFilePath; //通过新文件名和旧文件路径重创一个新文件路径
        if ("".equals(newFileName)){ // 新的文件名不能为空
            Log.i(String.valueOf(dateFormat.format(new Date())), "新文件名为空了。");
            return 3;
        }

        // 通过新文件名和旧文件路径重创一个新文件路径
        if (oldFile.isDirectory()) { // 判断是否为文件夹
            newFilePath = oldFilePath.substring(0, oldFilePath.lastIndexOf("/")) + "/" + newFileName;
        } else {
            newFilePath = oldFilePath.substring(0, oldFilePath.lastIndexOf("/"))+ "/"  + newFileName + oldFilePath.substring(oldFilePath.lastIndexOf("."));
        }

        File newFile = new File(newFilePath);
        if(newFile.exists()){   //新文件名重复
            Log.i(String.valueOf(dateFormat.format(new Date())), "新文件名重复了。");
            return 2;
        }else{
            try {
                newFile.createNewFile();
                Log.i(String.valueOf(dateFormat.format(new Date())), "新文件创建成功:"+newFile.getAbsolutePath());
            } catch (IOException e) {
                Log.i(String.valueOf(dateFormat.format(new Date())), "创建新文件不成功。");
                e.printStackTrace();
            }
        }
        //执行重命名
        oldFile.renameTo(newFile);
        return 1;
    }


    /**
     * 录音文件重命名弹窗
     * @param context   调用该方法的活动的上下文
     * @param oldFilePath  旧文件的路径，直接用 file.getAbsolutePath() 传过来即可
     * @param initFileName 为命名文本框的默认文本，如果是刚录音完调用，则传入时间，如果是对录音历史列表的文件重命名，则传入旧文件名
     */
    public static void renameDialog(final Context context, final String oldFilePath, final String initFileName) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("为新文件起个名字吧");

        // 输入新文件名的文本框
        final EditText setNameTextView = new EditText(context);
        // 初始状态时，文本框的内容为时间或旧文件名
        setNameTextView.setText(initFileName);

        dialogBuilder.setView(setNameTextView);

        dialogBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (renameFile(oldFilePath, String.valueOf(setNameTextView.getText()))){
                    case 1:
                        break;
                    case 2:
                        renameFile(oldFilePath, initFileName);
                        break;
                    case 3:
                        renameFile(oldFilePath, initFileName);
                        break;
                    case 4:
                        renameFile(oldFilePath, initFileName);
                        break;
                }
            }
        });
        dialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (renameFile(oldFilePath, String.valueOf(setNameTextView.getText()))){
                    case 1:
                        break;
                    case 2:
                        renameFile(oldFilePath, initFileName);
                        break;
                    case 3:
                        renameFile(oldFilePath, initFileName);
                        break;
                    case 4:
                        renameFile(oldFilePath, initFileName);
                        break;
                }
            }
        });
        dialogBuilder.show();
    }
}
