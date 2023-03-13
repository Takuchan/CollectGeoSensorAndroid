package com.takuchan.sensortoml;

import java.util.ArrayList;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SensorValueDatabase extends RealmObject {
    private ArrayList<GetSensorValueModel> accelerometerList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> gyroscopeList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> linearAcceleList = new ArrayList<GetSensorValueModel>();
    private ArrayList<GetSensorValueModel> rotationList = new ArrayList<GetSensorValueModel>();
}

