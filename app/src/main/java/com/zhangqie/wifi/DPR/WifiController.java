package com.zhangqie.wifi.DPR;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.widget.ListView;

import com.zhangqie.wifi.demo1.WifiAdmin;

import java.util.List;

public class WifiController {
    private ListView mlistView;
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    //回调
    public interface WifiCallback{
        void refreshWifi();
    }
    private WifiCallback callback;

    public WifiController(WifiCallback callback){
        this.callback = callback;
    }


}
