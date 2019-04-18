package com.zhangqie.wifi.DPR;

public class Kalman{
    double e1 = 2.5;
    double e2 = 1;
    double gain = 0.0;
    public void calGain(){
        gain = Math.sqrt((e1*e1)/(e1*e1 + e2*e2));
    }
    public double getGain(){
        return gain;
    }
    public point calOptimal(point p1,point p2){
        double x = p1.getX() + gain*(p2.getX() - p1.getX());
        double y = p1.getY() + gain*(p2.getY() - p1.getY());
        return new point(x,y);
    }

    public void updateVariance(){
        e1 = Math.sqrt((1-gain)*e1*e1);
    }
}
