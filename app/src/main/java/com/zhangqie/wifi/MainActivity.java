package com.zhangqie.wifi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhangqie.wifi.DPR.PDRActivity;
import com.zhangqie.wifi.DPR.PdrTestActivity;
import com.zhangqie.wifi.demo1.Demo1Activity;
import com.zhangqie.wifi.demo2.Demo2Activity;
import com.zhangqie.wifi.getRssi.GetWifiRssiActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                startActivity(new Intent(MainActivity.this, Demo1Activity.class));
                break;
            case R.id.btn2:
                startActivity(new Intent(MainActivity.this, Demo2Activity.class));
                break;
            case R.id.btn3:
                startActivity(new Intent(MainActivity.this, PDRActivity.class));
                break;
            case R.id.btn4:
                startActivity(new Intent(MainActivity.this, GetWifiRssiActivity.class));
                break;
            case R.id.btn5:
                startActivity(new Intent(MainActivity.this, PdrTestActivity.class));
                break;
        }
    }
}
