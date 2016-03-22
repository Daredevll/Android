package com.forecastforyou.fcast4u;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forecastforyou.fcast4u.model.WeatherDataPack;

import java.util.ArrayList;

/**
 * Created by Jovch on 18-Mar-16.
 */
public class CustomRecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{

    Activity activity;
    ArrayList<WeatherDataPack> temps;

    CustomRecyclerAdapter(Activity activity, ArrayList<WeatherDataPack> temps){
        this.activity = activity;
        this.temps = temps;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View row = inflater.inflate(R.layout.weather_recycler_view_layout, parent, false);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return new RecyclerViewHolder(row);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        WeatherDataPack wdp = temps.get(position);
        holder.temperature.setText(wdp.getTemperature() + "\u00B0C");
        holder.clouds.setText(wdp.getClouds());
        holder.wind.setText(wdp.getWind());
        holder.time.setText(wdp.getTime());
        holder.timeDate.setText(wdp.getTimeDate());
        holder.humidity.setText(wdp.getHumidity()+"% humidity");
        holder.icon.setImageBitmap(wdp.getIcon());

    }

    @Override
    public int getItemCount() {
        return temps.size();
    }

    public void clear(){
        temps.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<WeatherDataPack> newItems){
        temps.addAll(newItems);
        notifyDataSetChanged();
    }

}
