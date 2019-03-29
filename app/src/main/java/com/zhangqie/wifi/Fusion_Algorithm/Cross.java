package com.zhangqie.wifi.Fusion_Algorithm;

import java.util.Vector;

public class Cross{

    int id;
    int[] roadID;

    public void Cross(int id,int[] roadID){
        this.id = id;
        this.roadID = roadID;
    }

    public int getID(){
        return id;
    }

    final public int getIndex(int singleRoadID){
        for(int i = 0; i < roadID.length; i++){
            if(roadID[i] == singleRoadID){
                return i;
            }
        }
        return -1;
    }


    public int judgeTurn(int sourceRoadID, int targetRoadID){
        int sourceRoadIndex = this.getIndex(sourceRoadID);
        int targetRoadIndex = this.getIndex(targetRoadID);
        if (-1 == sourceRoadIndex || -1 == targetRoadIndex){
            return -1;
        }
        else{
            switch (targetRoadIndex - sourceRoadIndex) {
                case -3:
                case 1:
                    return 0; //0 turn left
                case -2:
                case 2:
                    return 1; //1 go straight
                case -1:
                case 3:
                    return 2; //2 turn right
                case 0:
                    return -1;
                default:
                    return -1;
            }
        }
    }

    public Vector potentialTurn(int sourceRoadID){
        Vector<Integer> vec = new Vector<Integer>();
        vec.add(0);vec.add(1);vec.add(2);
        int sourceRoadIndex = this.getIndex(sourceRoadID);
        if(-1 == sourceRoadIndex){
            vec.clear();
            return vec;
        }
        else{
            for(int i = 0; i < 4; i++){
                if(-1 == roadID[i]){
                    switch (i - sourceRoadIndex) {
                        case -3:
                        case 1:
                            vec.remove(0); //0 turn left]
                            break;
                        case -2:
                        case 2:
                            vec.remove(1); //1 go straight
                            break;
                        case -1:
                        case 3:
                            vec.remove(2); //2 turn right
                            break;
                    }
                }
            }
            return vec;
        }
    }
}
