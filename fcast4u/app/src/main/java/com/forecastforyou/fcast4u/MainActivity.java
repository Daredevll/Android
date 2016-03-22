package com.forecastforyou.fcast4u;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.forecastforyou.fcast4u.model.CustomLocationListener;
import com.forecastforyou.fcast4u.model.WeatherDataPack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Jovch on 16-Mar-16.
 */
public class MainActivity extends Activity implements CustomLocationListener.LocationUser {

    // TEST URL BUILD HERE: http://api.openweathermap.org/data/2.5/forecast?lat=42.6650466&lon=23.27003735&APPID=2a9214389e51cf441d3b1ff8a413aea7&units=metric

    private boolean firstClick = true;

    static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast";
    static final String API_KEY = "2a9214389e51cf441d3b1ff8a413aea7";
    static final String UNITS_METRIC = "&units=metric";

    public static final int GPS_CODE = 1;
    public static final int NETWORK_CODE = 2;

    public double latitude = 0;
    public double longitude = 0;
    boolean searchByCityNameMaster;

    TextView locationView;
    LinearLayout responseLayout;
    EditText cityName;

    LocationManager locManager;
    LocationListener locListener;
    Location cachedLoc;
    String locProviderGps = LocationManager.GPS_PROVIDER;
    String locProviderNet = LocationManager.NETWORK_PROVIDER;

    Button scaredButton, okButton, notOkButton;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeRefresh;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        responseLayout = (LinearLayout) findViewById(R.id.responseHolder);
        responseLayout.setVisibility(View.GONE);

        cityName = (EditText) findViewById(R.id.city_name);

        /** Sets up the SwipeRefresh indicator */
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeHolder);
        swipeRefresh.setColorSchemeResources(R.color.indicatorArrow);
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.indicatorTransparency);

        recyclerView = (RecyclerView) findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomRecyclerAdapter(this, new ArrayList<WeatherDataPack>());
        recyclerView.setAdapter(adapter);
        swipeRefresh.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        locationView = (TextView) findViewById(R.id.locationText);

        locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locListener = new CustomLocationListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        cachedLoc = locManager.getLastKnownLocation(locProviderGps);
        if (cachedLoc == null) {
            cachedLoc = locManager.getLastKnownLocation(locProviderNet);
        }

        scaredButton = (Button) findViewById(R.id.scaredButton);
        okButton = (Button) findViewById(R.id.notScared);
        notOkButton = (Button) findViewById(R.id.stillScared);

        scaredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(cityName.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                searchByCityNameMaster = cityName.getText().toString().length() != 0;

                ExecuteParams ep;
                if (firstClick) {
                    progressBar.setVisibility(View.VISIBLE);
                    if (cachedLoc != null) {
                        latitude = cachedLoc.getLatitude();
                        longitude = cachedLoc.getLongitude();
                        ep = new ExecuteParams(latitude, longitude, searchByCityNameMaster, cityName.getText().toString());
                        new AsyncJsonFetcher().execute(ep);
                    } else {
                        // TODO: Check if program cycles infinitely here when run for first time on new devices with no LastCachedLocation
                        Toast.makeText(MainActivity.this, "Last location unknown.", Toast.LENGTH_SHORT).show();
                    }
                    firstClick = false;
                }


                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (searchByCityNameMaster) {
                    new AsyncJsonFetcher().execute(new ExecuteParams(latitude, longitude, searchByCityNameMaster, cityName.getText().toString()));
                }
                else {
                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locListener);
                }
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                swipeRefresh.setRefreshing(false);
                scaredButton.callOnClick();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        notOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "+359897239101";
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phone, null));
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }
        });

        scaredButton.callOnClick();
    }

    public void refreshLocation(double lon, double lat) {
        this.longitude = lon;
        this.latitude = lat;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locManager.removeUpdates(locListener);
        new AsyncJsonFetcher().execute(new ExecuteParams(latitude, longitude, searchByCityNameMaster, cityName.getText().toString()));
    }

    /** updates the recyclerView with new adapter, containing the given list */
    private void updateAdapter(ArrayList<WeatherDataPack> list){
        ((CustomRecyclerAdapter) recyclerView.getAdapter()).clear();
        ((CustomRecyclerAdapter) recyclerView.getAdapter()).addAll(list);
        if (swipeRefresh.getVisibility() != View.VISIBLE){
            swipeRefresh.setVisibility(View.VISIBLE);
        }
    }

    public LocationManager getLocationManager() {
        return locManager;
    }

    @Override
    public void hideProgressBar() {
        if (progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
        }
    }

    class AsyncJsonFetcher extends AsyncTask<ExecuteParams, Void, ArrayList<WeatherDataPack>>{

        @Override
        protected ArrayList<WeatherDataPack> doInBackground(ExecuteParams... params) {
            // Making a request and fetching the response Json
            URL url = null;
            HttpURLConnection connection;
            Scanner sc;
            StringBuffer jsonBuffer = null;

            double currentLat = params[0].latitude;
            double currentLon = params[0].longitude;

            try {
                if (params[0].searchByCityName){
                    url = new URL(BASE_URL + "?q=" + params[0].htmlFriendlyCityName + "&APPID=" + API_KEY + UNITS_METRIC);
                }
                else {
                    url = new URL(BASE_URL + "?lat=" + currentLat + "&lon=" + currentLon + "&APPID=" + API_KEY + UNITS_METRIC);
                }
                connection = (HttpURLConnection) url.openConnection();
                Log.e("url", "connection initialized properly");
                connection.connect();
                Log.e("url", "connection connected properly");

                sc = new Scanner(connection.getInputStream());
                Log.e("url", "scanner initialized properly");
                jsonBuffer = new StringBuffer();

                while (sc.hasNextLine()){
                    jsonBuffer.append(sc.nextLine());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("url", e.getMessage() + ", caused by" + e.getCause());
                e.printStackTrace();
            }

            // TODO: fetch the Json from the request, create an ArrayList and refresh the adapter

            int temperature = 0;
            double pressure = 0;
            int humidity = 0;
            String clouds = null;
            String wind = null;
            String time = null;
            Bitmap icon = null;
            String iconCode = null;

            ArrayList<WeatherDataPack> weatherObjectsList = new ArrayList<>();

            try {
                JSONObject rootJson = new JSONObject(jsonBuffer.toString());
                JSONObject city = rootJson.getJSONObject("city");
                final String cityName = city.getString("name") + " (" + city.getString("country") + ")";
                JSONArray list = rootJson.getJSONArray("list");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        locationView.setText("Showing results for " + cityName);
                    }
                });


                // TODO: Iterate the JSONArray list and fill the ArrayList with WeatherObjects

                for (int i = 0; i < list.length(); i++){
                    JSONObject singleForecast = list.getJSONObject(i);
                    JSONObject main = singleForecast.getJSONObject("main");
                    temperature = (int) main.getDouble("temp");
                    pressure = main.getDouble("pressure");
                    humidity = main.getInt("humidity");

                    JSONArray weatherArr = singleForecast.getJSONArray("weather");
                    clouds = weatherArr.getJSONObject(0).getString("description");
                    iconCode = weatherArr.getJSONObject(0).getString("icon");
                    JSONObject windJson = singleForecast.getJSONObject("wind");
                    wind = windJson.getDouble("speed") + "m/s, " + switchWindDirection(windJson.getDouble("deg"));
                    time = list.getJSONObject(i).getString("dt_txt").replaceFirst(" ", "\n  ");

                    String imgPath = "weather_icons/" + iconCode + ".png";

                    icon = getBitmap(MainActivity.this, imgPath);

                    weatherObjectsList.add(new WeatherDataPack(String.valueOf(temperature), wind, clouds, i==0?"Now": "in " + i*3 + " hours", time, String.valueOf(humidity),  icon));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weatherObjectsList;
        }

        @Override
        protected void onPostExecute(ArrayList<WeatherDataPack> weatherDataPacks) {
            updateAdapter(weatherDataPacks);
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);

            if (responseLayout.getVisibility() != View.VISIBLE) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                responseLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }).start();
            }

        }

        /**
         * Used to load images from the assets folder in the project
         * @param context
         * @param relativePath
         * @return
         */
        private Bitmap getBitmap(Context context, String relativePath) {
            InputStream bitmapIs = null;
            Bitmap bmp = null;
            try {
                bitmapIs = context.getAssets().open(relativePath);
                bmp = BitmapFactory.decodeStream(bitmapIs);
            } catch (IOException e) {
                e.printStackTrace();

                if(bmp != null) {
                    bmp.recycle();
                    bmp = null;
                }
            } finally {
                if(bitmapIs != null) {
                    try {
                        bitmapIs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bmp;
        }

        /**
         * Used to load images directly from the weather provider server
         * @param sourceURL should formatted as: http://openweathermap.org/img/w/" + iconCode + ".png, where iconCode is extracted from the JSON
         * @return
         */
        private Bitmap getBitmapFromURL(String sourceURL) {
            try {
                Log.e("src",sourceURL);
                URL url = new URL(sourceURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                Log.e("Bitmap","returned");
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Exception",e.getMessage());
                return null;
            }
        }

        private String switchWindDirection(double degree){
            String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
            return directions[ ((int)Math.round((  ((double)degree % 360) / 45))) % 8 ];
        }
    }

    private class ExecuteParams{
        double latitude, longitude;
        boolean searchByCityName;
        String htmlFriendlyCityName;

        ExecuteParams(double latitude, double longitude, boolean searchByCityName, String cityNameInput) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.searchByCityName = searchByCityName;
            htmlFriendlyCityName = validateSpaces(cityNameInput);
        }

        private String validateSpaces(String input){
            return input.replaceAll(" ", "%20");
        }
    }

}


