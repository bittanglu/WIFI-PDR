package com.zhangqie.wifi.DPR;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangqie.wifi.R;
import com.zhangqie.wifi.demo1.WifiAdmin;
import com.zhangqie.wifi.getRssi.RSSI;
import com.zhangqie.wifi.getRssi.location;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class PDRActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener {

    //行人航位推算所需变量定义
    private final String TAG = "PDRActivity";
    private final double directiont_error = 0.0;
    private String[] direct = new String[3];
    private SensorManager mSensorManager;
    private Sensor stepCounter;
    private Sensor aSensor;
    private Sensor mSensor;
    private TextView Azimuth,Pithch,Roll,Step;
    //PDR行人推算辅助类定义
    private Step_auxiliary loaction_auxiliary = new Step_auxiliary();
    //定义18个方向的数组
    float[] pdr_direction = new float[16];

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    float mSteps0 = 0,mSteps1 = 0;
    double pre_direct =  173;  //x轴方向
    double current_direct = 0.0;
    double x = 0.0,y = 0.0,dx = 0.0,dy = 0.0;
    double step_length = 0.75;

    //定义指纹定位所需变量
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    protected String ssid;
    private ListView mlistView;
    //存储指纹库变量
    ArrayList<RSSI> database = new ArrayList<RSSI>();
    //内部存储位置
    String data_filename = "";
    String pdr_data = "";
    String rssi_data = "";
    //服务器存储位置
    String urlStr = "https://8c190cc6.ngrok.io";
    //记录PDR行走过程
    ArrayList<point> pdr_list = new ArrayList<point>();
    String record = "";
    //记录RSSI采集信息
    ArrayList<RSSI> rssi_list = new ArrayList<RSSI>();
    //融合算法辅助类以及变量
    pdr_rssi res_cal = new pdr_rssi();
    ArrayList<location> location_res;
    //记录角度值
    float[] angle_matrix =  new float[100];
    int angle_index = 0;
    StepController Step_Controller;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdr);
        initView();
        //initSensnr();
        addBasePedometerListener();
        thread.start();
        //加载数据库
        //initDataBase();
    }
    private void addBasePedometerListener() {
        StepController.StepCallback callback=new StepController.StepCallback() {
            @Override
            public void refreshStep(int step, float stepLength, float distance) {
                Log.d(TAG,"catchStep");
                thread.start();
            }
        };
        //初始化
        Step_Controller =  new StepController(callback);
    }
    private void initDataBase() {
        data_filename = getApplicationContext().getFilesDir().getAbsolutePath() + "/database.txt";
        Log.d("文件目录",data_filename);
        File file = new File(data_filename);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            //数据库存在
            try {
                Log.d(TAG,"数据库存在");
                RSSI.readFromFile(database,data_filename);
                Log.d(TAG,"数据库信息:");
                for(RSSI temp : database) {
                    Log.d(TAG,temp.toString());
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }else{
            //数据库不存在，服务器获取数据
            try {
                Log.d(TAG,"本地数据库不存在，网络获取中...");
                String data_str = NetTool.readTxtFile( urlStr+"/php/database.txt","utf-8");
                Log.d(TAG,data_str);
                RSSI.writeToFile(data_str,data_filename);
                RSSI.readFromFile(database,data_filename);
                Log.d(TAG,"数据库下载完成");
                Log.d(TAG,"数据库信息:");
                for(RSSI temp : database) {
                    Log.d(TAG,temp.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //判断文件是否存在
    public boolean fileIsExists(String strFile)
    {
        try {
            File f=new File(strFile);
            if(!f.exists()){
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    //传感器初始化
    private void initSensnr() {
        //获取SensorManager管理器实例
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // getSensorList用于列出设备支持的所有sensor列表
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG,"Sensor size:"+sensorList.size());
        for (Sensor sensor : sensorList) {
            Log.i(TAG,"Supported Sensor: "+sensor.getName());
        }
        int versionCodes = Build.VERSION.SDK_INT;//取得SDK版本

        // 获取计步器sensor
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(stepCounter != null){
            // 如果sensor找到，则注册监听器
            mSensorManager.registerListener((SensorEventListener) this,stepCounter,10000);
        }else{
            Log.e(TAG,"no step counter sensor found");
        }
        //方位监测
        mSensorManager.registerListener((SensorEventListener) this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener((SensorEventListener) this, mSensor,SensorManager.SENSOR_DELAY_NORMAL);

        //更新显示数据的方法
        calculateOrientation();
        try {
            mWifiAdmin = new WifiAdmin(PDRActivity.this);
            mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    AlertDialog.Builder alert=new AlertDialog.Builder(PDRActivity.this);
                    ssid = mWifiList.get(position).SSID;
                    alert.setTitle(ssid);
                    alert.setMessage("输入密码");
                    final EditText et_password=new EditText(PDRActivity.this);
                    final SharedPreferences preferences=getSharedPreferences("wifi_password", Context.MODE_PRIVATE);
                    et_password.setText(preferences.getString(ssid, ""));
                    alert.setView(et_password);
                    //alert.setView(view1);
                    alert.setPositiveButton("连接", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pw = et_password.getText().toString();
                            if(null == pw  || pw.length() < 8){
                                makeText(PDRActivity.this, "密码至少8位", LENGTH_SHORT).show();
                                return;
                            }
                            SharedPreferences.Editor editor=preferences.edit();
                            editor.putString(ssid, pw);   //保存密码
                            editor.commit();
                            mWifiAdmin.addNetwork(mWifiAdmin.CreateWifiInfo(ssid, et_password.getText().toString(), 3));
                        }
                    });
                    alert.setNegativeButton("取消", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //删除
                            //mWifiAdmin.removeWifi(mWifiAdmin.getNetworkId());
                        }
                    });
                    alert.create();
                    alert.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //扫描当前位置的无线信息
    protected RSSI scanner_rssi(){
        mWifiAdmin.startScan(PDRActivity.this);
        mWifiList = mWifiAdmin.getWifiList();
        RSSI rssi = new RSSI(x,y);
        int ap_num = mWifiList.size();
        for(int j = 0;j < ap_num;j++){
            ScanResult scanResult = mWifiList.get(j);
            rssi.addItem(scanResult.BSSID,scanResult.level);
        }
        return rssi;
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            //msg.what == 1，进行显示的更新
            if(msg.what == 1){
                Azimuth.setText("当前方向:" + current_direct);
                Pithch.setText("与X轴夹角:" + Double.toString(current_direct - pre_direct));
                Roll.setText("移动距离:" + "(" + dx + "," + dy + ")");
                String temp = "移动步数:" + Step_Controller.getStep()+" ";
                //temp += "当前坐标（" + x + "," + y + ")";
                Step.setText(temp);
            }
        }
    };
    final Thread thread = new Thread(new Runnable(){
        @Override
        public void run() {
            Message message = new Message();
            message.what = 1;
            handler.sendMessage(message);
        }
    });

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void initView(){
        //显示方向角数据
        Azimuth = (TextView)findViewById(R.id.Azimuth);
        Pithch = (TextView)findViewById(R.id.Pithch);
        Roll = (TextView)findViewById(R.id.Roll);
        Step = (TextView)findViewById(R.id.Step);
        //用于测试，显示打印当前步数
        Step.setText("当前步数:" + mSteps0);

        Button getstep = (Button) findViewById(R.id.getstep);
        getstep.setOnClickListener(this);
        Button start_pdr = (Button) findViewById(R.id.start) ;
        start_pdr.setOnClickListener(this);
        Button end_pdr = (Button) findViewById(R.id.end);
        end_pdr.setOnClickListener(this);
        Button detele = (Button) findViewById(R.id.delete);
        detele.setOnClickListener(this);
        //数据初始化
        int index = 0;
        double min = Double.MAX_VALUE;
        for(int i = 0;i < 16;i++) {
            pdr_direction[i] = (float) (24 * i - 180);
            if(min > Math.abs(pdr_direction[i] - 173)) {
                min = Math.abs(pdr_direction[i] - 173);
                index = i;
            }
        }
        pre_direct = pdr_direction[index];
    }
    // 实现SensorEventListener回调接口，在sensor改变时，会回调该接口
    // 并将结果通过event回传给app处理
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticFieldValues = event.values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            accelerometerValues = event.values;
            Step_Controller.refreshAcc(accelerometerValues,System.currentTimeMillis());
        }

        RSSI get_rssi = scanner_rssi();
        calculateOrientation();
        angle_index = (angle_index+1)%100;
        angle_matrix[angle_index] = (float) current_direct;
        //步数更新
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR)
        {
            Step_Controller.addStep();
            pdr_list.add(new point(getSmoothAngle(5),Step_Controller.getLength()));
            if (mSteps0 == 0)
            {
                mSteps0 = event.values[0];
                mSteps1 = event.values[0];
            }
            else {
                mSteps1 = event.values[0];
            }
            //pre_direct = current_direct;
            //current_direct = Double.valueOf(direct[0]);

            //位置更新
            if (mSteps1 != mSteps0) {
                dx = step_length * Math.cos(current_direct - pre_direct);
                dy = step_length * Math.sin(current_direct - pre_direct);
                x += dx;
                y += dy;
            }
            //calFinalStation();
            //界面更新
            thread.start();
            Log.i(TAG,"Detected step changes:"+event.values[0]);
        }
    }

    private double getSmoothAngle(int count){
        double[] angle = new double[count];
        int k = 0;
        int most_num = 0;
        int i;
        if(angle_index < count - 1){
            for(i = 99;i > 100 - count + angle_index;i--){
                angle[k++] = angle_matrix[i];
                if(angle_matrix[i] > 145 || angle_matrix[i] < -145)
                    most_num++;
            }
            for(i = 0;i <= angle_index;i++) {
                angle[k++] = angle_matrix[i];
                if(angle_matrix[i] > 145 || angle_matrix[i] < -145)
                    most_num++;
            }
        }else{
            for(i = angle_index-count+1;i <= angle_index;i++) {
                angle[k++] = angle_matrix[i];
                if(angle_matrix[i] > 145 || angle_matrix[i] < -145)
                    most_num++;
            }
        }
        if(most_num > count/2){
            for(i = 0;i<count;i++){
                if(angle[i] > 145 || angle[i] < -145)
                    angle[i] +=160;
            }
        }
        double angle_sum = 0.0;
        for(i = 0 ;i < count;i++) {
            angle_sum += angle[i];
        }
        return angle_sum/count;
    }

    public void calFinalStation(){
        RSSI rssi = scanner_rssi();
        rssi_list.add(rssi);
        /***
         * 进行一次计算
         *
        ArrayList<Double> p_wifi = new ArrayList<Double>();
        ArrayList<Double> p_fianl = new ArrayList<Double>();
        res_cal.setPonitPdr(x,y);
        res_cal.setAnglePdr(current_direct - pre_direct);
        res_cal.setDistancePdr(step_length);
        //通过数据库database，计算出相似节点列表res_cal.location_list以及相似度p_wifi
        rssi.EWKNN(database,p_wifi,res_cal.location_list);
        //计算角度和距离差
        res_cal.calAngleList();
        res_cal.calDistanceList();
        //计算概率
        res_cal.calProbability(res_cal.angle_list,res_cal.probability_angle);
        res_cal.calProbability(res_cal.distance_error_rssi,res_cal.probability_distance);
        //权重融合
        res_cal.weightNormalization(p_wifi,res_cal.probability_distance,res_cal.probability_angle,p_fianl,0.8);
        //用最终的数据计算出融合定位结果
        res_cal.calFinalPoint(p_fianl,res_cal.location_list);
        //清空数据，准备进行下一次的计算，将定位结果作为初始位置
        res_cal.clearAll();
        x = res_cal.point_final.getX();
        y = res_cal.point_final.getY();
        //定位位置信息显示
        thread.start();
         */
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Log.i(TAG,"onAccuracyChanged");
    }

    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    protected void onPause() {
        // if unregister this hardware will not detected the step changes
        // mSensorManager.unregisterListener(this);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.getstep:
                //pdr_list.add(new point((current_direct - pre_direct),step_length));
                //calFinalStation();
                Log.i("步行总数", String.valueOf(mSteps1 - mSteps0));
                break;
            case R.id.start:
                //注册传感器以及数据库获取
                initSensnr();
                //initDataBase();
                /**
                //通过WIFI指纹定位初始化位置
                RSSI rssi = scanner_rssi();
                //通过数据库database，计算出相似节点列表res_cal.location_list以及相似度p_wifi
                rssi.EWKNN(database,new ArrayList<Double>(),new ArrayList<location>());
                x = rssi.getX();
                y = rssi.getY();
                 **/
                //显示位置数据
                thread.start();
                break;
            case R.id.end:
                //解除注册
                mSensorManager.unregisterListener((SensorEventListener) this);
                //数据上传
                pdr_data = getApplicationContext().getFilesDir().getAbsolutePath() + "/pdr_data"+".txt";
                rssi_data = getApplicationContext().getFilesDir().getAbsolutePath() + "/rssi_data"+".txt";
                if(pdr_list.size() > 0) point.writeToFile(pdr_list,pdr_data);
                if(rssi_list.size() > 0) RSSI.writeToFile(rssi_list,rssi_data);
                try {
                    //数据传输不能在主线程中进行，通过子进程实现
                    new Thread(uploadurlThread).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rssi_list.clear();
                pdr_list.clear();
                break;

            case R.id.delete:
                File file = new File(data_filename);
                // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
                if (file.exists() && file.isFile()) {
                    if (file.delete()) {
                        Log.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + data_filename + "成功！");
                    } else {
                        Toast.makeText(getApplicationContext(), "删除单个文件" + data_filename + "失败！", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "删除单个文件失败：" + data_filename + "不存在！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    //用于上传数据的线程
    Runnable  uploadurlThread  = new  Runnable (){
        @Override
        public void run(){
            Log.d("数据上传","start");
            try {
                NetTool.sendFile(urlStr + "/php/pdr/index.php",pdr_data);
                NetTool.sendFile(urlStr + "/php/index.php",rssi_data);
                Log.d(TAG,"上传数据成功");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    //计算方位
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void calculateOrientation() {
        //方位计算，0表示正北，180正南，90东，270西
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        current_direct = values[0];
        Log.i(TAG, "角度信息：" + values[0]+"");
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);
        //if(values[2] < 0) values[2] += 360;
        /**
        direct[0] =  String.valueOf(values[0]);
        direct[1] =  String.valueOf(values[1]);
        direct[2] =  String.valueOf(values[2]);
        **/
        //更新UI界面
        thread.start();
        //方向为16个方向中的一个
        int index = 0;
        double min = Double.MAX_VALUE;
        for(int i = 0;i < 16;i++) {
            if(min > Math.abs(pdr_direction[i] - current_direct)) {
                min = Math.abs(pdr_direction[i] - current_direct);
                index = i;
            }
        }
        //current_direct = pdr_direction[index];
        /*
        if(values[0] >= -22.5 && values[0] < 22.5){
            Log.i(TAG, "正北");
            current_direct = 0;
        }
        else if(values[0] >= 22.5 && values[0] < 67.5){
            Log.i(TAG, "东北");
            current_direct = 45;
        }
        else if(values[0] >= 67.5 && values[0] <= 112.5){
            Log.i(TAG, "正东");
            current_direct = 90;
        }
        else if(values[0] >= 112.5 && values[0] < 157.5){
            Log.i(TAG, "东南");
            current_direct = 135;
        }
        else if((values[0] >= 157.5 || values[0] <= -157.5)){
            Log.i(TAG, "正南");
            current_direct = 180;//或者-180
        }
        else if(values[0] >= -157.5 && values[0] <= -112.5){
            Log.i(TAG, "西南");
            current_direct = -135;
        }
        else if(values[0] >= -112.5 && values[0] < -67.5){
            Log.i(TAG, "正西");
            current_direct = -90;
        }
        else if(values[0] >= -67.5 && values[0] < -22.5){
            Log.i(TAG, "西北");
            current_direct = -45;
        }
        */
    }
}

