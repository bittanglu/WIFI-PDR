package com.zhangqie.wifi.getRssi;

/**
 * Created by 唐路 on 2018/10/10.
 */
//位置类，包含成员变量x，y，方差var
public class location {
    double x;
    double y;
    double var;
    location(double x,double y,double var){
        this.x = x;
        this.y = y;
        this.var = var;
    }
    public double getVar(){
        return this.var;
    }
}