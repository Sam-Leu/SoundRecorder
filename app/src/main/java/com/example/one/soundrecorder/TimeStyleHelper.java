package com.example.one.soundrecorder;

public class TimeStyleHelper {
    //将秒数格式化成时分秒格式
    public static String showTimeCount(int time) {
        int second = time % 60; //格式化的秒，而time是原始的秒
        if (time < 60) {
            return "00:00:" + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second));
        } else {
            int original_minute = time / 60;  //原始的分
            int minute = original_minute % 60;  //格式化的分
            if (original_minute < 60) {
                return "00:" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + ":" + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second));
            } else {
                int original_hour = time / 3600;    //原始的时，上升到天数是时才会用到
                int hour = original_hour % 24;  //格式化的时
                return (hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour)) + ":" + (minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute)) + ":" + (second < 10 ? "0" + String.valueOf(second) : String.valueOf(second));
            }
        }
    }
}
