package com.zhangqie.wifi.Fusion_Algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class Graph{

    private int [][] graph;//加权有向图
    private int temp; //寄存当前数据编号前三位
    private int start;//源点编号 从 0开始
    private int dimention;
    static int INF  = Integer.MAX_VALUE /100 ;
    //用于标记顶点是否已经计算
    private Set<Integer> vertexSet = new HashSet<Integer>();
    //存储结果，Map的key对应各终点编号，value对应路径编号列表。
    private Map<Integer, List<Integer>> pathListMap = new HashMap<Integer, List<Integer>>();
    /**
     * 构造函数，必须初始化路径矩阵和起始点
     * @param graph
     * @param start
     */
    public Graph(Vector roadVec, Vector crossVec, Car carInfo) {
        updateGraph(roadVec,crossVec,carInfo);
        this.start = start;
        this.dimention = graph.length;
        calculate();
    }

    /**
     * 初始化，更新权重函数
     * 以车辆在这条路上的最快同行时间作为权重
     */

    private void updateGraph(Vector<Road> roadVec, Vector<Cross> crossVec,Car carInfo){
        int[][] graphTemp = new int[crossVec.size()][crossVec.size()];
        for(int i = 0 ;i < crossVec.size();i++){
            for(int j = 0;j < crossVec.size();j++){
                graphTemp[i][j] = Integer.MAX_VALUE;
            }
        }
        temp = roadVec.get(1).getID()/1000;
        for(int i = 0; i < roadVec.size(); i++){
            int speed = roadVec.get(i).getSpeed() < carInfo.getSpeed() ? roadVec.get(i).getSpeed() : carInfo.getSpeed();
            if(1 == roadVec.get(i).getIsDuplex()){
                graphTemp[roadVec.get(i).getFrom()-1][roadVec.get(i).getTo()-1] = roadVec.get(i).length/speed; //注意此处序号减了1   需不需要转为float型
                graphTemp[roadVec.get(i).getTo()-1][roadVec.get(i).getFrom()-1] = roadVec.get(i).length/speed;
            }
            else{
                graphTemp[roadVec.get(i).getFrom()-1][roadVec.get(i).getTo()-1] = roadVec.get(i).length/speed; //注意此处序号减了1
            }
        }
        this.graph = graphTemp;
    }
    /**
     * 计算函数
     */
    private void calculate(){
        //初始化
        for(int end=0; end<dimention; end++){
            if(end == start){ continue; }//起始点自己的路径排除。
            List<Integer> pathList =  new ArrayList<Integer>();
            pathList.add( start );//每条路径的起始点都为start，pathList只记录编号，不记录路径权值
            pathList.add( end );//每条路径的第二个参数为终点编号
            pathListMap.put(end, pathList);
        }
        //计算主体
        for(int bridge=0; bridge<dimention; bridge++){
            if(bridge == start){ continue; }
            if(!vertexSet.contains(bridge)){//确保每个基点只循环计算一次
                for(int next=0; next<dimention; next++){
                    if(next == start || next==bridge ){ continue; }
                    if(startTo(bridge) + getRawLength(bridge, next) < startTo(next) ){
                        List<Integer> pathList =  pathListMap.get(next);
                        List<Integer> bridgePathList =  pathListMap.get(bridge);
                        //清空，使用新的
                        pathList.clear();
                        pathList.addAll( bridgePathList );
                        pathList.add( next );
                    }
                }
            }
            vertexSet.add(bridge);
        }
        //检查，是否桥接的路径都被更新
        for(int end=0; end<dimention; end++){
            if(end == start){ continue; }
            List<Integer> pathList =  pathListMap.get(end);
            int size = pathList.size();
            if(size>2){
                for(int end2=0; end2<dimention; end2++){
                    int isEnd = pathList.get(size-2 );
                    if(end2 == isEnd){
                        pathList.clear();
                        pathList.addAll(  pathListMap.get(end2) );
                        pathList.add( end );
                    }
                }
            }
        }
    }
    /**
     * 获取当前指定路径的长度
     * @param start
     * @param end
     * @return
     */
    private int startTo(int end) {
        int pathLen = 0;
        List<Integer> pathList = pathListMap.get(end);
        for(int i=0;i< pathList.size()-1;i++){
            pathLen += graph[pathList.get(i)][pathList.get(i+1)];
        }
        return pathLen;
    }
    /**
     * 直接提取矩阵中的相邻路径。
     * @param start
     * @param end
     * @return
     */
    private int getRawLength(int start, int end) {
        if(end == start){
            return 0;
        }
        return graph[start][end];
    }
    /**
     * 得到指定目标的路劲长度
     * @param end
     * @return
     */
    public int getLength(int end) {
        if(end == start){
            return 0;
        }
        return startTo(end);
    }
    /**
     * 在控制台打印输出所有路径编号
     */
    public void printResult(){
        System.out.println( pathListMap);
    }
    /**
     * 所有路径编号
     * @return
     */
    public Map<Integer, List<Integer>> getPathListMap() {
        return pathListMap;
    }
    /**
     * Test out put
     * @param args
     */
    /*public static void main(String[] args) {

        //int [][] graph = {
        //        { INF,  10, INF,  30, 100},
        //        { INF, INF,  50, INF, INF},
        //        { INF, INF, INF, INF,  10},
        //        { INF, INF,  20, INF,  60},
        //        { INF, INF, INF, INF, INF}};
        int[][] graph={
                {INF, INF, 10 , INF, 30 , 100},
                {INF, INF, 5  , INF, INF, INF},
                {INF, INF, INF, 50 , INF, INF},
                {INF, INF, INF, INF, INF, 10 },
                {INF, INF, INF, 20 , INF, 60 },
                {INF, INF, INF, INF, INF, INF},
        };
        int start = 0;
        int end = 0;
        int length = graph.length;
        for(start=0; start<length; start++){
            System.out.println();
            Dijkstra dijkstra = new Dijkstra(graph, start);
            dijkstra.printResult();
            for(end=0; end<length; end++){
                if(end == start){ continue;}
                int len = dijkstra.getLength( end );
                System.out.println( " Length(" + start + "-"  + end + ") = " +  ( (len == INF)? "Infinity" : len )  );
            }
        }
      */
}
