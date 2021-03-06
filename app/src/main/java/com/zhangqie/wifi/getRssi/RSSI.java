package com.zhangqie.wifi.getRssi;

import java.util.*;
import java.io.*;
/**
 * Created by 唐路 on 2018/9/26.
 */

//RSSI类，用于存储每次监测的RSSI信号
/*
成员变量：
坐标：x，y
方差variance数组，地址BSSID数组，信号强度数组rssi

方法：

 RSSI() 初始化，设置坐标均为0
 RSSI(double x,double y) 初始化，设置坐标
 setCoordinate(double x,double y) 设置坐标
 addItem(String id,int rssi) 加入一组数据
 addItem(String id,int rssi,Double var) 加入一组数据
 merge(ArrayList<RSSI> data) 合并一组数据，传入数据的坐标是一样的
 print() 打印
 toString(RSSI rssi) 返回String
 readFromFile(ArrayList<RSSI> res,String Filename) 从文件中读取RSSI数据库到res中
 writeToFile(ArrayList<RSSI> res,String Filename) 将RSSI数据组res写入到文件中
 WKNN(ArrayList<RSSI> res,int k) WKNN的实现，找到K个距离最近的点

 */
public class RSSI{
    //坐标
    double x;
    double y;
    //方差
    final public static int min_rssi = -105;
    ArrayList<Double> variance = new ArrayList<Double>();
    ArrayList<String> BSSID = new  ArrayList<String> ();
    ArrayList<Integer> rssi = new  ArrayList<Integer>();
    RSSI(){
        this.x = 0;
        this.y = 0;
    }
    RSSI(double x,double y){
        this.x = x;
        this.y = y;
    }
    public void setCoordinate(double x,double y){
        this.x = x;
        this.y = y;
    }
    public void addItem(String id,int rssi){
        this.BSSID.add(id);
        this.rssi.add(rssi);
    }
    public void addItem(String id,int rssi,Double var){
        this.BSSID.add(id);
        this.rssi.add(rssi);
        this.variance.add(var);
    }
    //data为需要合并的RSSI信号
    public void merge(ArrayList<RSSI> data){
        int len = data.size();
        if (len <= 0) return;
        int i,j,k,index;
        ArrayList<String> id = new ArrayList<String>();
        ArrayList<Integer> value = new ArrayList<Integer>();
        ArrayList<Integer> count = new ArrayList<Integer>();
        for(i = 0;i < len;i++){
            RSSI temp = data.get(i);
            for(j = 0;j < temp.BSSID.size();j++){
                String temp_id = temp.BSSID.get(j);
                index = id.indexOf(temp_id);
                if (index >= 0){
                    value.set(index,value.get(index)+temp.rssi.get(j));
                    count.set(index,count.get(index) + 1);
                }else{
                    id.add(temp_id);
                    value.add(temp.rssi.get(j));
                    count.add(1);
                }
            }
        }
        for(i = 0; i < value.size();i++){
            int val = (value.get(i) + min_rssi*(len - count.get(i)))/len;
            double v = 0.0;
            for(j = 0;j < len;j++){
                int val1 = min_rssi;
                index = data.get(j).BSSID.indexOf(id.get(i));
                if (index >= 0)  val1 = data.get(j).rssi.get(index);
                v += (val1-val)*(val1-val);
            }
            v = Math.sqrt(v/len);
            value.set(i,val);
            this.variance.add(v);
        }
        this.BSSID.addAll(id);
        this.rssi.addAll(value);
    }
    //print RSSI
    public void print(){
        int len = this.BSSID.size();
        System.out.print(RSSI.toString(this));
    }
    //change RSSI to String
    public static String toString(RSSI rssi){
        String st = "";
        int len = rssi.BSSID.size();
        st += rssi.x + "," + rssi.y;
        for(int i = 0 ;i < len;i++){
            st += "," + rssi.BSSID.get(i) + "," + rssi.rssi.get(i) + "," + rssi.variance.get(i);
        }
        st +="\r\n";
        return st;
    }
    //从文件中读出RSSI
    public static void readFromFile(ArrayList<RSSI> res,String Filename)throws Exception{
        File filename = new File(Filename);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
        BufferedReader br = new BufferedReader(reader);
        String line = "";
        try{
            while(line != null){
                line = br.readLine();
                if (line == null) break;
                String[] st = line.replaceAll(" ", "").split(",");
                RSSI temp = new RSSI(Double.parseDouble(st[0]),Double.parseDouble(st[1]));
                int len = (st.length - 2)/3;
                for (int i = 0;i < len;i++){
                    temp.addItem(st[2 + i*3],Integer.valueOf(st[3 + i*3]),Double.parseDouble(st[4 + i*3]));
                }
                res.add(temp);
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //将RSSI序列化后写入文件
    public static void writeToFile(ArrayList<RSSI> res,String Filename){
        int len = res.size();
        try{
            FileWriter writer = new FileWriter(Filename,false);
            for(int i = 0;i < len;i++){
                writer.write(RSSI.toString(res.get(i)));
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    //匿名Comparator实现
    public static Comparator<location> varComparator = new Comparator<location>(){

        @Override
        public int compare(location c1, location c2) {
            return (int) (c1.var - c2.var);
        }
    };
    //WKKN
    public void WKNN(ArrayList<RSSI> res,int k){
        ArrayList<Integer> x = new ArrayList<Integer>();
        ArrayList<Integer> y = new ArrayList<Integer>();
        int len_a = this.BSSID.size();
        int len_b = 0;
        int min = -105;
        //优先队列

        Queue<location> locationPriorityQueue = new PriorityQueue<>(k,varComparator);
        int index = 0;
        for(RSSI temp : res){
            double sum_var = 0;
            len_b = temp.BSSID.size();
            for(int i = 0 ;i < len_a;i++){
                String current = this.BSSID.get(i);
                for(int j = 0 ;j < len_b;j++){
                    if (current.equals(temp.BSSID.get(j)) && this.rssi.get(i) != min_rssi && temp.rssi.get(j) != min_rssi){
                        x.add(i);
                        y.add(j);
                        sum_var += 1/(1 + temp.variance.get(j));
                        break;
                    }
                }
            }
            double distance = 0;
            for (int i = 0;i < x.size();i++){
                int curr_i = x.get(i),curr_j = y.get(i);
                //distance += (this.rssi.get(curr_i) - temp.rssi.get(curr_j))*(this.rssi.get(curr_i) - temp.rssi.get(curr_j));
                distance += (1/(1+temp.variance.get(curr_j))/sum_var)*(this.rssi.get(curr_i) - temp.rssi.get(curr_j))*(this.rssi.get(curr_i) - temp.rssi.get(curr_j));
            }
            locationPriorityQueue.add(new location(temp.x,temp.y,Math.sqrt(distance)));
            x.clear();
            y.clear();
        }
        int count = k;
        while(count > 0){
            location temp = locationPriorityQueue.poll();
            this.x += temp.x;
            this.y += temp.y;
            count--;
        }
        this.x /= k;
        this.y /= k;
    }
}
