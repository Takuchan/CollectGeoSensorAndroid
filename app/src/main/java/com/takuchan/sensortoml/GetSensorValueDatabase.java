package com.takuchan.sensortoml;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GetSensorValueDatabase extends RealmObject {
    @PrimaryKey
    private long id;
    private String examName;
    private String timestamp;
    private RealmList<SensorValueDatabase> sensorValueDatabases;
}
