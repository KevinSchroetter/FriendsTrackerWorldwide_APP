package com.osmap.pbfrks.friendstrackerworldwide;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements MapEventsReceiver {
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    public static final String EXTRA_MESSAGE4 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE4";
    private MapView osm;
    private MapController mc;
    private TextView textGeoLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Location location;
    private int addMarkerStatus = 0;
    private double radius = 100.0;
    private boolean locationChanged = false;

    private String myUsername;
    private double myLatitude = 10000;
    private double myLongitude = 10000;
    private Button updateButton;
    private Button centerButton;
    private ProgressDialog dialog;
    private int initialization = 0;
    private int initializationMessage = 0;

    private JSONObject resultFriends;
    private JSONObject resultFriendsMarker;
    private JSONObject resultMyMarker;
    private JSONObject resultRequests;

    ApiCaller apiCaller;
    private HashMap<String, Marker> myFriends;
    protected ArrayList<String> myFriendsArrayList;
    private GeoPoint myGeoPoint;
    private Marker myMarker;
    private HashMap<String, Marker> myFriendsLocationMarker;
    private HashMap<String, Marker> myFriendsMarkerMarker;
    private HashMap<String, SpecialMarker> myMarkerMarker;
    private HashMap <String, Marker> myNotifications;


    private updateMyLocationTask myLocTask = null;
    private getMyFriendsTask myFriendsTask = null;
    private getMyFriendsMarkerTask myFriendsMarkerTask = null;
    private getMyMarkerTask myMarkerTask = null;
    private addMarkerTask addTask = null;
    private deleteMarkerTask deleteTask = null;
    private getRequestsTask requestsTask = null;

    private MapEventsOverlay mapEventsOverlay;

    private AlertDialog.Builder initDisplayMessageBuilder;
    private AlertDialog.Builder deleteMarkerMessageBuilder;
    private AlertDialog.Builder addMarkerMessageBuilder;
    private AlertDialog initDisplayMessage;
    private AlertDialog deleteMarkerMessage;
    private ItemizedIconOverlay<OverlayItem> myOverlay;
    private ArrayList<OverlayItem> myOverlayItems;
    private Marker.OnMarkerDragListener markerListener;
    private String descriptionText;
    private GeoPoint addMarkerPoint;
    private SpecialMarker castedMarker;
    private ArrayList<String> sendIt;
    private Marker castedMyMarker;
    private Button requestButton;
    private ArrayList<String> requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        apiCaller = new ApiCaller();
        myFriends = new HashMap<String, Marker>();
        myFriendsArrayList = new ArrayList<String>();
        myFriendsLocationMarker = new HashMap<String, Marker>();
        myFriendsMarkerMarker = new HashMap<String, Marker>();
        myMarkerMarker = new HashMap<String,SpecialMarker>();
        myNotifications = new HashMap<String, Marker>();
        requests = new ArrayList<String>();

        sendIt = new ArrayList<String>();
        deleteMarkerMessageBuilder = new AlertDialog.Builder(MainActivity.this);
        addMarkerMessageBuilder = new AlertDialog.Builder(MainActivity.this);

        markerListener = new Marker.OnMarkerDragListener(){

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }

            @Override
            public void onMarkerDragStart(Marker marker) {
                castedMarker = (SpecialMarker) marker;

                deleteMarkerMessageBuilder.setTitle("Delete your Marker?");
                deleteMarkerMessageBuilder.setMessage("Attempting to delete your Marker: \n"+marker.getTitle());
                deleteMarkerMessageBuilder.setCancelable(false);
                deleteMarkerMessageBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addMarkerStatus = 1;
                        attemptDeleteMarker(castedMarker.getId());
                    }
                });
                deleteMarkerMessageBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do Nothing
                    }
                });
                deleteMarkerMessage = deleteMarkerMessageBuilder.create();
                deleteMarkerMessage.show();
            }
        };

        initDisplayMessageBuilder = new AlertDialog.Builder(MainActivity.this);
        initDisplayMessageBuilder.setTitle("INITIALIZATION COMPLETE");
        initDisplayMessageBuilder.setMessage("Updated your GPS-Location...\nUpdated your Markers...\nUpdated your Friendlist...\nUpdated Markers of your Friends...");
        initDisplayMessageBuilder.setCancelable(false);
        initDisplayMessageBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do things
            }
        });
        initDisplayMessage = initDisplayMessageBuilder.create();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        myUsername = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        centerButton = (Button) findViewById(R.id.buttonCenter);
        updateButton = (Button) findViewById(R.id.buttonUpdate);
        requestButton = (Button) findViewById(R.id.buttonRequest);
        if(requests.size()<=0){
            requestButton.setVisibility(View.INVISIBLE);
        }
        if(initialization==0) {
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
        castedMyMarker = new Marker(osm);
        mc = (MapController) osm.getController();
        mapEventsOverlay = new MapEventsOverlay(this, this);
        textGeoLocation = (TextView) findViewById(R.id.geoLocationText);
        centerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mc.animateTo(myGeoPoint);
            }
        });
        requestButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent reqIntent = new Intent(MainActivity.this, RequestActivity.class);
                reqIntent.putStringArrayListExtra(EXTRA_MESSAGE4,requests);
                reqIntent.putExtra(EXTRA_MESSAGE2,myUsername);
                startActivityForResult(reqIntent, 5);
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
                textGeoLocation.setText("Hello "+myUsername+"!\nLatitude: "+ String.format("%.6f", myLatitude)+"\nLongitude: "+String.format("%.6f", myLongitude));
                addMyMarker();
                mc.setZoom(14);

                if (initialization == 0){
                    updateButton.setVisibility(View.VISIBLE);
                    attemptGetMyMarker();
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
        InfoWindow.closeAllInfoWindowsOn(osm);


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

        osm.getOverlays().add(0, mapEventsOverlay);
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
            case R.id.action_settings_nearby:
                Intent myIntent = new Intent(MainActivity.this, NotificationActivity.class);
                sendIt.clear();
                myNotifications.clear();
                updateNotifications();
                myIntent.putStringArrayListExtra(EXTRA_MESSAGE3, sendIt);
                startActivityForResult(myIntent, 4);
                break;
            case R.id.action_settings_close:
                System.exit(0);
        }
        return true;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {

        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        addMarkerPoint = p;
        addMarkerMessageBuilder.setTitle("Add Marker");
        addMarkerMessageBuilder.setMessage("Enter description:");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        addMarkerMessageBuilder.setView(input);
        addMarkerMessageBuilder.setPositiveButton("ADD",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        addMarkerStatus = 1;
                        descriptionText = input.getText().toString();
                        attemptAddMarker((double)addMarkerPoint.getLatitude(), (double)addMarkerPoint.getLongitude(),descriptionText);
                        textGeoLocation.setText("Lat: "+addMarkerPoint.getLatitude()+"\nLon: "+addMarkerPoint.getLongitude());
                    }
                });
        addMarkerMessageBuilder.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        addMarkerMessageBuilder.show();
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
                osm.getOverlays().clear();
                myFriendsLocationMarker.clear();
                myFriends.clear();
                myFriendsArrayList.clear();
                if(resultFriends.has("friendsLocation")){
                    try {

                        JSONArray friends = resultFriends.getJSONArray("friendsLocation");
                        for (int i = 0; i< friends.length(); i++){
                            JSONObject friendJSON = friends.getJSONObject(i);
                            JSONObject friendLocation = friendJSON.getJSONObject("geoLocation");
                            //HashMap<String,String> friendInfo = new HashMap<String,String>();
                            //friendInfo.put("username", friendJSON.get("username").toString());
                           // friendInfo.put("latitude", friendLocation.get("latitude").toString());
                           // friendInfo.put("longitude", friendLocation.get("longitude").toString());
                           // friendInfo.put("description",friendJSON.get("description").toString());
                            double friendLat = Double.parseDouble(friendLocation.get("latitude").toString());
                            double friendLon = Double.parseDouble(friendLocation.get("longitude").toString());
                            GeoPoint friendPoint = new GeoPoint(friendLat, friendLon);
                            Marker friendMarker = new Marker(osm);
                            friendMarker.setPosition(friendPoint);
                            friendMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            friendMarker.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.person));
                            String ending = "s";

                            if(friendJSON.get("username").toString().endsWith("s") || friendJSON.get("username").toString().endsWith("x") || friendJSON.get("username").toString().endsWith("z")){
                                ending = "'";
                            }
                            friendMarker.setTitle(friendJSON.get("username").toString()+ending+" Position");
                            myFriendsLocationMarker.put(friendJSON.get("username").toString(), friendMarker);
                            myFriends.put(friendJSON.get("username").toString()+ ending + " Position", friendMarker);
                            myFriendsArrayList.add(friendJSON.get("username").toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                attemptGetFriendsMarker();
                attemptGetRequests();
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
                            String ending = "s";
                            if(friendJSON.get("owner").toString().endsWith("s") || friendJSON.get("owner").toString().endsWith("x") || friendJSON.get("owner").toString().endsWith("z")){
                                ending = "'";
                            }
                            friendMarker.setTitle(friendJSON.get("owner").toString() + ending + " Marker:\n"+friendJSON.get("description").toString());
                            myFriendsMarkerMarker.put(friendMarker.getTitle(), friendMarker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(initializationMessage == 0){
                    initDisplayMessage.show();
                    mc.animateTo(myGeoPoint);
                    initializationMessage = 1;
                }else{
                    Toast.makeText(MainActivity.this, "Account data updated!", Toast.LENGTH_SHORT).show();
                }
                drawMarker();
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
                            SpecialMarker mySpecialMarker = new SpecialMarker(osm);
                            mySpecialMarker.updateId(markerID);
                            mySpecialMarker.setPosition(friendPoint);
                            mySpecialMarker.setAnchor(SpecialMarker.ANCHOR_CENTER, SpecialMarker.ANCHOR_BOTTOM);
                            mySpecialMarker.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.marker2));
                            mySpecialMarker.setTitle(mySavedMarkersJSON.get("description").toString());
                            GeoPoint testPoint = new GeoPoint(48.656, 9.54);
                            mySpecialMarker.setDraggable(true);
                            mySpecialMarker.setOnMarkerDragListener(markerListener);
                            myMarkerMarker.put(mySavedMarkersJSON.get("_id").toString(), mySpecialMarker);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(initializationMessage == 0){
                    attemptUpdateLocation();
                }
                else if(addMarkerStatus == 1){
                    attemptUpdateLocation();
                    addMarkerStatus = 0;
                }
                else{
                    Toast.makeText(MainActivity.this, "Your markers updated!", Toast.LENGTH_SHORT).show();
                }
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
        if(requestCode == 3){
            if(resultCode == RESULT_OK){
                attemptGetFriends();
            }
        }
        if(requestCode == 4){
            if(resultCode == RESULT_OK){
                String targetMarker = data.getStringExtra(EXTRA_MESSAGE3);
                mc.animateTo(myNotifications.get(targetMarker).getPosition());
            }
        }
        if(requestCode == 5){
            //TODO handle result from requests
            if(resultCode == RESULT_OK){
                attemptGetFriends();
            }
        }

    }
    private void updateNotifications() {
        String distance = "";
        if(myMarkerMarker.size()>0) {
            for (HashMap.Entry<String, SpecialMarker> mymarker : myMarkerMarker.entrySet()) {
                distance = distanceBetween(myLatitude, myLongitude, mymarker.getValue().getPosition().getLatitude(), mymarker.getValue().getPosition().getLongitude());
                castedMyMarker = new Marker(osm);
                castedMyMarker.setPosition(mymarker.getValue().getPosition());
                castedMyMarker.setTitle("mm" + distance + " km:\n" + mymarker.getValue().getTitle());
                myNotifications.put(castedMyMarker.getTitle(),castedMyMarker);
            }
        }
        if(myFriends.size()>0){
            for (HashMap.Entry<String, Marker> friend: myFriends.entrySet()){
                distance = distanceBetween(myLatitude, myLongitude, friend.getValue().getPosition().getLatitude(), friend.getValue().getPosition().getLongitude());
                myNotifications.put("fp" + distance + " km:\n" + friend.getKey(), friend.getValue());
            }
        }
        if(myFriendsMarkerMarker.size()>0){
            for( HashMap.Entry<String, Marker> friendMarker: myFriendsMarkerMarker.entrySet()){
                distance = distanceBetween(myLatitude, myLongitude, friendMarker.getValue().getPosition().getLatitude(), friendMarker.getValue().getPosition().getLongitude());
                myNotifications.put("fm" + distance + " km:\n"+friendMarker.getKey(), friendMarker.getValue());
            }
        }

        if(myNotifications.size()>0) {
            for (HashMap.Entry<String, Marker> marker : myNotifications.entrySet()) {
                sendIt.add(marker.getKey());
            }
        }
    }
    private String distanceBetween(double lat1, double lon1, double lat2, double lon2){
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
        String s = String.format("%.1f", dist);
        return (s);
    }
    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }

    public class addMarkerTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final double myLatitude;
        private final double myLongitude;
        private final String myDescription;

       addMarkerTask(String owner, double latitude, double longitude, String description) {
            myUsername = owner;
            myLatitude = latitude;
            myLongitude = longitude;
            myDescription = description;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("owner", myUsername);
                locationUpdateParams.put("latitude", myLatitude);
                locationUpdateParams.put("longitude", myLongitude);
                locationUpdateParams.put("description",myDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/marker/addMarker";
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executePost(apiUrl,locationUpdateParams.toString());
            if(resultObj.has("message")){
                return true;
            }
            else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            addTask = null;
            //showProgress(false);

            if (success) {
                osm.getOverlays().clear();
                attemptGetMyMarker();
            } else {
                Toast.makeText(MainActivity.this, "Could not add Marker!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            addTask = null;
            //showProgress(false);
        }
    }
    private void attemptAddMarker(double lat, double lon, String description) {
        if (addTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            addTask = new MainActivity.addMarkerTask(myUsername, lat,lon, description);
            addTask.execute((Void) null);
        }
    }
    public class deleteMarkerTask extends AsyncTask<Void, Void, Boolean> {

        private final String id;
        deleteMarkerTask(String id) {
            this.id = id;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("id", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/marker/deleteMarker";
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executeDelete(apiUrl,locationUpdateParams.toString());
            try {
                if(resultObj.get("message")!=null){
                    return true;
                }
                else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            deleteTask = null;
            //showProgress(false);

            if (success) {
                osm.getOverlays().clear();
                attemptGetMyMarker();
            } else {
                Toast.makeText(MainActivity.this, "Could not delete Marker!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            deleteTask = null;
            //showProgress(false);
        }
    }
    private void attemptDeleteMarker(String id) {

        if (deleteTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            // do nothing
        } else {

            deleteTask = new MainActivity.deleteMarkerTask(id);
            deleteTask.execute((Void) null);

        }
    }
    public class getRequestsTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;


        getRequestsTask(String username) {
            myUsername = username;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/getRequests/"+myUsername;
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executeGet(apiUrl);
            if(resultObj == null){
                return false;
            }
            else{
                resultRequests = resultObj;
                return true;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            requestsTask = null;
            //showProgress(false);

            if (success) {
                requests.clear();
                if(resultRequests.has("sentRequests")){
                    try {
                        JSONArray sentReq = resultRequests.getJSONArray("sentRequests");
                        for (int i = 0; i< sentReq.length(); i++){
                            requests.add("Y#"+sentReq.get(i).toString());
                        }
                        JSONArray openReq = resultRequests.getJSONArray("openRequests");
                        for (int i = 0; i< openReq.length(); i++){
                            requests.add("F#"+openReq.get(i).toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(requests.size()>0){
                    requestButton.setVisibility(View.VISIBLE);
                }
                else{
                    requestButton.setVisibility(View.INVISIBLE);
                }
            }
            else{
                Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
            }
            resultRequests = null;
        }

        @Override
        protected void onCancelled() {
            requestsTask = null;
            //showProgress(false);
        }
    }

    private void attemptGetRequests() {
        if (requestsTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            requestsTask = new MainActivity.getRequestsTask(myUsername);
            requestsTask.execute((Void) null);
        }
    }
}
