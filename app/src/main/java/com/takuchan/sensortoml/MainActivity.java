package com.takuchan.sensortoml;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Color;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Button exportButton,startButton;
    private ViewPager2 viewPager2;
    public static TextView countDownText;
    LineChart acclelerometerChart,gyroscopeChart,linear_accChart,rotationChart;
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
        acclelerometerChart = findViewById(R.id.ACCELEROMETERchart);
        gyroscopeChart = findViewById(R.id.GYROSCOPEchart);

        viewPager2 = findViewById(R.id.pager2);
        pagerAdapter = new ScreenSlidePagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);

        linear_accChart = findViewById(R.id.LINEAR_ACCELERATIONchart);
        rotationChart = findViewById(R.id.ROTATION_VECTORchart);
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
                LineData accelerometerData = ReadyGraphData(accelerometerList);
                acclelerometerChart.setData(accelerometerData);
                acclelerometerChart.invalidate();
                LineData gyroData = ReadyGraphData(gyroscopeList);
                gyroscopeChart.setData(gyroData);
                gyroscopeChart.invalidate();
                LineData linearData = ReadyGraphData(linearAcceleList);
                linear_accChart.setData(linearData);
                linear_accChart.invalidate();
                LineData rotationData = ReadyGraphData(rotationList);
                rotationChart.setData(rotationData);
                rotationChart.invalidate();
                measureToggle = !measureToggle;
            }
        });

    }

    public static LineData ReadyGraphData(ArrayList<GetSensorValueModel> sensorArrayList){
        ArrayList<Entry> xValues = new ArrayList<>();
        ArrayList<Entry> yValues = new ArrayList<>();
        ArrayList<Entry> zValues = new ArrayList<>();
        int count = 0;
        for (GetSensorValueModel model : sensorArrayList){
            xValues.add(new Entry(count,model.getXValue()));
            yValues.add(new Entry(count,model.getYValue()));
            zValues.add(new Entry(count,model.getZValue()));
            count ++;
        }
        LineDataSet set1 = new LineDataSet(xValues,"X");
        LineDataSet set2 = new LineDataSet(yValues,"Y");
        LineDataSet set3 = new LineDataSet(zValues,"Z");
        set1.setColor(Color.RED);
        set2.setColor(Color.BLUE);
        set3.setColor(Color.GREEN);

        LineData lineData = new LineData(set1,set2,set3);
        return lineData;
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
            return new ScreenSlidePageFragment();
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }
}