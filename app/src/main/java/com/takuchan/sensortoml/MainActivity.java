package com.takuchan.sensortoml;


import androidx.appcompat.app.AppCompatActivity;


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
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.takuchan.database.sampledata.GetSensorValueDatabase;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Button exportButton,startButton;
    public static TextView countDownText;

    private FloatingActionButton fab,fab2;
    private ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> rotationList = new ArrayList<GetSensorValueModel>();
    private ArrayList<Integer> indexaccleList = new ArrayList<Integer>();
    private ArrayList<Integer> indexlinearList = new ArrayList<Integer>();

    private boolean startToggle = false;
    private long countNumber = 1000; // 3秒x 1000 mms
    private long refreshChartNumber = 1000; // 1秒
    private long interval = 10;
    Realm realm;
    public static long nowDatabasePrimarykey = -1;
    private Sensor accelerometer,gyroscope,linearacc,rotation;

    private GetSensorValueDatabase getSensorValueDatabase;
    private Boolean isReady = false;
    LineChart acclelerometerChart,gyroscopeChart,linear_accChart,rotationChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Get an instance of the TextView
        fab = findViewById(R.id.floatingActionButton);
        fab2 = findViewById(R.id.floatingActionButton3);
        startButton = findViewById(R.id.button2);
        exportButton = findViewById(R.id.button);
        countDownText = findViewById(R.id.countdownText);
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        fab.setVisibility(View.GONE);
        countDownText.setVisibility(View.GONE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linearacc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        acclelerometerChart = findViewById(R.id.ACCELEROMETERchart);
        gyroscopeChart = findViewById(R.id.GYROSCOPEchart);
        linear_accChart = findViewById(R.id.LINEAR_ACCELERATIONchart);
        rotationChart = findViewById(R.id.ROTATION_VECTORchart);


        //タイマーのインスタンスを作成
        final CountDown countDown = new CountDown(countNumber,interval);

        Timer timer = new Timer(false);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {

            }
        };
        timer.schedule(task,1000,refreshChartNumber);

        //計測開始ボタンが押された
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startToggle){
                    isReady = false;
                    timer.cancel();
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
                            sensorManager.registerListener(MainActivity.this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
                            sensorManager.registerListener(MainActivity.this,gyroscope,SensorManager.SENSOR_DELAY_FASTEST);
                            sensorManager.registerListener(MainActivity.this,linearacc,SensorManager.SENSOR_DELAY_FASTEST);
                            sensorManager.registerListener(MainActivity.this,rotation,SensorManager.SENSOR_DELAY_FASTEST);
                        }
                    },countNumber);

                }
                startToggle = !startToggle;
            }
        });

        //右下のボタンが押された
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("加速度", String.valueOf(accelerometerList.size()));
                Log.d("ジャイロ",String.valueOf(gyroscopeList.size()));
                Log.d("直線加速度",String.valueOf(linearAcceleList.size()));
                Log.d("回転加速度", String.valueOf(rotationList.size()));
                LineData accelerometerData = ReadyGraphData(accelerometerList);
                indexaccleList.add(accelerometerList.size());
                indexlinearList.add(linearAcceleList.size());
                //LimitLineを設定
                XAxis xAxis = acclelerometerChart.getXAxis();
                for (int i = 0; i < indexaccleList.size(); i++){
                    LimitLine limitLine = ReadyGraphLimit(indexaccleList.get(i));
                    xAxis.addLimitLine(limitLine);
                }
                acclelerometerChart.setData(accelerometerData);
                acclelerometerChart.notifyDataSetChanged();
                acclelerometerChart.invalidate();
                acclelerometerChart.setVisibleXRangeMaximum(120);
                acclelerometerChart.moveViewToX(accelerometerData.getEntryCount());
                LineData gyroData = ReadyGraphData(gyroscopeList);
                gyroscopeChart.setData(gyroData);
                gyroscopeChart.notifyDataSetChanged();
                gyroscopeChart.invalidate();
                gyroscopeChart.setVisibleXRangeMaximum(120);
                gyroscopeChart.moveViewToX(accelerometerData.getEntryCount());
                LineData linearData = ReadyGraphData(linearAcceleList);
                linear_accChart.setData(linearData);
                linear_accChart.notifyDataSetChanged();
                linear_accChart.invalidate();
                linear_accChart.setVisibleXRangeMaximum(120);
                linear_accChart.moveViewToX(accelerometerData.getEntryCount());
                LineData rotationData = ReadyGraphData(rotationList);
                rotationChart.setData(rotationData);
                rotationChart.notifyDataSetChanged();
                rotationChart.invalidate();
                rotationChart.setVisibleXRangeMaximum(120);
                rotationChart.moveViewToX(accelerometerData.getEntryCount());


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

        if(isReady){
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
            }else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                rotationX = event.values[0];
                rotationY = event.values[1];
                rotationZ = event.values[2];
                rotationList.add(new GetSensorValueModel(rotationX, rotationY, rotationZ));
            }

        }else{
            //一番センサーの電源が入るのが遅いジャイロセンサーの起動検知
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                isReady = true;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private static LimitLine ReadyGraphLimit(int xValue){
        LimitLine limitLine = new LimitLine(xValue,"");
        limitLine.setLineColor(Color.parseColor("#f6b894"));
        limitLine.setLineWidth(2);
        limitLine.setTextSize(10f);
        return  limitLine;
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
        set1.setColor(R.color.xAixs);
        set2.setColor(R.color.yAixs);
        set3.setColor(R.color.zAixs);
        set1.setDrawCircles(false);
        set2.setDrawCircles(false);
        set3.setDrawCircles(false);

        LineData lineData = new LineData(set1,set2,set3);
        return lineData;
    }

}

