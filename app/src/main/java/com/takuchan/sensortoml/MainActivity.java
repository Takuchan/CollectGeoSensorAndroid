package com.takuchan.sensortoml;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView textView;
    LineChart acclelerometerChart,gyroscopeChart,linear_accChart,orientationChart,rotationChart;
    private FloatingActionButton fab;
    private ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> orientationList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> rotationList = new ArrayList<GetSensorValueModel>();


    private Boolean measureToggle = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the TextView
        textView = findViewById(R.id.textView3);
        fab = findViewById(R.id.floatingActionButton);
        acclelerometerChart = findViewById(R.id.ACCELEROMETERchart);
        gyroscopeChart = findViewById(R.id.GYROSCOPEchart);
        linear_accChart = findViewById(R.id.LINEAR_ACCELERATIONchart);
        orientationChart = findViewById(R.id.ORIENTATIONchart);
        rotationChart = findViewById(R.id.ROTATION_VECTORchart);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LineData lineData = ReadyGraphData(accelerometerList);
                acclelerometerChart.setData(lineData);
                acclelerometerChart.invalidate();
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
        Sensor accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
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

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            String strTmp = "加速度センサー\n"
                    + " X: " + sensorX + "\n"
                    + " Y: " + sensorY + "\n"
                    + " Z: " + sensorZ;
            textView.setText(strTmp);

            accelerometerList.add(new GetSensorValueModel(sensorX,sensorY,sensorZ));
            showInfo(event);
        }
    }

    // （お好みで）加速度センサーの各種情報を表示
    private void showInfo(SensorEvent event){
        // センサー名
        StringBuffer info = new StringBuffer("Name: ");
        info.append(event.sensor.getName());
        info.append("\n");

        // ベンダー名
        info.append("Vendor: ");
        info.append(event.sensor.getVendor());
        info.append("\n");

        // 型番
        info.append("Type: ");
        info.append(event.sensor.getType());
        info.append("\n");

        // 最小遅れ
        int data = event.sensor.getMinDelay();
        info.append("Mindelay: ");
        info.append(data);
        info.append(" usec\n");

        // 最大遅れ
        data = event.sensor.getMaxDelay();
        info.append("Maxdelay: ");
        info.append(data);
        info.append(" usec\n");

        // レポートモード
        data = event.sensor.getReportingMode();
        String stinfo = "unknown";
        if(data == 0){
            stinfo = "REPORTING_MODE_CONTINUOUS";
        }else if(data == 1){
            stinfo = "REPORTING_MODE_ON_CHANGE";
        }else if(data == 2){
            stinfo = "REPORTING_MODE_ONE_SHOT";
        }
        info.append("ReportingMode: ");
        info.append(stinfo);
        info.append("\n");

        // 最大レンジ
        info.append("MaxRange: ");
        float fData = event.sensor.getMaximumRange();
        info.append(fData);
        info.append("\n");

        // 分解能
        info.append("Resolution: ");
        fData = event.sensor.getResolution();
        info.append(fData);
        info.append(" m/s^2\n");

        // 消費電流
        info.append("Power: ");
        fData = event.sensor.getPower();
        info.append(fData);
        info.append(" mA\n");

//        Log.d("センサーの情報",String.valueOf(info));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}