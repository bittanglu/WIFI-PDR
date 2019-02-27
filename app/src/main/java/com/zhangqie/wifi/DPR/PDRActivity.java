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

import com.zhangqie.wifi.R;
import com.zhangqie.wifi.demo1.WifiAdmin;
import com.zhangqie.wifi.getRssi.RSSI;

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

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    float mSteps0 = 0,mSteps1 = 0;
    double pre_direct = 0.0;
    double current_direct = 0.0;
    double x = 0.0,y = 0.0;
    double step_length = 0.537748;

    //定义指纹定位所需变量
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    protected String ssid;
    private ListView mlistView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdr);
        initView();
        initSensnr();
    }
    //传感器初始化
    private void initSensnr() {
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
            if(msg.what == 1){
                Azimuth.setText("Azimuth:" + direct[0]);
                Pithch.setText("Pithch:" + direct[1]);
                Roll.setText("Roll:" + direct[2]);
                String temp = "当前步数:" + (mSteps1 - mSteps0) +" ";
                temp += "当前坐标（" + x + "," + y + ")";
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
        Step.setText("当前步数:" + mSteps0);
        //用于测试，显示打印当前步数
        //获取SensorManager管理器实例
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // getSensorList用于列出设备支持的所有sensor列表
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG,"Sensor size:"+sensorList.size());
        for (Sensor sensor : sensorList) {
            Log.i(TAG,"Supported Sensor: "+sensor.getName());
        }
        // 获取计步器sensor
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(stepCounter != null){
            // 如果sensor找到，则注册监听器
            mSensorManager.registerListener((SensorEventListener) this,stepCounter,10000);
        }else{
            Log.e(TAG,"no step counter sensor found");
        }

        Button getstep = (Button) findViewById(R.id.getstep);
        getstep.setOnClickListener(this);

        //方位监测
        mSensorManager.registerListener((SensorEventListener) this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener((SensorEventListener) this, mSensor,SensorManager.SENSOR_DELAY_NORMAL);

        //更新显示数据的方法
        calculateOrientation();
    }
    // 实现SensorEventListener回调接口，在sensor改变时，会回调该接口
    // 并将结果通过event回传给app处理
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticFieldValues = event.values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = event.values;
        calculateOrientation();
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
        {
            if (mSteps0 == 0)
            {
                mSteps1 = event.values[0];
                mSteps0 = 0;
            }
            else {
                mSteps0 = mSteps1;
                mSteps1 = event.values[0];
            }
            pre_direct = current_direct;
            current_direct = Double.valueOf(direct[0]);

            //位置更新
            x += step_length * Math.sin(current_direct - pre_direct);
            y += step_length * Math.cos(current_direct - pre_direct);
            //界面更新
            thread.start();
            Log.i(TAG,"Detected step changes:"+event.values[0]);
        }
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
                Log.i("步行总数", String.valueOf(mSteps1 - mSteps0));
                break;
        }
    }
    //计算方位
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private void calculateOrientation() {
        //方位计算，0表示正北，180正南，90东，270西
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        Log.i(TAG, values[0]+"");
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);
        if(values[2] < 0) values[2] += 360;
        direct[0] =  String.valueOf(values[0]);
        direct[1] =  String.valueOf(values[1]);
        direct[2] =  String.valueOf(values[2]);

        //更新UI界面
        thread.start();

        if(values[0] >= -5 && values[0] < 5){
            Log.i(TAG, "正北");
        }
        else if(values[0] >= 5 && values[0] < 85){
            Log.i(TAG, "东北");
        }
        else if(values[0] >= 85 && values[0] <=95){
            Log.i(TAG, "正东");
        }
        else if(values[0] >= 95 && values[0] <175){
            Log.i(TAG, "东南");
        }
        else if((values[0] >= 175 && values[0] <= 180)){
            Log.i(TAG, "正南");
        }
        else if(values[0] >= 185 && values[0] <= 265){
            Log.i(TAG, "西南");
        }
        else if(values[0] >= 265 && values[0] < 275){
            Log.i(TAG, "正西");
        }
        else if(values[0] >= 275 && values[0] < 355){
            Log.i(TAG, "西北");
        }
    }
}

