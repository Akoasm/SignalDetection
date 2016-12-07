package com.example.admin.signaldetection.Signal;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.admin.signaldetection.R;

/**
 * Created by 王小川 on 2016/11/29.
 */
public class MainActivity extends AppCompatActivity {

    TextView operator_tv,internetType_tv,GPSisopen_tv,level_tv,intensity_tv;
    MyProgressBar progressBar = null;
    String IMSI;
    private ConnectivityManager manager;
    public MapView mapView = null;
    public BaiduMap baiduMap = null;

    // 定位相关声明
    public LocationClient locationClient = null;
    //自定义图标
    BitmapDescriptor mCurrentMarker = null;
    boolean isFirstLoc = true;// 是否首次定位


    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mapView == null)
                return;

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);	//设置定位数据
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll,25);	//设置地图中心点以及缩放级别
//				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(u);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detection_main);
        getview();
        setview();
        getCurrentNetDBM(MainActivity.this);
    }

    public void getview() {
        operator_tv = (TextView) findViewById(R.id.item_operator);
        internetType_tv = (TextView) findViewById(R.id.item_internetType);
        GPSisopen_tv = (TextView) findViewById(R.id.item_GPSisopen);
        level_tv = (TextView) findViewById(R.id.item_level);
        intensity_tv = (TextView) findViewById(R.id.item_intensity);
        progressBar = (MyProgressBar) findViewById(R.id.item_progressbar);

        mapView = (MapView) this.findViewById(R.id.bmapView); // 获取地图控件引用
        baiduMap = mapView.getMap();
        //开启定位图层
        baiduMap.setMyLocationEnabled(true);

        locationClient = new LocationClient(getApplicationContext()); // 实例化LocationClient类
        locationClient.registerLocationListener(myListener); // 注册监听函数
        this.setLocationOption();	//设置定位参数
        locationClient.start(); // 开始定位
    }



    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mapView.onPause();
    }

    /**
     * 设置定位参数
     */
    private void setLocationOption() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 返回的定位结果是百度经纬度,默认值gcj02
        option.setScanSpan(5000); // 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(true); // 返回的定位结果包含地址信息
        option.setNeedDeviceDirect(true); // 返回的定位结果包含手机机头的方向

        locationClient.setLocOption(option);
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
        SIMCardInfo siminfo = new SIMCardInfo(MainActivity.this);
        operator_tv.setText(siminfo.getProvidersName());
        //GPS状态
        IntenetUtil intenetUtil = new IntenetUtil();
        boolean isopen = intenetUtil.isOPen(this);
        if(isopen == true){
            GPSisopen_tv.setText("开启");
        }else if(isopen == false){
            GPSisopen_tv.setText("未开启");
        }

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
                String nettypes = getCurrentNetType(MainActivity.this);
                internetType_tv.setText(nettypes);
            }
            else {
                Toast.makeText(MainActivity.this, "请检查网络环境", Toast.LENGTH_SHORT).show();
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
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        // TODO Auto-generated method stub
        super.onDestroy();
        mapView.onDestroy();
        mapView = null;
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
