package com.zhangqie.wifi.DPR;

import com.zhangqie.wifi.getRssi.location;

import java.util.ArrayList;

/**
 * Created by 唐路 on 2019/2/24.
 */
public class pdr_rssi{
    //所需要的基本元素变量
    point point_pro = new point();
    point point_final = new point();
    point point_pdr = new point();;
    double angle_pdr,distance_pdr;
    ArrayList<location> location_list = new ArrayList<location>();
    //计算出的结果
    ArrayList<Double> angle_list = new ArrayList<Double>();
    ArrayList<Double> distance_error_rssi = new ArrayList<Double>();

    ArrayList<Double> probability_angle = new ArrayList<Double>();
    ArrayList<Double> probability_distance = new ArrayList<Double>();

    ////所需要的方法

    //设置基础变量
    pdr_rssi(){
        point_pro = new point(0,0);
    }
    public void setPonitPro(double x,double y){
        point_pro.setCoordinate(x,y);
    }
    public void setPonitFinal(double x,double y){
        point_final.setCoordinate( x, y);
    }
    public void setPonitPdr(double x,double y){
        point_pdr.setCoordinate( x, y);
    }
    public void setAnglePdr(double angle){
        angle_pdr = angle;
    }
    public void setDistancePdr(double distance){
        distance_pdr = distance;
    }

    //计算结果
    public void calAngleList(){
        int len = location_list.size();
        for (int i = 0;i < len;i++){
            point temp = new point(location_list.get(i).getX(),location_list.get(i).getY());
            angle_list.add(getAngle(temp,point_pro) - angle_pdr);
        }
    }
    public void calDistanceList(){
        int len = location_list.size();
        for (int i = 0;i < len;i++){
            point temp = new point(location_list.get(i).getX(),location_list.get(i).getY());
            distance_error_rssi.add(getDistance(point_pro,temp) - distance_pdr);
        }
    }
    //计算一组数据的高斯分布概率
    public void calProbability(ArrayList<Double> list,ArrayList<Double> res){
        double average = 0.0,sigma = 0.0;
        int len = list.size();

        for (int i = 0; i < len;i++)
            average += list.get(i);
        average = average / len;

        for (int i = 0; i < len;i++)
            sigma += Math.sqrt((list.get(i) - average) * (list.get(i) - average));
        sigma = sigma/(len - 1);

        double twoSigmaSquare = 2.0f * sigma * sigma;
        double sigmaroot = Math.sqrt(twoSigmaSquare * Math.PI);

        double probability = 0.0;
        for (int i = 0; i < len;i++){
            probability = Math.exp(-(list.get(i) - average)*(list.get(i) - average)/twoSigmaSquare)/sigmaroot;
            res.add(probability);
        }
    }

    public double getAngle(point a,point b){
		point c = new point(b.getX() - a.getX(),b.getY() - a.getY());
        return Math.toDegrees(Math.atan(c.getY()/c.getX()));
    }
    public double getDistance(point a,point b){
        return Math.sqrt((a.getX() - b.getX())*(a.getX() - b.getX())+(a.getY() - b.getY())*(a.getY() - b.getY()));
    }

    //Weight normalization
    public void weightNormalization(ArrayList<Double> p_wifi,ArrayList<Double> listA,ArrayList<Double> listB,ArrayList<Double> p_final,double weight){
        int len = listA.size();
        double p_sum =0.0;
        ArrayList<Double> p_temp = new ArrayList<Double>();
        for (int i = 0;i < len;i++){
            double p = weight * p_wifi.get(i) + (1 - weight)*listA.get(i)*listB.get(i);
            p_sum += p;
            p_temp.add(p);
        }
        for (int i = 0;i < len;i++){
            p_final.add(p_temp.get(i)/p_sum);
        }
    }
    public void calFinalPoint(ArrayList<Double> p,ArrayList<location> list) {
        double x =0.0,y=0.0;
        int len = p.size();
        for(int i = 0;i < len;i++){
            x += p.get(i) * list.get(i).getX();
            y += p.get(i) * list.get(i).getY();
        }
        this.setPonitFinal(x,y);
    }
    public void clearAll(){
        location_list.clear();
        angle_list.clear();
        distance_error_rssi.clear();
        probability_angle.clear();
        probability_distance.clear();
    }

    //测试代码
    /*仅限测试使用
    public static void main(String[] args){
        //测试数据
    }
    */
}

