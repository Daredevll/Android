package com.forecastforyou.fcast4u;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Jovch on 18-Mar-16.
 */
public class RecyclerViewHolder extends RecyclerView.ViewHolder{

    TextView temperature;
    TextView wind;
    TextView clouds;
    TextView time;
    TextView timeDate;
    TextView humidity;
    ImageView icon;

    public RecyclerViewHolder(View v) {
        super(v);
        temperature = (TextView) v.findViewById(R.id.temperature);
        wind = (TextView) v.findViewById(R.id.wind);
        clouds = (TextView) v.findViewById(R.id.clouds);
        time = (TextView) v.findViewById(R.id.time);
        timeDate = (TextView) v.findViewById(R.id.time_date);
        humidity = (TextView) v.findViewById(R.id.humidity);
        icon = (ImageView) v.findViewById(R.id.iconView);
    }
}
