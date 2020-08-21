package com.example.weatherapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient fused;
    TextView textView, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9;
    String cityName;
    ImageView imageView;
    private RequestQueue requestQueue;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<Content> contentList;
    private RecyclerView.Adapter adapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        recyclerView = findViewById(R.id.recyclerview);
        textView = findViewById(R.id.text);
        textView2 = findViewById(R.id.temp);
        textView3 = findViewById(R.id.humidper);
        textView4 = findViewById(R.id.climate);
        textView5 = findViewById(R.id.windspeed);
        textView6 = findViewById(R.id.maxtime);
        textView7 = findViewById(R.id.sunrise);
        textView8 = findViewById(R.id.sunset);
        textView9 = findViewById(R.id.date);
        imageView = findViewById(R.id.image);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy");
        String strDate = formatter.format(date);
        textView9.setText(strDate);

        contentList = new ArrayList<>();
        adapter = new Adapter(getApplicationContext(), contentList);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fused = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fused.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        try {
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(), location.getLongitude(), 1
                            );

                            String lat = String.valueOf(addresses.get(0).getLatitude());
                            String lon = String.valueOf(addresses.get(0).getLongitude());
                            getData(lat,lon);
                            cityName = addresses.get(0).getLocality();
                            String stateName = addresses.get(0).getAdminArea();
                            textView.setText(cityName + " : " + stateName);
                            Log.d("city", "" + cityName);
                            Log.d("state", "" + stateName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
        getWeather();

    }

    private void getData(String lat,String lon) {
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&appid=7f861af6a0499b3ff32e8b218de8eead";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray jsonArray = response.getJSONArray("hourly");
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject hour = jsonArray.getJSONObject(i);
                                String dt = hour.getString("dt");
                                String temp = hour.getString("temp");
                                double mtemp = (Double.parseDouble(temp) - 273.15);
                                mtemp = Math.round(mtemp * 100.0) / 100.0;
                                Log.d("dt", String.valueOf(mtemp));
                                Log.d("temp",temp);
                                Instant in = Instant.ofEpochSecond(Long.parseLong(dt));
                                ZoneId.of("Asia/Kolkata");
                                LocalDateTime l = LocalDateTime.ofInstant(in, ZoneId.of("Asia/Kolkata"));
                                String str = l.toString();
                                String[] parts = str.split("T");
                                String part = parts[1];
                                String date = parts[1].substring(0, part.length());
                                contentList.add(new Content(date,String.valueOf(mtemp)));
                            }
                            adapter = new Adapter(MainActivity.this, contentList);
                            recyclerView.setAdapter(adapter);
                        }
                        catch (JSONException e){
                            e.printStackTrace();

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        RequestQueue requestQueue1 = Volley.newRequestQueue(this);
        requestQueue1.add(jsonObjectRequest);
    }

    private void getWeather() {
        String city = (String) textView.getText();
        requestQueue = Volley.newRequestQueue(this);
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + "7f861af6a0499b3ff32e8b218de8eead";
        Log.d("urldemo", url);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject main = response.getJSONObject("main");
                            JSONObject wind = response.getJSONObject("wind");
                            JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                            JSONObject sys = response.getJSONObject("sys");
                            String weathermain = weather.getString("main");
                            String temp = main.getString("temp");
                            double temperature = Double.parseDouble(temp) - 273.15;
                            temperature = Math.round(temperature * 100.0) / 100.0;
                            String humid = main.getString("humidity");
                            String max = main.getString("temp_max");
                            double maxtemp = Double.parseDouble(max) - 273.15;
                            maxtemp = Math.round(maxtemp * 100.0) / 100.0;
                            String speed = wind.getString("speed");
                            String feelslike = main.getString("feels_like");
                            double feels = Double.parseDouble(feelslike) - 273.15;
                            feels = Math.round(feels * 100.0) / 100.0;
                            double speedres = Double.parseDouble(speed) * 3.6;
                            String sunrise = sys.getString("sunrise");
                            String sunset = sys.getString("sunset");
                            textView7.setText(time(Long.parseLong(sunrise)) + " AM");
                            textView8.setText(time(Long.parseLong(sunset)) + " PM");
                            textView6.setText(maxtemp + "°");
                            textView5.setText(String.valueOf(speedres));
                            if (weathermain.equals("rain") || weathermain.equals("Rain")) {
                                imageView.setImageResource(R.drawable.rain);
                            } else if (weathermain.equals("drizzle") || weathermain.equals("Drizzle")) {
                                imageView.setImageResource(R.drawable.drizzle);
                            } else if (weathermain.equals("sunny") || weathermain.equals("Sunny")) {
                                imageView.setImageResource(R.drawable.cloudy);
                            } else if (weathermain.equals("thunderstorm") || weathermain.equals("Thunderstorm")) {
                                imageView.setImageResource(R.drawable.thunderstorm);
                            } else if (weathermain.equals("haze") || weathermain.equals("Haze")) {
                                imageView.setImageResource(R.drawable.haze);
                            }
                            textView4.setText(weathermain + ", Feels like : " + feels + "°C");
                            Log.d("weathermain", "" + weathermain);
                            textView2.setText(temperature + "°C");
                            textView3.setText(humid + "%");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(request);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String time(long time) {
        Instant in = Instant.ofEpochSecond(time);
        ZoneId.of("Asia/Kolkata");
        LocalDateTime l = LocalDateTime.ofInstant(in, ZoneId.of("Asia/Kolkata"));
        String str = l.toString();
        String[] parts = str.split("T");
        String part2 = parts[1];
        String date = parts[1].substring(0, part2.length() - 3);
        return date;
    }

    public void refresh(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}