package com.forecastforyou.fcast4u.model;

import android.graphics.Bitmap;

/**
 * Created by Jovch on 18-Mar-16.
 */
public class WeatherDataPack {

    String temperature;
    String wind;
    String clouds;
    String time;
    String timeDate;
    String humidity;
    Bitmap icon;

    public WeatherDataPack(String temperature, String wind, String clouds, String time, String timeDate, String humidity, Bitmap icon) {
        this.temperature = temperature;
        this.wind = wind;
        this.clouds = clouds;
        this.time = time;
        this.timeDate = timeDate;
        this.humidity = humidity;
        this.icon = icon;
    }


    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }

    public String getClouds() {
        return clouds;
    }

    public String getTime() {
        return time;
    }

    public String getTimeDate() {
        return timeDate;
    }

    public String getHumidity() {
        return humidity;
    }

    public Bitmap getIcon() {
        return icon;
    }
}
