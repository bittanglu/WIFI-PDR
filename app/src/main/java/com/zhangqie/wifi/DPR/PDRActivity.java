package com.zhangqie.wifi.DPR;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.zhangqie.wifi.R;

import java.util.List;

public class PDRActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener {

    private final String TAG = "PDRActivity";
    SensorManager mSensorManager;
    Sensor stepCounter;
    float mSteps = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdr);
        initView();
    }

    private void initView(){
        //用于测试，显示打印当前步数
        // 获取SensorManager管理器实例
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // getSensorList用于列出设备支持的所有sensor列表
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Log.i(TAG,"Sensor size:"+sensorList.size());
        for (Sensor sensor : sensorList) {
            Log.i(TAG,"Supported Sensor: "+sensor.getName());
        }
        // 获取计步器sensor
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCounter != null){
            // 如果sensor找到，则注册监听器
            mSensorManager.registerListener((SensorEventListener) this,stepCounter,10000);
        }
        else{
            Log.e(TAG,"no step counter sensor found");
        }

        Button getstep = (Button) findViewById(R.id.getstep);
        getstep.setOnClickListener(this);

    }
    // 实现SensorEventListener回调接口，在sensor改变时，会回调该接口
    // 并将结果通过event回传给app处理
    @Override
    public void onSensorChanged(SensorEvent event) {
        mSteps = event.values[0];
        Log.i(TAG,"Detected step changes:"+event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Log.i(TAG,"onAccuracyChanged");
    }

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
                Log.i("步行总数", String.valueOf(mSteps));
                break;
        }
    }
}
