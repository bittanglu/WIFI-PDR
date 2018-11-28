package com.zhangqie.wifi.getRssi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.zhangqie.wifi.R;
import com.zhangqie.wifi.demo1.WifiAdmin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class GetWifiRssiActivity extends AppCompatActivity implements View.OnClickListener {

    EditText edit_x,edit_y;
    Button button_query,button_upload;
    private ListView listView;
    private List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    SimpleAdapter adapter = null;
    private Handler handler = new Handler();
    ArrayList<RSSI> res = new ArrayList<RSSI>();
    private ListView mlistView;
    protected WifiAdmin mWifiAdmin;
    private List<ScanResult> mWifiList;
    private String filename = "";
    public int level;
    protected String ssid;
    int collection_count = 2; //采集次数
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_wifi_rssi);
        initView();
        try {
            mWifiAdmin = new WifiAdmin(GetWifiRssiActivity.this);
            mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    AlertDialog.Builder alert=new AlertDialog.Builder(GetWifiRssiActivity.this);
                    ssid = mWifiList.get(position).SSID;
                    alert.setTitle(ssid);
                    alert.setMessage("输入密码");
                    final EditText et_password=new EditText(GetWifiRssiActivity.this);
                    final SharedPreferences preferences=getSharedPreferences("wifi_password", Context.MODE_PRIVATE);
                    et_password.setText(preferences.getString(ssid, ""));
                    alert.setView(et_password);
                    //alert.setView(view1);
                    alert.setPositiveButton("连接", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String pw = et_password.getText().toString();
                            if(null == pw  || pw.length() < 8){
                                makeText(GetWifiRssiActivity.this, "密码至少8位", LENGTH_SHORT).show();
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

        //用于UI更新
        class uploadListViewThread extends Thread{
            public void run(){
                Log.d("开始采集","start");
                listView.setAdapter(adapter);
                Log.d("开始结束","end");
            }
        }
    }

    private void initView() {
        findViewById(R.id.query).setOnClickListener(this);

        edit_x = (EditText)findViewById(R.id.location_x);
        edit_y =  (EditText)findViewById(R.id.location_y);

        button_query = (Button)findViewById(R.id.query);
        button_query.setOnClickListener(GetWifiRssiActivity.this);

        button_upload = (Button)findViewById(R.id.upload);
        button_upload.setOnClickListener(GetWifiRssiActivity.this);

        listView = (ListView)findViewById(R.id.list_view);
    }

    //用于采集数据的线程
    class GetRssiThread extends Thread{
        public void run(){
            Log.d("开始采集","start");
            double x = Double.valueOf(edit_x.getText().toString());
            double y = Double.valueOf(edit_y.getText().toString());
            ArrayList<RSSI> temp = new ArrayList<RSSI>();
            for(int i = 0 ;i < collection_count ;i++){
                mWifiAdmin.startScan(GetWifiRssiActivity.this);
                mWifiList = mWifiAdmin.getWifiList();
                RSSI rssi = new RSSI(x,y);
                int ap_num = mWifiList.size();
                for(int j = 0;j < ap_num;j++){
                    ScanResult scanResult = mWifiList.get(j);
                    rssi.addItem(scanResult.BSSID,scanResult.level);
                }
                temp.add(rssi);
                try {
                    Log.d("采集次数",String.valueOf(i+1));
                    sleep(2000);//采集时间间隔为2s
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            RSSI merge = new RSSI(x,y);
            merge.merge(temp);
            Log.d("采集信息",RSSI.toString(merge));
            updata_ListView(merge);
            res.add(merge);
            Log.d("采集结束","end");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.query:
                GetRssiThread scanrssi = new GetRssiThread();
                scanrssi.start();
                break;
            case R.id.upload:
                filename = getApplicationContext().getFilesDir().getAbsolutePath() + "/data.txt";
                RSSI.writeToFile(res,filename);
                Log.d("文件目录",filename);
                res.clear();
                new Thread(uploadurlThread).start();
                try {
                    RSSI.readFromFile(res,filename);
                    for(RSSI temp : res)
                        temp.print();
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case R.id.clear:
                res.clear();
                break;
        }
    }
    public void updata_ListView(RSSI current_rssi){
        int len  = current_rssi.BSSID.size();
        if(list != null) list.clear();
        for (int i = 0;i < len;i++){
            Map<String, String> map = new HashMap<String, String>();
            map.put("BSSID", current_rssi.BSSID.get(i));
            map.put("RSSI", String.valueOf(current_rssi.rssi.get(i)));
            list.add(map);
        }
        adapter = new SimpleAdapter(this, list,
                R.layout.listview_item, new String[] { "BSSID",
                "RSSI" }, new int[] {
                R.id.rssi_bssid,
                R.id.rssi_rssi });
        new Thread(){
            public void run(){
                handler.post(uploadListViewThread);
            }
        }.start();
    }
    //用于采集数据的线程
    Runnable uploadListViewThread  = new  Runnable(){
        @Override
        public void run() {
            //更新界面
            Log.d("开始采集","start");
            listView.setAdapter(adapter);
            Log.d("开始结束","end");
        }
    };
    /**
     * 上传文件到服务器
     * @param context
     * @param uploadUrl     上传服务器地址
     * @param oldFilePath       本地文件路径
     */
    //用于上传数据的线程
    Runnable  uploadurlThread  = new  Runnable (){
        @Override
        public void run(){
            Log.d("数据上传","start");
            String uploadUrl = " https://abe12dd6.ngrok.io/php/index.php";
            String oldFilePath = filename;
            try {
                URL url = new URL(uploadUrl);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();

                // 允许Input、Output，不使用Cache
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);

                con.setConnectTimeout(50000);
                con.setReadTimeout(50000);
                // 设置传送的method=POST
                con.setRequestMethod("POST");
                //在一次TCP连接中可以持续发送多份数据而不会断开连接
                con.setRequestProperty("Connection", "Keep-Alive");
                //设置编码
                con.setRequestProperty("Charset", "UTF-8");
                //text/plain能上传纯文本文件的编码格式
                con.setRequestProperty("Content-Type", "text/plain");

                // 设置DataOutputStream
                DataOutputStream ds = new DataOutputStream(con.getOutputStream());

                // 取得文件的FileInputStream
                FileInputStream fStream = new FileInputStream(oldFilePath);
                // 设置每次写入1024bytes
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];

                int length = -1;
                // 从文件读取数据至缓冲区
                while ((length = fStream.read(buffer)) != -1) {
                    // 将资料写入DataOutputStream中
                    ds.write(buffer, 0, length);
                }
                ds.flush();
                fStream.close();

                InputStream is = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);
                String result = br.readLine();
                Log.d("服务器信息",result);

                ds.close();
                is.close();

                if(con.getResponseCode() == 200){
                    Log.d("文件上传成功！" ,oldFilePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("文件上传失败！" ,oldFilePath);
                Log.d("报错信息toString：",e.toString());
            }
            Log.d("数据上传结束","end");
        }
    };
}
