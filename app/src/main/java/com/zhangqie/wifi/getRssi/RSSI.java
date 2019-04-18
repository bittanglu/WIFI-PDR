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
public  class RSSI{
    //坐标
    double x;
    double y;
    //方差
    final public static int min_rssi = -105;
    ArrayList<Double> variance = new ArrayList<Double>();
    ArrayList<String> BSSID = new  ArrayList<String> ();
    ArrayList<Integer> rssi = new  ArrayList<Integer>();
    public RSSI(){
        this.x = 0;
        this.y = 0;
    }
    public RSSI(double x, double y){
        this.x = x;
        this.y = y;
    }
    public double getX(){
        return this.x;
    }
    public double getY(){
        return this.y;
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
        if(rssi.variance.size() == 0){
            for(int i = 0 ;i < len;i++){
                st += "," + rssi.BSSID.get(i) + "," + rssi.rssi.get(i);
            }
        }else{
            for(int i = 0 ;i < len;i++){
                st += "," + rssi.BSSID.get(i) + "," + rssi.rssi.get(i) + "," + rssi.variance.get(i);
            }
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
    public static void writeToFile(String res,String Filename) {
        try{
            FileWriter writer = new FileWriter(Filename,false);
            writer.write(res);
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
            return (int) (c1.distance - c2.distance);
        }
    };
    //自适应匹配，选取最佳的M个AP节点计算距离
    public void Adaptive_matching(ArrayList<RSSI> res,int k){
        int size = this.BSSID.size();
        int m = size;
        int[] array = new int[size];
        int i = 0;
        for(int temp : this.rssi){
            if (temp == - min_rssi){
                array[i++] = temp;
                m--;
            }
        }
        if(m < 10) m = (int) (Math.log(m)/Math.log(2) + 1);
        RSSI temp = new RSSI();
        Arrays.sort(array);
        //输出控制台查看
        for(int unm:array) {
            System.out.print(unm);
            System.out.print("");
        }
        //记录选取的m个ap节点所在的位置
        int[] table = new int[m];
        for(i = 0;i < m;i++){
            for(int j = 0;j < size;j++)
                if(array[i] == this.rssi.get(j))
                    temp.addItem(this.BSSID.get(j),this.rssi.get(j));
        }
        this.WKNN(res,k);
    }
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
        this.x /= 0;
        this.y /= 0;
        while(count > 0){
            location temp = locationPriorityQueue.poll();
            this.x += temp.x;
            this.y += temp.y;
            count--;
        }
        this.x /= k;
        this.y /= k;
    }
    //Variable K-nearest neighbors based on weighted distance
    //K值自动调节的调节的WKNN算法，效果优于WKNN
    public void Varity_WKNN(ArrayList<RSSI> res){
        ArrayList<Integer> x = new ArrayList<Integer>();
        ArrayList<Integer> y = new ArrayList<Integer>();
        int len_a = this.BSSID.size();
        int len_b = 0;
        int min = -105;
        //优先队列
        Queue<location> locationPriorityQueue = new PriorityQueue<>(2,varComparator);
        int index = 0;
        double dis_sum = 0;
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
                    //sum_var += 1/(1 + temp.variance.get(j));
                }
            }
            double distance = 0;
            double dis_max = 0;
            for (int i = 0;i < x.size();i++){
                int curr_i = x.get(i),curr_j = y.get(i);
                distance += (1/(1+temp.variance.get(curr_j))/sum_var)*(this.rssi.get(curr_i) - temp.rssi.get(curr_j))*(this.rssi.get(curr_i) - temp.rssi.get(curr_j));
                dis_max = Math.max(dis_max,distance);
            }
            distance += 2 * (len_a - x.size()) * dis_max;
			/*
			Processing of undetected data in fingerprint database
			for(int i = 0;i < len_b;i++){
				if(!y.contains(i)){
					distance += (1/(1+temp.variance.get(i))/sum_var)*(min - temp.rssi.get(i))*(min - temp.rssi
					get(i));
				}
			}
			*/
            dis_sum += Math.sqrt(distance);
            locationPriorityQueue.add(new location(temp.x,temp.y,Math.sqrt(distance)));
            x.clear();
            y.clear();
        }
        double dis_average = dis_sum / locationPriorityQueue.size();
        int count = 0;
        this.x = 0;
        this.y = 0;
        double error = 0;
        double error_var = 0,error_pro = 0;
        //Using Similarity as Measure
		/*
		location temp = locationPriorityQueue.poll();
		while(temp.var <= dis_average){
			count++;
			this.x += temp.x;
			this.y += temp.y;
			temp = locationPriorityQueue.poll();
		}
		*/
        //Using distance growth as a measure
        double Reciprocal_dis_sum = 0.0;
        while(count < 10){
            location location_temp = locationPriorityQueue.poll();
            error_var = location_temp.distance - error;
            this.x += location_temp.x/location_temp.getDis();
            this.y += location_temp.y/location_temp.getDis();
            Reciprocal_dis_sum += 1/location_temp.getDis();
            count++;
            if (error_pro != 0 && error_var >= 1.12 * error_pro)
                break;
            error_pro = error_var;
        }
        this.x = this.x/Reciprocal_dis_sum;
        this.y = this.y/Reciprocal_dis_sum;
    }
    //Enhanced Weighted K-Nearest Neighbor , Consideration of node stability
    //res为指纹库列表
    //probability_distance为结果列表的可能性列表
    //location_res为位置结果列表
    //Enhanced Weighted K-Nearest Neighbor , Consideration of node stability
    public void EWKNN(ArrayList<RSSI> res,ArrayList<Double> probability_distance,ArrayList<location> location_res){
        ArrayList<Integer> x = new ArrayList<Integer>();
        ArrayList<Integer> y = new ArrayList<Integer>();
        int len_a = this.BSSID.size();
        int len_b = 0;
        int min = -105;
        //优先队列
        ArrayList<location> locationPriorityQueue = new ArrayList<location>();
        int index = 0;
        this.x = 0;
        this.y = 0;
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
                    //sum_var += 1/(1 + temp.variance.get(j));
                }
            }
            double distance = 0;
            double dis_max = 0;
            for (int i = 0;i < x.size();i++){
                int curr_i = x.get(i),curr_j = y.get(i);
                distance += (this.rssi.get(curr_i) - temp.rssi.get(curr_j))*(this.rssi.get(curr_i) - temp.rssi.get(curr_j));
                dis_max = Math.max(dis_max,distance);
            }
            distance += 2 * (len_a - x.size()) * dis_max;
			/*
			Processing of undetected data in fingerprint database
			for(int i = 0;i < len_b;i++){
				if(!y.contains(i)){
					distance += (1/(1+temp.variance.get(i))/sum_var)*(min - temp.rssi.get(i))*(min - temp.rssi
					get(i));
				}
			}
			*/
            if(distance == 0.0){
                this.x = temp.x;
                this.y = temp.y;
                return ;
            }
            location loc = new location(temp.x,temp.y,Math.sqrt(distance));
            if(loc == null) System.out.println("RSSI:loc = null");
            locationPriorityQueue.add(loc);
            x.clear();
            y.clear();
        }
        ArrayList<location> location_list = new ArrayList<location>();
        //sort
        Collections.sort(locationPriorityQueue,varComparator);

        //Computing Reserved Nodes
        double dis_sum = 0;
        int k = 0;
        for(location location_temp : locationPriorityQueue){
            if(location_temp.getDis() < 1.7 * locationPriorityQueue.get(0).getDis()){
                location_list.add(location_temp);
                dis_sum += location_temp.getDis();
            }
        }
        //System.out.println("First:" + location_list.size());
        double dis_average = dis_sum / location_list.size();
        double Reciprocal_dis_sum = 0.0;
        int count = 0;
        for(int i = 0;i < location_list.size();i++){
            location location_temp = location_list.get(i);
            if(location_temp.getDis() > dis_average){
                if(i == 0 ){
                    System.out.println("dis_average:" + Double.toString(dis_average));
                    System.out.println("location_temp.getDis():" + Double.toString(location_temp .getDis()));
                }
                if(count > 1)break;
            }
            count++;
            if(location_temp.getDis() == 0)
                location_temp.setDis(1);
            this.x += location_temp.x/location_temp.getDis();
            this.y += location_temp.y/location_temp.getDis();
            //System.out.println("Distance:" + location_temp.getDis());
            location_res.add(location_temp);
            Reciprocal_dis_sum += 1/location_temp.getDis();
        }
        //System.out.println("Second:" + count);
        this.x = this.x/Reciprocal_dis_sum;
        this.y = this.y/Reciprocal_dis_sum;
        for(int i = 0;i < location_res.size();i++){
            probability_distance.add((1/location_res.get(i).getDis()) / Reciprocal_dis_sum);
        }
        if(this.x > 1000) System.out.println("Reciprocal_dis_sum:" + Double.toString(Reciprocal_dis_sum));
    }
    //normalization
    //归一化处理，效果并不好
    public static void Normalization(RSSI temp){
        int len = temp.rssi.size();
        int[] a = new int[len];
        int sum = 0;
        for(int i = 0;i < len;i++){
            a[i] = temp.rssi.get(i) - min_rssi;
            sum += a[i];
        }
        temp.rssi.clear();
        for(int i = 0;i < len;i++){
            a[i] = (a[i]*100)/sum;
            temp.rssi.add(a[i]);
        }
    }
}
