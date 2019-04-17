package com.smakhorin.telegramchartapp.utils;

import android.content.Context;

import com.smakhorin.telegramchartapp.R;
import com.smakhorin.telegramchartapp.charts.Followers;
import com.smakhorin.telegramchartapp.charts.Line;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JSONParser {

    /**
     * Parses JSONArray of objects from a string
     *
     * @param json      string to parse
     * @return          returns a complete list of custom objects
     * @throws JSONException
     */
    public static List<Followers> parseJSONToListOfFollowers(String json) throws JSONException {
        JSONArray followersArray = new JSONArray(json);
        List<Followers> followersList = new ArrayList<>();

        for (int i = 0; i < followersArray.length(); i++) {
            JSONObject followersJson = followersArray.getJSONObject(i);
            Followers followers = new Followers();
            followers.setListOfX(getArrayOfX(followersJson));
            for (int j = 1; j < followersJson.getJSONArray("columns").length(); j++) {
                followers.addLine(getY(followersJson, j));
            }
            followersList.add(followers);
        }

        return followersList;
    }

    /**
     * Returns a list of X-coords
     *
     * @param followersJson     json object to get array from
     * @return                  a list of long integers
     * @throws JSONException
     */
    private static List<Long> getArrayOfX(JSONObject followersJson) throws JSONException {
        JSONArray columnsJson = followersJson.getJSONArray("columns");
        JSONArray x = columnsJson.getJSONArray(0);
        x.remove(0);
        List<Long> listOfX = new ArrayList<>();
        for (int i = 0; i < x.length(); i++) {
            listOfX.add(x.getLong(i));
        }
        return listOfX;
    }

    /**
     * Returns a specific line based on an index query
     *
     * @param followersJson     json to retrieve from
     * @param index             index to search
     * @return                  Line object
     * @throws JSONException
     */
    private static Line getY(JSONObject followersJson, int index) throws JSONException {
        JSONArray columnsJson = followersJson.getJSONArray("columns");
        Line lineY = new Line();
        lineY.setListOfY(getArrayOfY(columnsJson.getJSONArray(index)));
        JSONObject nameJson = followersJson.getJSONObject("names");
        JSONObject colorJson = followersJson.getJSONObject("colors");
        lineY.setName(nameJson.getString("y" + (index - 1)));
        lineY.setColor(colorJson.getString("y" + (index - 1)));
        return lineY;
    }

    /**
     * Returns list of Y-coords
     *
     * @param y     json array to retrieve list from
     * @return      list of integers
     * @throws JSONException
     */
    private static List<Integer> getArrayOfY(JSONArray y) throws JSONException {
        y.remove(0);
        List<Integer> listOfY = new ArrayList<>();
        for (int i = 0; i < y.length(); i++) {
            listOfY.add(y.getInt(i));
        }
        return listOfY;
    }

    /**
     * Reads raw json from R.raw
     *
     * @param context      context which is called from (in our case MainActivity)
     * @return              String object with full json data
     */
    public static String readRawJSON(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.formatted);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int size;
        try {
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return outputStream.toString();
    }
}
