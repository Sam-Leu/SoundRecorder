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

    private boolean isShow;
    private boolean isChecked;

    public Record(){

    }

    public Record(String fileName, String filePath, String createTime, String duration, String size){

        this.fileName = fileName;
        this.filePath = filePath;
        this.createTime = createTime;
        this.duration = duration;
        this.size = size;
    }

    public Record(String fileName,  String createTime, String duration, String size){

        this.fileName = fileName;
        this.createTime = createTime;
        this.duration = duration;
        this.size = size;
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

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        this.isShow = show;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public Record(String fileName,  String createTime, String duration, String size, boolean isShow, boolean isChecked){
        super();
        this.fileName = fileName;
        this.createTime = createTime;
        this.duration = duration;
        this.size = size;

        this.isShow = isShow;
        this.isChecked = isChecked;
    }
}
