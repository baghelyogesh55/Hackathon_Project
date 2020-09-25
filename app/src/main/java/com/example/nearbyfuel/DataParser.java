package com.example.nearbyfuel;

import android.renderscript.Element;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by navneet on 23/7/16.
 */
public class DataParser extends AppCompatActivity {
    private static final String TAG = "DataParser";
    public List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            Log.d("Places", "parse");
            jsonObject = new JSONObject((String) jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            Log.d("Places", "parse error");
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
                Log.d("Places", "Adding places");

            } catch (JSONException e) {
                Log.d("Places", "Error in Adding places");
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<String, String>();

        String placeName = "";
        String vicinity = "";
        String latitude = "";
        String longitude = "";
        String brands = "";
        String city = "";
        double dis;

        try {

           placeName = googlePlaceJson.getJSONObject("poi").getString("name");
           vicinity = googlePlaceJson.getJSONObject("address").getString("freeformAddress");
           city = googlePlaceJson.getJSONObject("address").getString("countrySecondarySubdivision");
            Log.d(TAG, "getPlace: City : "+city);
            try {
                brands = googlePlaceJson.getJSONObject("poi").getJSONArray("brands").getJSONObject(0).getString("name");
            }
            catch(Exception ex)
            {
                brands="Not available";
            }
            Log.d(TAG, "getPlace: City : "+city);
            FuelFragment.city = city;
            latitude = googlePlaceJson.getJSONObject("position").getString("lat");
            longitude = googlePlaceJson.getJSONObject("position").getString("lon");
            dis=googlePlaceJson.getDouble("dist");
            Log.d(TAG, "getPlace: distance in meter : "+dis);
            Log.d(TAG, "getPlace: placeName:"+placeName);
            googlePlaceMap.put("name",placeName);
            googlePlaceMap.put("vicinity",vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lon", longitude);
            googlePlaceMap.put("dist",""+dis);
            googlePlaceMap.put("brands",brands);

            Log.d(TAG, "getPlace: lat:"+latitude+" lon:"+longitude);
            Log.d("getPlace", "Putting Places");

        } catch (JSONException e) {
            Log.d("getPlace", "Error");
            e.printStackTrace();
        }
        return googlePlaceMap;
    }



}