package com.takuchan.database.sampledata;
import com.takuchan.sensortoml.GetSensorValueModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GetSensorValueDatabase extends RealmObject {
    @PrimaryKey
    public long id;
    public String examName;
    public String timestamp;
    public RealmList<SensorListDatabase> accelerometerList = new RealmList<>();
    public RealmList<SensorListDatabase> gyroscopeList = new RealmList<>();
    public RealmList<SensorListDatabase> linearaccleList = new RealmList<>();
    public RealmList<SensorListDatabase> rotationList = new RealmList<>();

}
