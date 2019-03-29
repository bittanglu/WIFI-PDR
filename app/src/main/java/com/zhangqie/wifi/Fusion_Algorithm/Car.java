package com.zhangqie.wifi.Fusion_Algorithm;

import java.util.ArrayList;

public class Car{
    int id;
    int from;
    int to;
    int speed;
    int planTime;
    Position position;
    int state;
    ArrayList<Integer> rote = new ArrayList<Integer>();
    public void Car(int id,int from,int to,int speed,int planTime){
        this.id = id;
        this.from = from;
        this.to = to;
        this.speed = speed;
        this.planTime = planTime;
    }
    public int getId(){
        return id;
    }
    public int getFrom(){
        return from;
    }
    public int getTo(){
        return to;
    }
    public int getSpeed(){
        return speed;
    }
    public int getPlanTime(){
        return planTime;
    }
    public void setPosition(Position position){
        this.position = position;
    }
    public Position getPosition(){
        return position;
    }
    public void setState(int state){
        this.state = state;
    }
    public int getState(){
        return state;
    }
}
