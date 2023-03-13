package com.takuchan.database.sampledata;

import io.realm.RealmObject;

public class SensorValueDatabase extends RealmObject {
    public float x;
    public float y;
    public float z;

    public SensorValueDatabase(float i, float i1, float i2) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

}

