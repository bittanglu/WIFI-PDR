package com.zhangqie.wifi.getRssi;

/**
 * Created by 唐路 on 2018/10/9.
 */
public class location{
    double x;
    double y;
    double distance;
    location(double x,double y,double distance){
        this.x = x;
        this.y = y;
        this.distance = distance;
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public void setDis(double distance){
        this.distance = distance;
    }
    public double getDis(){
        return distance;
    }

}
