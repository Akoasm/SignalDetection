package com.example.admin.signaldetection.Signal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.admin.signaldetection.R;

/**
 * Created by admin on 2016/12/5.
 */

public class TwoActivity extends Activity {

    TextView operator_tv,internetType_tv,GPSisopen_tv,level_tv,intensity_tv,current_latitude,current_longitude;
    MyProgressBar progressBar = null;
    String IMSI;
    private ConnectivityManager manager;
    private LocationClient locationClient = null;
    private static final int UPDATE_TIME = 0;
    private static int LOCATION_COUTNS = 0;


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.getInstance().notifyPermissionsChange(permissions,grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.two_activity);
        getview();
        setview();
        getCurrentNetDBM(TwoActivity.this);

    }

    public void getview() {
        operator_tv = (TextView) findViewById(R.id.item_operator);
        internetType_tv = (TextView) findViewById(R.id.item_internetType);
        GPSisopen_tv = (TextView) findViewById(R.id.item_GPSisopen);
        level_tv = (TextView) findViewById(R.id.item_level);
        intensity_tv = (TextView) findViewById(R.id.item_intensity);
        progressBar = (MyProgressBar) findViewById(R.id.item_progressbar);
        current_latitude = (TextView) findViewById(R.id.current_latitude);
        current_longitude = (TextView) findViewById(R.id.current_longitude);


    }




    public void getCurrentNetDBM(Context context) {

        final TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        PhoneStateListener mylistener = new PhoneStateListener(){
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                String signalInfo = signalStrength.toString();
                String[] params = signalInfo.split(" ");
                if(tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                    //4G网络 最佳范围   >-90dBm 越大越好
                    int Itedbm = Integer.parseInt(params[9]);
                    setDBM(Itedbm+""+"dBm");
                    int asu = (Itedbm+113)/2;
                    if(Itedbm < -110){
                        level_tv.setText("差");
                    }else if(Itedbm < -70 && Itedbm > -110){
                        level_tv.setText("良好");
                    }else if(Itedbm > -70){
                        level_tv.setText("优");
                    }
                    progressBar.setProgress(asu);
                }else if(tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA ||
                        tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS){
                    //3G网络最佳范围  >-90dBm  越大越好  ps:中国移动3G获取不到  返回的无效dbm值是正数（85dbm）
                    //在这个范围的已经确定是3G，但不同运营商的3G有不同的获取方法，故在此需做判断 判断运营商与网络类型的工具类在最下方
//                    String yys = getYYS(getApplication());//获取当前运营商
                    String yys = getYYS(IMSI);
                    if (yys=="中国移动") {
                        setDBM(0+"");//中国移动3G不可获取，故在此返回0

                    }else if (yys=="中国联通") {
                        int cdmaDbm = signalStrength.getCdmaDbm();
                        int asu = (cdmaDbm+113)/2;
                        setDBM(cdmaDbm+""+"dBm"+asu);
                        if(cdmaDbm < -110){
                            level_tv.setText("差");
                        }else if(cdmaDbm < -70 && cdmaDbm > -110){
                            level_tv.setText("良好");
                        }else if(cdmaDbm > -70){
                            level_tv.setText("优");
                        }
                        progressBar.setProgress(asu);
                    }else if (yys=="中国电信") {
                        int evdoDbm = signalStrength.getEvdoDbm();
                        setDBM(evdoDbm+""+"dBm");
                        int asu = (evdoDbm+113)/2;
                        if(evdoDbm < -110){
                            level_tv.setText("差");
                        }else if(evdoDbm < -70 && evdoDbm > -110){
                            level_tv.setText("良好");
                        }else if(evdoDbm > -70){
                            level_tv.setText("优");
                        }
                        progressBar.setProgress(asu);
                    }
                }else{
                    //2G网络最佳范围>-90dBm 越大越好
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2*asu;
                    setDBM(dbm+""+"dBm");
                    if(dbm < -110){
                        level_tv.setText("差");
                    }else if(dbm < -70 && dbm > -110){
                        level_tv.setText("良好");
                    }else if(dbm > -70){
                        level_tv.setText("优");
                    }
                    progressBar.setProgress(asu);
                }
            }
        };
        //开始监听
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }
    public void setDBM(String dbm){
        intensity_tv.setText(dbm);

    }

    public String getYYS(String IMSI){
        String ProvidersName=null;
        if (IMSI.startsWith("46000") || IMSI.startsWith("46002")||IMSI.startsWith("46007")) {
            ProvidersName = "中国移动";
        } else if (IMSI.startsWith("46001")) {
            ProvidersName = "中国联通";
        } else if (IMSI.startsWith("46003")) {
            ProvidersName = "中国电信";
        }
        return ProvidersName;
    }
    /**
     * 运营商、GPS、网络类型
     */
    public void setview(){
        //运营商
        SIMCardInfo siminfo = new SIMCardInfo(TwoActivity.this);
        if(siminfo.getProvidersName() == "中国电信"){

        }
        operator_tv.setText(siminfo.getProvidersName());
        //GPS状态
        IntenetUtil intenetUtil = new IntenetUtil();
        boolean isopen = intenetUtil.isOPen(this);
        if(isopen == true){
            GPSisopen_tv.setText("开启");
        }else if(isopen == false){
            GPSisopen_tv.setText("未开启");
        }

        locationClient = new LocationClient(this);
        // 设置定位条件
        final LocationClientOption option = new LocationClientOption();
//        option.setOpenGps(true); // 是否打开GPS
        option.setCoorType("bd09ll"); // 设置返回值的坐标类型。
//        option.setPriority(LocationClientOption.NetWorkFirst); // 设置定位优先级
        // option.setProdName("LocationDemo"); //
        // 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
        option.setScanSpan(UPDATE_TIME);// 设置定时定位的时间间隔。单位毫秒
        locationClient.setLocOption(option);
        locationClient.start();

        // 注册位置监听器
        locationClient.registerLocationListener(new BDLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                IntenetUtil intenetUtil = new IntenetUtil();
                boolean isopen = intenetUtil.isOPen(TwoActivity.this);
                // TODO Auto-generated method stub
                if (location == null) {
                    return;
                }
                if(isopen == true){
                    current_latitude.setText("" + location.getLatitude());
                    Log.i("wj",location.getLatitude()+"");
                    current_longitude.setText("" + location.getLongitude());
                    Log.i("wj",location.getLongitude()+"");
                }

            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(NetworkReceiver, intentFilter);
    }
    /**
     * 发送广播
     */
    private BroadcastReceiver NetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkAvailable()){
                String nettypes = getCurrentNetType(TwoActivity.this);
                internetType_tv.setText(nettypes);
            }
            else {
                Toast.makeText(TwoActivity.this, "请检查网络环境", Toast.LENGTH_SHORT).show();
                internetType_tv.setText("请检查网络环境");
            }
        }
    };

    /**
     * 广播反注册
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(NetworkReceiver);
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
            locationClient = null;
        }
    }

    /**
     * 检测网络是否连接
     */
    private boolean NetworkAvailable() {
        try {
            Thread.sleep(600);
            //得到网络连接信息
            manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                // 获取NetworkInfo对象
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                //去进行判断网络是否连接
                if (networkInfo != null || networkInfo.isAvailable()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 网络已经连接，然后去判断是wifi连接还是mobile连接
     */
    public String getCurrentNetType(Context context) {
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            type = "null";
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            Toast.makeText(this, "当前为网络状态为Wi-Fi", Toast.LENGTH_SHORT).show();
            type = "wifi";
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS
                    || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                Toast.makeText(this, "当前为网络状态为2G", Toast.LENGTH_SHORT).show();
                type = "2g";
            } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                Toast.makeText(this, "当前为网络状态为3g", Toast.LENGTH_SHORT).show();
                type = "3g";
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                Toast.makeText(this, "当前为网络状态为4g", Toast.LENGTH_SHORT).show();
                type = "4g";
            }
        }
        return type;
    }
}
