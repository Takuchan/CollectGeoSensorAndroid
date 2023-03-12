package com.takuchan.sensortoml;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Button exportButton,startButton;
    LineChart acclelerometerChart,gyroscopeChart,linear_accChart,rotationChart;
    private FloatingActionButton fab;
    private ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> orientationList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> rotationList = new ArrayList<GetSensorValueModel>();


    private boolean measureToggle = false;
    private boolean startToggle = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the TextView
        fab = findViewById(R.id.floatingActionButton);
        startButton = findViewById(R.id.button2);
        exportButton = findViewById(R.id.button);
        acclelerometerChart = findViewById(R.id.ACCELEROMETERchart);
        gyroscopeChart = findViewById(R.id.GYROSCOPEchart);
        linear_accChart = findViewById(R.id.LINEAR_ACCELERATIONchart);
        rotationChart = findViewById(R.id.ROTATION_VECTORchart);

        fab.setVisibility(View.GONE);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startToggle){
                    fab.setVisibility(View.GONE);
                }else{
                    fab.setVisibility(View.VISIBLE);
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
    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        List<Sensor> sensors = new ArrayList<>();
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if(sensors.size() > 0){
            for (int i = 0; i < sensors.size()-1 ; i++){
                Sensor s = sensors.get(i);
                sensorManager.registerListener(this,s,SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
//        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
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

    // （お好みで）加速度センサーの各種情報を表示
//    private void showInfo(SensorEvent event){
//        // センサー名
//        StringBuffer info = new StringBuffer("Name: ");
//        info.append(event.sensor.getName());
//        info.append("\n");
//
//        // ベンダー名
//        info.append("Vendor: ");
//        info.append(event.sensor.getVendor());
//        info.append("\n");
//
//        // 型番
//        info.append("Type: ");
//        info.append(event.sensor.getType());
//        info.append("\n");
//
//        // 最小遅れ
//        int data = event.sensor.getMinDelay();
//        info.append("Mindelay: ");
//        info.append(data);
//        info.append(" usec\n");
//
//        // 最大遅れ
//        data = event.sensor.getMaxDelay();
//        info.append("Maxdelay: ");
//        info.append(data);
//        info.append(" usec\n");
//
//        // レポートモード
//        data = event.sensor.getReportingMode();
//        String stinfo = "unknown";
//        if(data == 0){
//            stinfo = "REPORTING_MODE_CONTINUOUS";
//        }else if(data == 1){
//            stinfo = "REPORTING_MODE_ON_CHANGE";
//        }else if(data == 2){
//            stinfo = "REPORTING_MODE_ONE_SHOT";
//        }
//        info.append("ReportingMode: ");
//        info.append(stinfo);
//        info.append("\n");
//
//        // 最大レンジ
//        info.append("MaxRange: ");
//        float fData = event.sensor.getMaximumRange();
//        info.append(fData);
//        info.append("\n");
//
//        // 分解能
//        info.append("Resolution: ");
//        fData = event.sensor.getResolution();
//        info.append(fData);
//        info.append(" m/s^2\n");
//
//        // 消費電流
//        info.append("Power: ");
//        fData = event.sensor.getPower();
//        info.append(fData);
//        info.append(" mA\n");
//
////        Log.d("センサーの情報",String.valueOf(info));
//    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}