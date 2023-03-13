package com.takuchan.sensortoml;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class ScreenSlidePageFragment extends Fragment {

    private ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> rotationList = new ArrayList<GetSensorValueModel>();

    LineChart acclelerometerChart,gyroscopeChart,linear_accChart,rotationChart;

    public ScreenSlidePageFragment(ArrayList<GetSensorValueModel> accelerometerList, ArrayList<GetSensorValueModel> gyroscopeList, ArrayList<GetSensorValueModel> linearAcceleList, ArrayList<GetSensorValueModel> rotationList){
        this.accelerometerList = accelerometerList;
        this.gyroscopeList = gyroscopeList;
        this.linearAcceleList = linearAcceleList;
        this.rotationList = rotationList;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_screen_slide_page, container, false);
        acclelerometerChart = view.findViewById(R.id.ACCELEROMETERchart);
        gyroscopeChart = view.findViewById(R.id.GYROSCOPEchart);
        linear_accChart = view.findViewById(R.id.LINEAR_ACCELERATIONchart);
        rotationChart = view.findViewById(R.id.ROTATION_VECTORchart);

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

        return view;
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
}