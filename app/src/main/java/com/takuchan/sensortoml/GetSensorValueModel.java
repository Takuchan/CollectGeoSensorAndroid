package com.takuchan.sensortoml;

public class GetSensorValueModel{
    private float x;
    private float y;
    private float z;
    public GetSensorValueModel(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getXValue(){
        return this.x;
    }
    public float getYValue(){
        return this.y;
    }
    public float getZValue(){
        return this.z;
    }
}
