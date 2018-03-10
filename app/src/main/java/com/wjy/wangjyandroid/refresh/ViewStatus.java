package com.wjy.wangjyandroid.refresh;

public enum ViewStatus {
    START("初始状态",0),
    REFRESHING("正在刷新",1), //这个状态由刷新框架设置
    START_SHAND("51的手",2),
    START_SBODY("51的身体",3),
    START_SEYES("51的眼睛",4),
    START_SLOGAN("绘制标语",5),
    START_SHAKE("抖动",6);

    private String name;
    private int id;

    ViewStatus(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}