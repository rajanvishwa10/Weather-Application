package com.example.weatherapplication;

public class Content {
    public String dt;
    public String temp;

    public Content(){

    }
    public Content(String dt, String temp) {
        this.dt = dt;
        this.temp = temp;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }
}
