package com.takuchan.database.sampledata;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SensorListDatabase extends RealmObject {
    @PrimaryKey
    public long id;
    public RealmList<SensorValueDatabase> sensorValueDatabases = new RealmList<>();
}
