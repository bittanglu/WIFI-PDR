package com.zhangqie.wifi.Fusion_Algorithm;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;
import java.io.IOException;

public class LoadData{

    Vector<Road> roadVec = new Vector<Road>();
    Vector<Car> carVec = new Vector<Car>();
    Vector<Cross> crossVec = new Vector<Cross>();

    public void loadRoadInfo() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("*.txt"));
        reader.readLine();
        String line;
        while((line=reader.readLine())!=null){
            String item[] = line.split(",");
            Road roadInfo = new Road();
            roadInfo.id = Integer.parseInt(item[1]); //id
            roadInfo.length = Integer.parseInt(item[2]); //length
            roadInfo.speed = Integer.parseInt(item[3]); //speed
            roadInfo.channel = Integer.parseInt(item[4]); //channel
            roadInfo.from = Integer.parseInt(item[5]);
            roadInfo.to = Integer.parseInt(item[6]);
            roadInfo.isDuplex = Integer.parseInt(item[7]);
            roadVec.add(roadInfo);
        }
    }

    public void loadCarInfo() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("*.txt"));
        reader.readLine();
        String line;
        while((line=reader.readLine())!=null){
            String item[] = line.split(",");
            Car carInfo = new Car();
            carInfo.id = Integer.parseInt(item[1]);
            carInfo.from = Integer.parseInt(item[2]);
            carInfo.to = Integer.parseInt(item[3]);
            carInfo.speed = Integer.parseInt(item[4]);
            carInfo.planTime = Integer.parseInt(item[5]);
            carVec.add(carInfo);
        }
    }

    public void loadCrossInfo() throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader("*.txt"));
        reader.readLine();
        String line;
        while((line=reader.readLine())!=null){
            String item[] = line.split(",");
            int[] roadId = {Integer.parseInt(item[2]),Integer.parseInt(item[3]),Integer.parseInt(item[4]),Integer.parseInt(item[5])};
            Cross crossInfo = new Cross();
            crossVec.add(crossInfo);
        }
    }

    public void clearAllVec(){
        crossVec.clear();
        carVec.clear();
        roadVec.clear();
    }
}