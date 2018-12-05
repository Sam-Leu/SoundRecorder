package com.example.one.soundrecorder;

public class Record {
    /**
     *
     */
    private String fileName;
    private String filePath;
    private String createTime;
    private String duration;
    private String size;

    public Record(){

    }

    public Record(String fileName, String filePath, String createTime, String duration, String size){

        this.fileName = fileName;
        this.filePath = filePath;
        this.createTime = createTime;
        this.duration = duration;
        this.size = size;
    }

    public Record(String fileName,  String createTime, String duration){

        this.fileName = fileName;
        this.createTime = createTime;
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getDuration() {
        return duration;
    }

    public String getSize() {
        return size;
    }
}
