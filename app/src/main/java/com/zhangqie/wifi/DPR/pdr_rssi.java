package com.zhangqie.wifi.DPR;

import java.util.ArrayList;

/**
 * Created by 唐路 on 2019/2/24.
 */
public class pdr_rssi{
    //所需要的基本元素变量
    point point_pro,point_final,point_pdr;
    double angle_pdr,distance_pdr;
    ArrayList<point> location_list;
    //计算出的结果
    ArrayList<Double> angle_list = new ArrayList<Double>();
    ArrayList<Double> distance_error_rssi = new ArrayList<Double>();
    ArrayList<Double> probability_angle = new ArrayList<Double>();
    ArrayList<Double> probability_distance = new ArrayList<Double>();

    ////所需要的方法

    //设置基础变量
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
            angle_list.add(getAngle(point_pro,location_list.get(i)) - angle_pdr);
        }
    }
    public void calDistanceList(){
        int len = location_list.size();
        for (int i = 0;i < len;i++){
            distance_error_rssi.add(getDistance(point_pro,location_list.get(i)) - distance_pdr);
        }
    }
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
        for (int i = 0;i < len;i++){
            p_final.add(weight * p_wifi.get(i) + (1 - weight)*listA.get(i)*listB.get(i));
        }
    }

    //测试代码
    /*仅限测试使用
    public static void main(String[] args){
        //测试数据
    }
    */
}

