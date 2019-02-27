package com.zhangqie.wifi.DPR;

public class Step_auxiliary {

    //定义步数变量以及角度变量
    private int step_pre = 0;
    private int step_current = 0;
    private double current_angle = 0.0;
    private double pre_angle = 0.0;

    public Step_auxiliary(){

    }
    public Step_auxiliary(int step_pro,int step_current){
        this.step_pre = step_pro;
        this.step_current = step_current;
    }

    public void set_step_pro(int step_pro){
        this.step_pre = step_pro;
    }
    public void set_step_current(int step_current){
        this.step_current = step_current;
    }
    public void set_current_angle(double current_angle){
        this.current_angle = current_angle;
    }
    public void set_pre_angle(double pre_angle){
        this.pre_angle = pre_angle;
    }

    //获取步数以及角度
    public int get_step(){
        return step_current - step_pre;
    }
    public double get_angle(){
        return current_angle - pre_angle;
    }
}
