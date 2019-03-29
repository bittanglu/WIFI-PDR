package com.zhangqie.wifi.Fusion_Algorithm;

public class Road{
    //基础变量
    int id;
    int length;
    int speed;
    int channel;
    int from;
    int to;
    int isDuplex;

    //状态变量

    //构造函数
    public void Road(int id,int length,int speed,int channel,int from,int to,int isDuplex){
        this.id = id;
        this.length = length;
        this.speed = speed;
        this.channel = channel;
        this.from = from;
        this.to = to;
        this.isDuplex = isDuplex;
    }
    public int getID(){
        return id;
    }
    public int getLength(){
        return length;
    }
    public int getSpeed(){
        return speed;
    }
    public int getChannel(){
        return channel;
    }
    public int getFrom(){
        return from;
    }
    public int getTo(){
        return to;
    }
    public int getIsDuplex(){
        return isDuplex;
    }
}
