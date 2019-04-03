package com.zhangqie.wifi.DPR;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhangqie.wifi.R;

public class PdrTestActivity extends AppCompatActivity implements View.OnClickListener,SensorEventListener{
    private String TAG = "PdrTestActivity";
    private TextView direct,step_length;
    private Button start,end;
    private SensorManager mSensorManager;
    private Sensor stepCounter;
    private Sensor aSensor;
    private Sensor mSensor;
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];
    StepController Step;
    boolean falg = false;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdr_test);
        init_view();
    }

    private void init_view() {
        direct = (TextView)findViewById(R.id.direction);
        step_length = (TextView)findViewById(R.id.step_length);
        start = (Button)findViewById(R.id.pdr_start_btn);
        end = (Button)findViewById(R.id.pdr_stop_btn);
        start.setOnClickListener(this);
        end.setOnClickListener(this);
    }
    public void onClick(View v){
        switch(v.getId()){
            case R.id.pdr_start_btn:
                //获取SensorManager管理器实例

                mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
                int versionCodes = Build.VERSION.SDK_INT;//取得SDK版本
                addCountStepListener();
                addBasePedometerListener();
                aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                //方位监测
                mSensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);
                mSensorManager.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_NORMAL);
                Log.d(TAG,"pdr_start_btn");
                break;
            case R.id.pdr_stop_btn:

                Log.d(TAG,"pdr_stop_btn");
                break;
        }
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
        Step =  new StepController(callback);
        falg = true;
    }

    private void addCountStepListener() {
        stepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if(stepCounter != null){
            // 如果sensor找到，则注册监听器
            mSensorManager.registerListener((SensorEventListener) this,stepCounter,10000);
        }else{
            Log.e(TAG,"no step counter sensor found");
        }
    }

    // 实现SensorEventListener回调接口，在sensor改变时，会回调该接口
    // 并将结果通过event回传给app处理
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticFieldValues = event.values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
            Step.refreshAcc(accelerometerValues,System.currentTimeMillis());
        }
        if(event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Step.addStep();
            thread.start();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            //msg.what == 1，进行显示的更新
            if(msg.what == 1){
                direct.setText("步数:" + Step.getStep());
                step_length.setText("步长:" + Step.getLength());
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
}
