package com.example.nearbyfuel;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by navneet on 23/7/16.
 */
public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {
    private static final String TAG = "GetNearbyPlacesData";
    String googlePlacesData;
    GoogleMap mMap;
    public ArrayList<String> arrayList=new ArrayList<>();
    String url;

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("GetNearbyPlacesData", "doInBackground entered");
            mMap = (GoogleMap) params[0];
            url = (String) params[1];

            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d(TAG, "doInBackground: googlePacesData = "+googlePlacesData);
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }


    @Override
    protected void onPostExecute(String result) {
        Log.d(TAG, "onPostExecute: "+result);
        Log.d(TAG, "onPostExecute: Inside GetNearbyPlacesData");

        List<HashMap<String, String>> nearbyPlacesList = null;

        DataParser dataParser = new DataParser();

        nearbyPlacesList =  dataParser.parse(result);
        Log.d(TAG, "onPostExecute: nearbyPlacesList"+nearbyPlacesList);
        ShowNearbyPlaces(nearbyPlacesList);
        Log.d(TAG, "onPostExecute: Exiting");

    }

    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute","Entered into showing locations");
            Log.d(TAG, "ShowNearbyPlaces: MarkersOptions created");
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lon"));
            String placeName = googlePlace.get("name");
            String vicinity = googlePlace.get("vicinity");
            String dist = googlePlace.get("dist");
            double d= Double.parseDouble(dist);
            Log.d(TAG, "ShowNearbyPlaces: distance precision : "+ String.format("%.5f", d));
            if(d>1000)
            {
                d=d/1000;
                dist= String.format("%.2f", d)+" km";
            }
            else
            {
                dist= String.format("%.2f",d)+" m";
            }
            Log.d(TAG, "ShowNearbyPlaces: date : "+googlePlace.get("date"));
            Log.d(TAG, "ShowNearbyPlaces: pet : "+googlePlace.get("pet"));
            Log.d(TAG, "ShowNearbyPlaces: die : "+googlePlace.get("die"));
            String brands = googlePlace.get("brands");
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(placeName).snippet("Distance : "+dist+"\nBrand    : "+brands);
            mMap.addMarker(markerOptions);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            Log.d(TAG, "ShowNearbyPlaces: Markers Options Added");
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Log.d(TAG, "onMarkerClick: data on clicking marker_ title:"+marker.getTitle()+"\n"+marker.getSnippet());

                    return false;
                }
            });
            //move map camera
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }
}