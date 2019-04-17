package com.smakhorin.telegramchartapp;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.smakhorin.telegramchartapp.charts.ChartsAdapter;
import com.smakhorin.telegramchartapp.charts.Followers;
import com.smakhorin.telegramchartapp.utils.JSONParser;

import org.json.JSONException;

import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChartsAdapter chartsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String JSONString;

        JSONString = JSONParser.readRawJSON(this);
        if(JSONString == null || JSONString.isEmpty()) {
            Log.d("MainActivity","JSON String is null/empty");
        }
        else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Statistics");
            chartsAdapter = new ChartsAdapter();
            recyclerView = findViewById(R.id.rv_charts);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(chartsAdapter);
            parseJSON(JSONString);
        }
    }

    private void parseJSON(String json) {
        if(TextUtils.isEmpty(json)){
            return;
        }
        try {
            List<Followers> followersList = JSONParser.parseJSONToListOfFollowers(json);
            chartsAdapter.setData(followersList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_transition) {
            changeTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeTheme() {
        int colorToStatusBar, colorToToolbar, colorToBackground;

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            colorToStatusBar = ContextCompat.getColor(this, R.color.colorPrimaryDayDark);
            colorToToolbar = ContextCompat.getColor(this, R.color.colorPrimaryDay);
            colorToBackground = ContextCompat.getColor(this, R.color.colorBackgroundDay);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            colorToStatusBar = ContextCompat.getColor(this, R.color.colorPrimaryNightDark);
            colorToToolbar = ContextCompat.getColor(this, R.color.colorPrimaryNight);
            colorToBackground = ContextCompat.getColor(this, R.color.colorBackgroundNight);
        }

        chartsAdapter.changeTheme();
        getWindow().setStatusBarColor(colorToStatusBar);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(colorToToolbar));
        recyclerView.setBackgroundColor(colorToBackground);
    }
}