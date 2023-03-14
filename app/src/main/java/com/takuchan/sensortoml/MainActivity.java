package com.takuchan.sensortoml;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.takuchan.database.sampledata.GetSensorValueDatabase;
import com.takuchan.database.sampledata.SensorListDatabase;
import com.takuchan.database.sampledata.SensorValueDatabase;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Button exportButton,startButton;
    private ViewPager2 viewPager2;
    public static TextView countDownText;

    private FloatingActionButton fab;
    private ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> rotationList = new ArrayList<GetSensorValueModel>();
    private FragmentStateAdapter pagerAdapter;
    private boolean measureToggle = false;
    private boolean startToggle = false;

    private long countNumber = 4000; // 3秒x 1000 mms
    private long interval = 10;

    private static final int NUM_PAGES = 5;

    Realm realm;

    public static long nowDatabasePrimarykey = -1;

    private GetSensorValueDatabase getSensorValueDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the TextView
        fab = findViewById(R.id.floatingActionButton);
        startButton = findViewById(R.id.button2);
        exportButton = findViewById(R.id.button);
        countDownText = findViewById(R.id.countdownText);
        viewPager2 = findViewById(R.id.pager2);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        fab.setVisibility(View.GONE);
        countDownText.setVisibility(View.GONE);

        //タイマーのインスタンスを作成
        final CountDown countDown = new CountDown(countNumber,interval);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startToggle){
                    countDown.cancel();
                    startButton.setText("計測開始");
                    fab.setVisibility(View.GONE);
                    countDownText.setVisibility(View.GONE);
                    sensorManager.unregisterListener(MainActivity.this);
                }else{
                    if(nowDatabasePrimarykey == -1){
                        //データベースを新規作成

                        Number maxId = realm.where(GetSensorValueDatabase.class).max("id");
                        long nextId = 1;
                        if(maxId != null) nextId = maxId.longValue() + 1;
                        long finalNextId = nextId;
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                getSensorValueDatabase
                                        = realm.createObject(GetSensorValueDatabase.class,new Long(finalNextId));
                                getSensorValueDatabase.examName = "いいね";
                            }
                        });

                        nowDatabasePrimarykey = nextId;
                    }
                    countDown.start();
                    countDownText.setVisibility(View.VISIBLE);
                    startButton.setText("計測停止");
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fab.setVisibility(View.VISIBLE);
                            List<Sensor> sensors = new ArrayList<>();
                            sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
                            if(sensors.size() > 0){
                                for (int i = 0; i < sensors.size()-1 ; i++){
                                    Sensor s = sensors.get(i);
                                    sensorManager.registerListener(MainActivity.this,s,SensorManager.SENSOR_DELAY_FASTEST);
                                }
                            }
                        }
                    },countNumber);

                }
                startToggle = !startToggle;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                保存される側のMaxIDも見つけてみよう！
                GetSensorValueDatabase getSensorValueDatabase =
                        realm.where(GetSensorValueDatabase.class).equalTo("id",nowDatabasePrimarykey).findFirst();
                Log.d("getsensorValueDatabase",String.valueOf(getSensorValueDatabase));
                Number maxId = realm.where(SensorListDatabase.class).max("id");
                long nextId = 1;
                if(maxId != null) nextId = maxId.longValue() + 1;
//                SensorListDatabase sensorListDatabase
//                        = realm.createObject(SensorListDatabase.class,new Long(nextId));
                for(GetSensorValueModel model : accelerometerList){
                    SensorValueDatabase sensorValueDatabase = new SensorValueDatabase();
                    sensorValueDatabase.x = model.getXValue();
                    sensorValueDatabase.y = model.getYValue();
                    sensorValueDatabase.z = model.getZValue();
                    SensorListDatabase sensorListDatabase = new SensorListDatabase();
                    sensorListDatabase.sensorValueDatabases.add(sensorValueDatabase);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            getSensorValueDatabase.accelerometerList.add(sensorListDatabase);
                        }
                    });
                }
//                getSensorValueDatabase.accelerometerList.add(sensorListDatabase);
//
//                SensorListDatabase sensorListDatabase2
//                        = realm.createObject(SensorListDatabase.class,new Long(nextId));
//                for(GetSensorValueModel model : gyroscopeList){
//                    SensorValueDatabase sensorValueDatabase = new SensorValueDatabase();
//                    sensorValueDatabase.x = model.getXValue();
//                    sensorValueDatabase.y = model.getYValue();
//                    sensorValueDatabase.z = model.getZValue();
//                    sensorListDatabase2.sensorValueDatabases.add(sensorValueDatabase);
//                }
//                getSensorValueDatabase.gyroscopeList.add(sensorListDatabase);
//
//                SensorListDatabase sensorListDatabase3
//                        = realm.createObject(SensorListDatabase.class,new Long(nextId));
//                for(GetSensorValueModel model : linearAcceleList){
//                    SensorValueDatabase sensorValueDatabase = new SensorValueDatabase();
//                    sensorValueDatabase.x = model.getXValue();
//                    sensorValueDatabase.y = model.getYValue();
//                    sensorValueDatabase.z = model.getZValue();
//                    sensorListDatabase3.sensorValueDatabases.add(sensorValueDatabase);
//                }
//                getSensorValueDatabase.linearaccleList.add(sensorListDatabase);
//                SensorListDatabase sensorListDatabase4
//                        = realm.createObject(SensorListDatabase.class,new Long(nextId));
//                for(GetSensorValueModel model : rotationList){
//                    SensorValueDatabase sensorValueDatabase = new SensorValueDatabase();
//                    sensorValueDatabase.x = model.getXValue();
//                    sensorValueDatabase.y = model.getYValue();
//                    sensorValueDatabase.z = model.getZValue();
//                    sensorListDatabase4.sensorValueDatabases.add(sensorValueDatabase);
//                }
//                getSensorValueDatabase.rotationList.add(sensorListDatabase);

                accelerometerList.clear();
                gyroscopeList.clear();
                linearAcceleList.clear();
                rotationList.clear();
                measureToggle = !measureToggle;
            }
        });

    }


    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float sensorX, sensorY, sensorZ;
        float gyroX,gyroY,gyroZ;
        float linerX,linerY,linerZ;
        float rotationX,rotationY,rotationZ;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];
            accelerometerList.add(new GetSensorValueModel(sensorX,sensorY,sensorZ));
        }else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroX = event.values[0];
            gyroY = event.values[1];
            gyroZ = event.values[2];
            gyroscopeList.add(new GetSensorValueModel(gyroX,gyroY,gyroZ));
        }else if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            linerX = event.values[0];
            linerY = event.values[1];
            linerZ = event.values[2];
            linearAcceleList.add(new GetSensorValueModel(linerX,linerY,linerZ));
        }else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            rotationX = event.values[0];
            rotationY = event.values[1];
            rotationZ = event.values[2];
            rotationList.add(new GetSensorValueModel(rotationX,rotationY,rotationZ));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter{
        public ScreenSlidePagerAdapter(FragmentActivity fa){
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<>();
            ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<>();
            ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<>();
            ArrayList<GetSensorValueModel> rotationList = new ArrayList<>();

            RealmResults<GetSensorValueDatabase> realmResults = realm.where(GetSensorValueDatabase.class)
                    .equalTo("id", nowDatabasePrimarykey)
                    .findAll();

            Log.d("今のページ番号は",String.valueOf(position));
            if(!realmResults.isEmpty()) {
                GetSensorValueDatabase getSensorValueDatabase =
                        realm.where(GetSensorValueDatabase.class).equalTo("id", nowDatabasePrimarykey).findFirst();
                //加速度センサー
                accelerometerList.clear();
                RealmList<SensorListDatabase> sensorListDatabaseRealmList = getSensorValueDatabase.accelerometerList;
                SensorListDatabase sensorListDatabase1 = sensorListDatabaseRealmList.get(position);
                RealmList<SensorValueDatabase> sensorValueDatabaseRealmList = sensorListDatabase1.sensorValueDatabases;
                for (SensorValueDatabase sensorValue : sensorValueDatabaseRealmList) {
                    accelerometerList.add(new GetSensorValueModel(sensorValue.x, sensorValue.y, sensorValue.z));
                }
            }
//                //ジャイロスコープセンサー
//                gyroscopeList.clear();
//                RealmList<SensorListDatabase> sensorListDatabaseRealmList2 = getSensorValueDatabase.gyroscopeList;
//                SensorListDatabase sensorListDatabase2 = sensorListDatabaseRealmList2.get(position);
//                RealmList<SensorValueDatabase> sensorValueDatabaseRealmList2 = sensorListDatabase2.sensorValueDatabases;
//                for(SensorValueDatabase sensorValue: sensorValueDatabaseRealmList2){
//                    accelerometerList.add(new GetSensorValueModel(sensorValue.x,sensorValue.y,sensorValue.z));
//                }
//                //直線加速度センサー
//                linearAcceleList.clear();
//                RealmList<SensorListDatabase> sensorListDatabaseRealmList3 = getSensorValueDatabase.linearaccleList;
//                SensorListDatabase sensorListDatabase3 = sensorListDatabaseRealmList3.get(position);
//                RealmList<SensorValueDatabase> sensorValueDatabaseRealmList3 = sensorListDatabase3.sensorValueDatabases;
//                for(SensorValueDatabase sensorValue: sensorValueDatabaseRealmList3){
//                    accelerometerList.add(new GetSensorValueModel(sensorValue.x,sensorValue.y,sensorValue.z));
//                }
//                //回転速度センサー
//                rotationList.clear();
//                RealmList<SensorListDatabase> sensorListDatabaseRealmList4 = getSensorValueDatabase.rotationList;
//                SensorListDatabase sensorListDatabase4 = sensorListDatabaseRealmList4.get(position);
//                RealmList<SensorValueDatabase> sensorValueDatabaseRealmList4 = sensorListDatabase4.sensorValueDatabases;
//                for(SensorValueDatabase sensorValue: sensorValueDatabaseRealmList4){
//                    accelerometerList.add(new GetSensorValueModel(sensorValue.x,sensorValue.y,sensorValue.z));
//                }
//            }


            return new ScreenSlidePageFragment(accelerometerList,gyroscopeList,linearAcceleList,rotationList);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}