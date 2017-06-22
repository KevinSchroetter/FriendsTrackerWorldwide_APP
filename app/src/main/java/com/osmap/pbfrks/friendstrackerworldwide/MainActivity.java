package com.osmap.pbfrks.friendstrackerworldwide;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.KeyboardView;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.HashMap;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    private MapView osm;
    private MapController mc;
    private TextView textGeoLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Location location;

    private String myUsername;
    private double myLatitude;
    private double myLongitude;
    private Button updateButton;
    private Button resetButton;
    private ProgressDialog dialog;
    private int initialization = 0;
    private int initializationMessage = 0;

    private JSONObject resultFriends;
    private JSONObject resultFriendsMarker;
    private JSONObject resultMyMarker;

    ApiCaller apiCaller;
    private HashMap<String,HashMap<String, String>> myFriends;
    protected ArrayList<String> myFriendsArrayList;
    private GeoPoint myGeoPoint;
    private Marker myMarker;
    private HashMap<String, Marker> myFriendsLocationMarker;
    private HashMap<String, Marker> myFriendsMarkerMarker;
    private HashMap<String, SpecialMarker> myMarkerMarker;


    private updateMyLocationTask myLocTask = null;
    private getMyFriendsTask myFriendsTask = null;
    private getMyFriendsMarkerTask myFriendsMarkerTask = null;
    private getMyMarkerTask myMarkerTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        apiCaller = new ApiCaller();
        myFriends = new HashMap<String, HashMap<String, String>>();
        myFriendsArrayList = new ArrayList<String>();
        myFriendsLocationMarker = new HashMap<String, Marker>();
        myFriendsMarkerMarker = new HashMap<String, Marker>();
        myMarkerMarker = new HashMap<String,SpecialMarker>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        myUsername = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        resetButton = (Button) findViewById(R.id.buttonReset);
        updateButton = (Button) findViewById(R.id.buttonUpdate);
        if(initialization==0) {
            resetButton.setVisibility(View.INVISIBLE);
            updateButton.setVisibility(View.INVISIBLE);
            dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait until we got your GPS Data...");
            dialog.setCancelable(false);
            dialog.show();
        }
        osm = (MapView) findViewById(R.id.mapView);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);
        mc = (MapController) osm.getController();
        textGeoLocation = (TextView) findViewById(R.id.geoLocationText);
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                osm.getOverlays().remove(myFriendsLocationMarker.get("Phil"));
                osm.getOverlays().clear();
                InfoWindow.closeAllInfoWindowsOn(osm);
                osm.invalidate();
                Toast.makeText(MainActivity.this, "Map resetted", Toast.LENGTH_SHORT).show();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                updateMap();
            }
        });
                /*
         * This part manages getting gps data whenever the location changes
         */
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                GeoPoint updatedPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();
                myGeoPoint = updatedPoint;
                textGeoLocation.setText("Hello "+myUsername+"!\nLatitude: "+ myLatitude+"\nLongitude: "+myLongitude);
                addMyMarker();
                mc.setZoom(14);
                mc.animateTo(myGeoPoint);


                if (initialization == 0){
                    updateButton.setVisibility(View.VISIBLE);
                    resetButton.setVisibility(View.VISIBLE);
                    updateMap();
                    dialog.dismiss();
                    initialization = 1;
                }else{
                    drawMarker();
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        showMap();
    }

    private void drawMarker(){
        osm.getOverlays().clear();
        osm.getOverlays().add(myMarker);
        for(HashMap.Entry<String,Marker> marker: myFriendsLocationMarker.entrySet()){
            osm.getOverlays().add(marker.getValue());
        }
        for(HashMap.Entry<String,Marker> markermarker: myFriendsMarkerMarker.entrySet()){
            osm.getOverlays().add(markermarker.getValue());
        }
        for(HashMap.Entry<String,SpecialMarker> mymarker: myMarkerMarker.entrySet()){
            osm.getOverlays().add(mymarker.getValue());
        }
        osm.invalidate();
    }


    private void updateMap() {
        attemptUpdateLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                showMap();
                break;
            default:
                break;
        }
    }
    public void showMap(){
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            }
            return;
        }
        /*
         * This code only executes when the permissions are allowed.
         * It then starts the locationUpdate requests as well as a first geo-location
         * via the lastKnownLocation
         */


        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        //location = locationManager.getLastKnownLocation(GPS_PROVIDER);
    }

    public void addMyMarker(){
        Marker marker = new Marker(osm);
        marker.setPosition(myGeoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.position));
        marker.setTitle("YOU");
        myMarker = marker;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings_friends:
                Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                intent.putExtra(EXTRA_MESSAGE,myFriendsArrayList);
                intent.putExtra(EXTRA_MESSAGE2,myUsername);
                startActivityForResult(intent, 3);
                break;
            case R.id.action_settings_notifications:
                Toast.makeText(this, "Notifications selected", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
    public class updateMyLocationTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final double myLatitude;
        private final double myLongitude;

        updateMyLocationTask(String username, double latitude, double longitude) {
            myUsername = username;
            myLatitude = latitude;
            myLongitude = longitude;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("latitude", myLatitude);
                locationUpdateParams.put("longitude", myLongitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/updateUser";
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executePut(apiUrl,locationUpdateParams.toString());
            if(resultObj.has("message")){
                return true;
            }
            else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myLocTask = null;
            //showProgress(false);

            if (success) {
                osm.getOverlays().clear();
                osm.getOverlay().remove(osm);
                attemptGetFriends();
            } else {
                Toast.makeText(MainActivity.this, "Could not update GPS-Location!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            myLocTask = null;
            //showProgress(false);
        }
    }
    private void attemptUpdateLocation() {
        if (myLocTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            myLocTask = new MainActivity.updateMyLocationTask(myUsername, myLatitude, myLongitude);
            myLocTask.execute((Void) null);

        }
    }
    public class getMyFriendsTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;


        getMyFriendsTask(String username) {
            myUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/getFriendsLocation/"+myUsername;
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executeGet(apiUrl);



            if(resultObj == null){
                return false;
            }
            else{
                resultFriends = resultObj;
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myFriendsTask = null;
            //showProgress(false);

            if (success) {
                if(resultFriends.has("friendsLocation")){
                    try {
                        myFriendsLocationMarker.clear();
                        myFriends.clear();
                        myFriendsArrayList.clear();
                        JSONArray friends = resultFriends.getJSONArray("friendsLocation");
                        for (int i = 0; i< friends.length(); i++){
                            JSONObject friendJSON = friends.getJSONObject(i);
                            JSONObject friendLocation = friendJSON.getJSONObject("geoLocation");
                            HashMap<String,String> friendInfo = new HashMap<String,String>();
                            friendInfo.put("username", friendJSON.get("username").toString());
                            friendInfo.put("latitude", friendLocation.get("latitude").toString());
                            friendInfo.put("longitude", friendLocation.get("longitude").toString());
                            friendInfo.put("description",friendJSON.get("description").toString());
                            double friendLat = Double.parseDouble(friendLocation.get("latitude").toString());
                            double friendLon = Double.parseDouble(friendLocation.get("longitude").toString());
                            GeoPoint friendPoint = new GeoPoint(friendLat, friendLon);
                            Marker friendMarker = new Marker(osm);
                            friendMarker.setPosition(friendPoint);
                            friendMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            friendMarker.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.person));
                            friendMarker.setTitle(friendJSON.get("description").toString());
                            myFriendsLocationMarker.put(friendJSON.get("username").toString(), friendMarker);
                            myFriends.put(friendJSON.get("username").toString(), friendInfo);
                            myFriendsArrayList.add(friendJSON.get("username").toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                attemptGetFriendsMarker();
            }else{
                Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
            }
            resultFriends = null;
        }

        @Override
        protected void onCancelled() {
            myFriendsTask = null;
            //showProgress(false);
        }
    }
    public void attemptGetFriends() {
        if (myFriendsTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            myFriendsTask = new MainActivity.getMyFriendsTask(myUsername);
            myFriendsTask.execute((Void) null);

        }
    }
    public class getMyFriendsMarkerTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;


        getMyFriendsMarkerTask(String username) {
            myUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/marker/getFriendsMarker/"+myUsername;
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executeGet(apiUrl);



            if(resultObj == null){
                return false;
            }
            else{
                resultFriendsMarker = resultObj;
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myFriendsMarkerTask = null;
            //showProgress(false);

            if (success) {
                if(resultFriendsMarker.has("friendsMarker")){
                    try {
                        myFriendsMarkerMarker.clear();
                        JSONArray friends = resultFriendsMarker.getJSONArray("friendsMarker");
                        for (int i = 0; i< friends.length(); i++){
                            JSONObject friendJSON = friends.getJSONObject(i);
                            JSONObject friendLocation = friendJSON.getJSONObject("geoLocation");
                            double friendLat = Double.parseDouble(friendLocation.get("latitude").toString());
                            double friendLon = Double.parseDouble(friendLocation.get("longitude").toString());
                            GeoPoint friendPoint = new GeoPoint(friendLat, friendLon);
                            Marker friendMarker = new Marker(osm);
                            friendMarker.setPosition(friendPoint);
                            friendMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            friendMarker.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.marker3));
                            friendMarker.setTitle(friendJSON.get("owner").toString() + ":\n"+friendJSON.get("description").toString());
                            myFriendsMarkerMarker.put(friendJSON.get("owner").toString(), friendMarker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                attemptGetMyMarker();
            }
            else{
                Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
            }
            resultFriendsMarker = null;
        }

        @Override
        protected void onCancelled() {
            myFriendsMarkerTask = null;
            //showProgress(false);
        }
    }
    private void attemptGetFriendsMarker() {
        if (myFriendsMarkerTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            myFriendsMarkerTask = new MainActivity.getMyFriendsMarkerTask(myUsername);
            myFriendsMarkerTask.execute((Void) null);

        }
    }
    public class getMyMarkerTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;


        getMyMarkerTask(String username) {
            myUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/marker/getMyMarker/"+myUsername;
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executeGet(apiUrl);



            if(resultObj == null){
                return false;
            }
            else{
                resultMyMarker = resultObj;
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myMarkerTask = null;
            //showProgress(false);

            if (success) {
                if(resultMyMarker.has("myMarker")){
                    try {
                        myMarkerMarker.clear();
                        JSONArray mySavedMarkers = resultMyMarker.getJSONArray("myMarker");
                        for (int i = 0; i< mySavedMarkers.length(); i++){
                            JSONObject mySavedMarkersJSON = mySavedMarkers.getJSONObject(i);
                            JSONObject mySavedMarkersLocation = mySavedMarkersJSON.getJSONObject("geoLocation");
                            double friendLat = Double.parseDouble(mySavedMarkersLocation.get("latitude").toString());
                            double friendLon = Double.parseDouble(mySavedMarkersLocation.get("longitude").toString());
                            GeoPoint friendPoint = new GeoPoint(friendLat, friendLon);
                            String markerID = mySavedMarkersJSON.get("_id").toString();
                            SpecialMarker mySpecialMarker = new SpecialMarker(osm, markerID);
                            mySpecialMarker.setPosition(friendPoint);
                            mySpecialMarker.setAnchor(SpecialMarker.ANCHOR_CENTER, SpecialMarker.ANCHOR_BOTTOM);
                            mySpecialMarker.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.marker2));
                            mySpecialMarker.setTitle(mySavedMarkersJSON.get("description").toString());
                            myMarkerMarker.put(mySavedMarkersJSON.get("_id").toString(), mySpecialMarker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(initializationMessage == 0){
                    Toast.makeText(MainActivity.this, "Data initialized!", Toast.LENGTH_SHORT).show();
                    initializationMessage = 1;
                }else{
                    Toast.makeText(MainActivity.this, "Account data updated!", Toast.LENGTH_SHORT).show();
                }
                drawMarker();
            } else {
                Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
            }
            resultMyMarker = null;
        }

        @Override
        protected void onCancelled() {
            myMarkerTask = null;
            //showProgress(false);
        }
    }
    private void attemptGetMyMarker() {
        if (myMarkerTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            myMarkerTask = new MainActivity.getMyMarkerTask(myUsername);
            myMarkerTask.execute((Void) null);

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            updateMap();
        }
    }
}
