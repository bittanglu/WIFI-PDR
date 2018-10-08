package com.zhangqie.wifi.getRssi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhangqie.wifi.R;

public class GetWifiRssiActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_wifi_rssi);
        initView();
    }

    private void initView() {
        findViewById(R.id.query).setOnClickListener(this);
    }
 
    @Override
    public void onClick(View v) {

    }
}
