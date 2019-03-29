package com.zhangqie.wifi.DPR;

import android.util.Log;

import com.zhangqie.wifi.getRssi.RSSI;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

//辅助point类
public class point {
    double x,y;
    point(){
        x = 0.0;
        y = 0.0;
    }
    point(double x,double y){
        this.x = x;
        this.y = y;
    }
    public void setCoordinate(double x,double y){
        this.x = x;
        this.y = y;
    }
    public double getX(){
        return this.x;
    }
    public double getY(){
        return this.y;
    }

    //写入文件操作
    public String toString() {
        String st = "";
        st += this.getX() + "," + this.getY();
        st += "\r\n";
        return st;
    }
    public static void writeToFile(ArrayList<point> list, String filename){
        int len = list.size();
        try {
            FileWriter writer = new FileWriter(filename,false);
            for (int i = 0;i < len ;i++) {
                writer.write(list.get(i).toString());
                Log.d("PDR文件写入",list.get(i).toString());
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
