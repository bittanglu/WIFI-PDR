package com.zhangqie.wifi.getRssi;

import java.util.ArrayList;

/**
 * Created by 唐路 on 2018/9/26.
 */
//RSSI类，用于存储每次监测的RSSI信号
public class RSSI {
    int x;
    int y;
    ArrayList<String> BSSID = new  ArrayList<String> ();
    ArrayList<Integer> rssi = new  ArrayList<Integer>();
    RSSI(){
        this.x = 0;
        this.y = 0;
    }
    RSSI(int x,int y){
        this.x = x;
        this.y = y;
    }
    public void setCoordinate(int x,int y){
        this.x = x;
        this.y = y;
    }
    public void addItem(String id,int val){
        this.BSSID.add(id);
        this.rssi.add(val);
    }
    //用于合并采集的样本
    public void merge(ArrayList<RSSI> data){
        int len = data.size();
        int min_rssi = -90;
        if (len <= 0) return;
        int i,j,k;
        for(i = 0;i < len;i++){

        }
    }
}
