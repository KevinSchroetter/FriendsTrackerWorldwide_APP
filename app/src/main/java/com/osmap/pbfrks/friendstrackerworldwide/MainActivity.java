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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * The main activity and main screen of the FriendsTrackerWorldwide Application
 * This is used for navigating on the map, interacting on the map and invoking diffrent
 * functionalities from other activities such as friend management, request management and marker overview management
 * It also provides functionality for getting information about locations and markers through the FriendsTrackerWorldwide API
 * as well as adding and removing markers by also using the FTW API
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */

public class MainActivity extends AppCompatActivity implements MapEventsReceiver {
    /** Strings used for sending data between activities */
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    public static final String EXTRA_MESSAGE4 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE4";

    /** UI references */
    private MapView osm;
    private MapController mc;
    private TextView textGeoLocation;
    private Button updateButton;
    private Button centerButton;
    private Button requestButton;

    /**
     * An overlay that is set underneath the normal map overlay for making longtapping possible on the map
     * without interfering with the original marker overlay
     */
    private MapEventsOverlay mapEventsOverlay;
    /** Used for updating personal marker HashMaps after adding or deleting personal markers */
    private int addMarkerStatus = 0;
    /** Used for creating a dialog when a marker is attempted to be deleted */
    private AlertDialog.Builder deleteMarkerMessageBuilder;
    /** Used for displaying a dialog when a marker is attempted to be deleted */
    private AlertDialog deleteMarkerMessage;
    /** Used for creating and displaying a custom a dialog when a marker is attempted to be created and added to the map using a textfield for input */
    private AlertDialog.Builder addMarkerMessageBuilder;
    /** Used for creating a marker object out of latitude and longitide information received from clicking on a map */
    private GeoPoint addMarkerPoint;
    /** Used for entering a description when creating a new personal marker on the map */
    private String descriptionText;

    /** Overall user information */
    /** The username received from the LoginActivity */
    private String myUsername;
    /** The users latitude received when updating the GPS-Location of the mobile device */
    private double myLatitude = 10000;
    //* The users longitude received when updating the GPS-Location of the mobile device */
    private double myLongitude = 10000;
    /**
     * The users geo point created out of myLongitude and myLatitude when updating GPS-Location of the mobile device
     *  Also used for creating the own position marker and for centering the view of the OpenStreetMap
     */
    private GeoPoint myGeoPoint;
    /** The users marker object created by the GeoPoint and used for drawing it on the map */
    private Marker myMarker;
    /** A List of all friends names used to send the friendname information to the FriendActivity */
    protected ArrayList<String> myFriendsArrayList;
    /** All markes of the friends locations received by the FTW API. Used for calculating distances between friends and the users position */
    private HashMap<String, Marker> myFriends;
    /** Location of all Friends received by the FTW API used for drawing the friends positions on the map */
    private HashMap<String, Marker> myFriendsLocationMarker;
    /** Location of all Friends markers received by the FTW API used for drawing the friends markers on the map */
    private HashMap<String, Marker> myFriendsMarkerMarker;
    /** Location of personal markers received by the FTW API used for drawing the personal markers on the map */
    private HashMap<String, SpecialMarker> myMarkerMarker;
    /** Used for calculating distances between all objects. They will be stored in this HashMap and used for centering the map view on that a chosen marker used in OverviewActivity */
    private HashMap <String, Marker> myOverviews;
    /** Used for receiving friend request information from the FTW API and sending the information to the RequestActivity */
    private ArrayList<String> requests;

    /** Used for displaying an animated progress dialog when the app is waiting for GPS-Data of the mobile device on initial APP load */
    private ProgressDialog dialog;

    /** Used for the initialization process for loading all initial data required by the app */
    private int initialization = 0;
    /** Used for displaying a dialog after initialization is complete */
    private int initializationMessage = 0;
    /** Used for creating a dialog after initialization is complete, when the initializationMessage is greater than 0 */
    private AlertDialog.Builder initDisplayMessageBuilder;
    /** Used for displaying a dialog after initialization is complete */
    private AlertDialog initDisplayMessage;

    /** Used for performing API GET, POST, PUT and DELETE calls in tasks */
    private ApiCaller apiCaller;

    /** Tasks used for performing API calls using the ApiCaller in background tasks */
    private updateMyLocationTask myLocTask = null;
    private getMyFriendsTask myFriendsTask = null;
    private getMyFriendsMarkerTask myFriendsMarkerTask = null;
    private getMyMarkerTask myMarkerTask = null;
    private addMarkerTask addTask = null;
    private deleteMarkerTask deleteTask = null;
    private getRequestsTask requestsTask = null;
    private mySortingTask mySortTask = null;

    /** JSONObjects used for storing results of FTW API calls */
    private JSONObject resultFriends;
    private JSONObject resultFriendsMarker;
    private JSONObject resultMyMarker;
    private JSONObject resultRequests;

    /** Used for enabling drag events that simulate a long tap on markers. Used for marker deletion */
    private Marker.OnMarkerDragListener markerListener;
    /** Used for casting a special marker to a normal marker for a drag event on markers */
    private SpecialMarker castedMarker;

    /** Used for casting a normal marker to a special marker for calculating distances between the own position and other markers from friends and personal markers */
    private Marker castedMyMarker;

    /** Used for sending marker information to the OverviewsActivity */
    private ArrayList<String> sendIt;

    /** Used for updating GPS-Location of the mobile device */
    private LocationManager locationManager;
    /** Required for the LocationManager */
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Strict mode used for enabling internet calls in the MainActivity
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // Initializing required variables
        apiCaller = new ApiCaller();
        myFriends = new HashMap<String, Marker>();
        myFriendsArrayList = new ArrayList<String>();
        myFriendsLocationMarker = new HashMap<String, Marker>();
        myFriendsMarkerMarker = new HashMap<String, Marker>();
        myMarkerMarker = new HashMap<String,SpecialMarker>();
        myOverviews = new HashMap<String, Marker>();
        requests = new ArrayList<String>();
        sendIt = new ArrayList<String>();
        deleteMarkerMessageBuilder = new AlertDialog.Builder(MainActivity.this);
        addMarkerMessageBuilder = new AlertDialog.Builder(MainActivity.this);

        /**
         * DragListener used for simulating a long tap on a marker.
         * This listener is added to every personal marker object
         */
        markerListener = new Marker.OnMarkerDragListener(){

            @Override
            public void onMarkerDrag(Marker marker) {
                // Not required
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // Not required
            }

            /**
             * Method that attempts the deletion of a personal marker showing a dialog first
             * @param marker - The marker object that shall be deleted
             */
            @Override
            public void onMarkerDragStart(Marker marker) {
                castedMarker = (SpecialMarker) marker;

                deleteMarkerMessageBuilder.setTitle("Delete your Marker?");
                deleteMarkerMessageBuilder.setMessage("Attempting to delete your Marker: \n"+marker.getTitle());
                deleteMarkerMessageBuilder.setCancelable(false);
                deleteMarkerMessageBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Change the status for notifying the app to reload personal marker information
                        addMarkerStatus = 1;
                        //Call the delete marker method to actually try deleting the marker
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

        // Create the initialization message
        initDisplayMessageBuilder = new AlertDialog.Builder(MainActivity.this);
        initDisplayMessageBuilder.setTitle("INITIALIZATION COMPLETE");
        initDisplayMessageBuilder.setMessage("Updated your GPS-Location...\nUpdated your Markers...\nUpdated your Friendlist...\nUpdated Markers of your Friends...");
        initDisplayMessageBuilder.setCancelable(false);
        initDisplayMessageBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing, only there for confirming, so that the message wont disappear by itself
            }
        });
        // Creating the initialization message to enably the .show() call for displaying the message
        initDisplayMessage = initDisplayMessageBuilder.create();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Getting the intent information of the LoginActivity that called the MainActivity
        Intent intent = getIntent();
        // Receiving the username from LoginActivity
        myUsername = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        // Initializing buttons using the corresponding elements from activity_main.xml
        centerButton = (Button) findViewById(R.id.buttonCenter);
        updateButton = (Button) findViewById(R.id.buttonUpdate);
        requestButton = (Button) findViewById(R.id.buttonRequest);
        // Initializing the text field above the map using the corresponding element form activity_main.xml
        textGeoLocation = (TextView) findViewById(R.id.geoLocationText);
        // Hiding all button when there are no open friend requests
        if(requests.size()<=0){
            requestButton.setVisibility(View.INVISIBLE);
        }
        // Hiding the rest of the buttons and creating/showing the loading dialog until the app got GPS-Data from the mobile device using the LocationManager
        if(initialization==0) {
            updateButton.setVisibility(View.INVISIBLE);
            centerButton.setVisibility(View.INVISIBLE);
            dialog = new ProgressDialog(this);
            dialog.setMessage("Please wait until we got your GPS Data...");
            dialog.setCancelable(false);
            dialog.show();
        }
        // Creating the main mapView using OpenStreetMap from the apps' gradle
        osm = (MapView) findViewById(R.id.mapView);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);
        // Initializing the casted my marker for later usage
        castedMyMarker = new Marker(osm);
        // Creating the MapController from OpenStreetMap
        mc = (MapController) osm.getController();
        // Creating a new overlay used for tapping on the map in a background overlay
        mapEventsOverlay = new MapEventsOverlay(this, this);
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
        // Creating the LocationManager using the LOCTAION_SERVICE of the device
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            /**
             * This method will always be called when the mobile device update its' GPS-Information
             * It will then create a new GeoPoint object using the location information and add a marker made out of it to the map
             * When the app started and the initialization is not yet finished, this method will call attemptGetMyMarker which is
             * chained to severel other methods for updating all neccessary accountinformation including friends, markers, friendmarkers and requests
             * If the initialization process is already complete, the method will instead call the drawMarker method to draw all marker out of the already
             * received information
             * @param location the GPS location data sent from the mobile device
             */
            @Override
            public void onLocationChanged(Location location) {
                GeoPoint updatedPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                myLatitude = location.getLatitude();
                myLongitude = location.getLongitude();
                myGeoPoint = updatedPoint;
                textGeoLocation.setText("Hello "+myUsername+"!\nLatitude: "+ String.format("%.6f", myLatitude)+"\nLongitude: "+String.format("%.6f", myLongitude));
                // add the own marker to the map and center onto its position
                addMyMarker();
                mc.setZoom(14);
                if (initialization == 0){
                    // when inizialisation not yet complete, finish it and show all buttons and start the attemptGetMyMarker chain for updating account information
                    updateButton.setVisibility(View.VISIBLE);
                    centerButton.setVisibility(View.VISIBLE);
                    attemptGetMyMarker();
                    dialog.dismiss();
                    initialization = 1;
                }else{
                    // Redraw all markers using already received marker, friend and friend marker information
                    drawMarker();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Not used
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Not used
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Not used
            }
        };
        // Calling show map for checking for GPS permissions before attempting to update GPS location via a locationListener
        showMap();
    }

    /**
     * This method calls the showMap method to start the GPS locationListener when the app is installed for the first time and permission was granted.
     * ShowMap later checks permissions again since it is required in android Build version above 23 and then starts the GPS-Location listener when permissions are okay
     * @param requestCode - The requested code for the permission
     * @param permissions - The requested permission
     * @param grantResults - granted results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        // When requested selfPermissions finish with request code 10, the show map will finally start to update GPS-Data
        switch (requestCode) {
            case 10:
                showMap();
                break;
            default:
                break;
        }
    }

    /**
     * This method inflates the menu button on the top right of the app
     * @param menu - a menu object, that is built in the application layout theme
     * @return - true, when everything was fine
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This method manages the menu button.
     * @param item - The menu item that the user clicked on
     * @return - true when the item exists to continue
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Clicking on the "Friends" button
            case R.id.action_settings_friends:
                // Creating new intent for FriendActivity
                Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                // Sending the friend names to the FriendActivity
                intent.putExtra(EXTRA_MESSAGE,myFriendsArrayList);
                // Sending the username to the FriendActivity
                intent.putExtra(EXTRA_MESSAGE2,myUsername);
                // Starting the FriendActivity and waiting for its result call
                startActivityForResult(intent, 3);
                break;
            // Clicking on the "Marker Overview" button
            case R.id.action_settings_nearby:
                // Clearing the information of all friends, markers and personal marker data
                sendIt.clear();
                // Clearing the information of all friends, markers and personal markers data in myOverviews
                myOverviews.clear();
                // Callin updateOverviews to refill the cleared arrays and start the OverviewActivity via updateOverviews methode
                updateOverviews();
                break;
            // Clicking on the "Legend" button
            case R.id.action_settings_legend:
                //Creating a new intent for LegendActivity
                Intent inte = new Intent(MainActivity.this, LegendActivity.class);
                // Starting the LegendActivity without waiting for a response
                startActivity(inte);
                break;
            // Clicking on the "Close App" button
            case R.id.action_settings_close:
                // Closing MainActivity and return to LoginActivity
                System.exit(0);
        }
        return true;
    }

    /**
     * Unused Method from the MapEvents overlay for singleTapping on the map
     * @param p - GeoPoint received from the tap
     * @return - false, because method is not used
     */
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        //Doing nothing. Is not used
        return false;
    }

    /**
     * This method is used for enabling a long tap on the maps MapEvents overlay
     * It is used for showing an add dialog and invoking attemptAddMarker method for adding a marker and saving it in the database using ApiCaller
     * @param p - The GeoPoint received from tapping on the map
     * @return - true to confitm the tap
     */
    @Override
    public boolean longPressHelper(GeoPoint p) {
        //Create a marker out of the GeoPoint and add description and title
        addMarkerPoint = p;
        addMarkerMessageBuilder.setTitle("Add Marker");
        addMarkerMessageBuilder.setMessage("Enter description:");
        // Create an input text field for building a custom dialog that has a textfield
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        input.setLayoutParams(lp);
        addMarkerMessageBuilder.setView(input);
        addMarkerMessageBuilder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                addMarkerStatus = 1;
                descriptionText = input.getText().toString();
                // Call the attemptAddMarker method for communication with the API and adding a new marker on the map
                attemptAddMarker((double)addMarkerPoint.getLatitude(), (double)addMarkerPoint.getLongitude(),descriptionText);
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

    /**
     * This method is used for communication with other activities.
     * When an activity is called using StartActivityForResult, they can send a result to the invoking
     * activity, in this case to the MainActivity.
     * Then the MainActivity uses that information to continue using the app in different ways
     * depending on the result
     * @param requestCode - The request code sent by the other called activity determining the request code called by the MainActivity
     * @param resultCode - The result status of the called activity
     * @param data - the intent that sent the resultCode
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        // ReqiestCode 3 was used for calling FriendActivity
        if(requestCode == 3){
            if(resultCode == RESULT_OK){
                attemptGetFriends();
            }
        }
        // RequestCode 4 was used for calling OverviewActivity
        if(requestCode == 4){
            if(resultCode == RESULT_OK){
                // Receiving a String indicating a marker where the user clicked on "SHOW" button of the OverviewActivity
                String targetMarker = data.getStringExtra(EXTRA_MESSAGE3);
                mc.animateTo(myOverviews.get(targetMarker).getPosition());
            }
        }
        // RequestCode 5 was used for calling RequestActivity
        if(requestCode == 5){
            if(resultCode == RESULT_OK){
                attemptGetFriends();
            }
        }

    }

    /**
     * This method checks the permissions for using the GPS-Data of the mobile device
     * If the permission is granted, the app can continue to operate
     * If not, the app will do nothing
     */
    public void showMap(){
        // first check for GPS-Location permissions and used android Build version
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
        locationManager.requestLocationUpdates("gps", 30000, 0, locationListener);
    }

    /**
     * Used for creating a marker for the users position using the myGeoPoint filled with data from the location update
     * After creation, the method will draw the marker into the marker overlay of the OpenStreetMap mapView and set a title
     * indicating that it is the marker of the user himself
     */
    public void addMyMarker(){
        Marker marker = new Marker(osm);
        marker.setPosition(myGeoPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.position));
        marker.setTitle("YOU");
        myMarker = marker;
    }

    /**
     * This method updates all information regarding friends, markers and personal markers by calling
     * attemptUpdateLocation() method which is chained to other attempt methods
     */
    private void updateMap() {
        attemptUpdateLocation();
    }

    /**
     * This method clears all overlays on the map and then proceeds
     * to redraw the usres marker, friend position markers, friend markers and personal markers as well as
     * adding the mapEventsOverlay again to enable long taps on the map
     */
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

    /**
     * Represents an asynchronous task used to update the users latitude and longitude calling the FTW API
     */
    public class updateMyLocationTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the variables used for the update
        private final String myUsername;
        private final double myLatitude;
        private final double myLongitude;

        updateMyLocationTask(String username, double latitude, double longitude) {
            myUsername = username;
            myLatitude = latitude;
            myLongitude = longitude;
        }

        /**
         * Method used for performing a background task that sends information from the App to the API for updating user location
         * Performing user location update
         * @param params - a username, latitude and longitude that will be sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject locationUpdateParams = new JSONObject();
            // Adding required variables to the JSONObject array
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("latitude", myLatitude);
                locationUpdateParams.put("longitude", myLongitude);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for update user location
            String apiUrl = LoginActivity.URL+"/api/user/updateUser";
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a PUT request to the apiUrl destination
            resultObj = apiCaller.executePut(apiUrl,locationUpdateParams.toString());
            // Handling the results returned by the FTW API - either "message" or "err"
            if(resultObj.has("message")){
                return true;
            }
            else{
                return false;
            }
        }

        /**
         * Method called after the background task for further execution of the application
         * Clearing the map overlays on success and updating data with attemptGetFriends chain
         * @param success return statement from doInBackground()
         */
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

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            myLocTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to update Friend information calling the FTW API
     */
    public class getMyFriendsTask extends AsyncTask<Void, Void, Boolean> {
        private final String myUsername;
        getMyFriendsTask(String username) {
            myUsername = username;
        }

        /**
         * Method used for performing a background task that sends information from the App to the API for updating Friends
         * @param params - a username that will be sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining the API route called for getting friends with their positions
            String apiUrl = LoginActivity.URL+"/api/user/getFriendsLocation/"+myUsername;
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a GET request to the apiUrl destination
            resultObj = apiCaller.executeGet(apiUrl);
            // Handling the results returned by the FTW API
            if(resultObj == null){
                return false;
            }
            else{
                resultFriends = resultObj;
                return true;
            }
        }

        /**
         * Method called after the background task for further execution of the application
         * It adds all friend related information to the corresponding HashMaps and calls attemptGetRequests and attemptGetFriendsMarker
         * at the end to update neccessary information
         * @param success return statement from doInBackground()
         */
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

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            myFriendsTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to update friend marker information calling the FTW API
     */
    public class getMyFriendsMarkerTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;

        getMyFriendsMarkerTask(String username) {
            myUsername = username;
        }

        /**
         * Method used for performing a background task that sends information from the App to the API for updating Friends Markers
         * @param params - a username that will be sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining the API route called for gettubg friend markers
            String apiUrl = LoginActivity.URL+"/api/marker/getFriendsMarker/"+myUsername;
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a GET request to the apiUrl destination
            resultObj = apiCaller.executeGet(apiUrl);
            // Handling the results returned by the FTW API
            if(resultObj == null){
                return false;
            }
            else{
                resultFriendsMarker = resultObj;
                return true;
            }
        }

        /**
         * Method called after the background task for further execution of the application
         * Used for filling friend related data in the specific HashMaps
         * Also showing a Dialog when initialization process was started and endet as well as a Toast when the method was called out of
         * an initialization process for giving feedback to the user
         * Calls drawMarker at the end for updating the map
         * @param success return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myFriendsMarkerTask = null;
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

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            myFriendsMarkerTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to update the users markers calling the FTW API
     */
    public class getMyMarkerTask extends AsyncTask<Void, Void, Boolean> {
        private final String myUsername;
        getMyMarkerTask(String username) {
            myUsername = username;
        }

        /**
         * Method used for performing a background task that sends information from the App to the API for the users markers
         * @param params - a username that will be sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining the API route called for getting personal markers
            String apiUrl = LoginActivity.URL+"/api/marker/getMyMarker/"+myUsername;
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a GET request to the apiUrl destination
            resultObj = apiCaller.executeGet(apiUrl);
            // Handling the results returned by the FTW API
            if(resultObj == null){
                return false;
            }
            else{
                resultMyMarker = resultObj;
                return true;
            }
        }

        /**
         * Method called after the background task for further execution of the application
         * Filling the myMarkerMarker array with all information of the personal markers received from the API
         * Also creates SpecialMarker objects instead of Marker obejcts to add the id sent by the API to the marker
         * which is used for deleting the marker in another task
         * This method also calls attemptUpdateMyLocation for chaining the methods into each other when initialization was made
         * or a new marker has been added / removed from the map
         * @param success return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myMarkerTask = null;
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

        /**
         * Handling when the task is cancelled
         */
        @Override
        protected void onCancelled() {
            myMarkerTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to add a marker calling the FTW API
     */
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

        /**
         * Method used for performing a background task that sends information from the App to the API for adding a marker
         * @param params - a owner, latitude of a marker, longitude of a marker and description of a marker that will be sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject locationUpdateParams = new JSONObject();
            // Adding required variables to the JSONObject array
            try {
                locationUpdateParams.put("owner", myUsername);
                locationUpdateParams.put("latitude", myLatitude);
                locationUpdateParams.put("longitude", myLongitude);
                locationUpdateParams.put("description",myDescription);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for for adding a marker
            String apiUrl = LoginActivity.URL+"/api/marker/addMarker";
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a POST request to the apiUrl destination
            resultObj = apiCaller.executePost(apiUrl,locationUpdateParams.toString());
            // Handling the results returned by the FTW API - either "message" or "err"
            if(resultObj.has("message")){
                return true;
            }
            else{
                return false;
            }
        }

        /**
         * Method called after the background task for further execution of the application
         * Clearing the map overlays on success and updating all data with attemptGetMyMarker chain
         * This mehtod also calls attemptGetMyMarker for starting the update process of all information the app requires
         * @param success return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            addTask = null;
            if (success) {
                osm.getOverlays().clear();
                attemptGetMyMarker();
            } else {
                Toast.makeText(MainActivity.this, "Could not add Marker!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            addTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to delete a marker calling the FTW API
     */
    public class deleteMarkerTask extends AsyncTask<Void, Void, Boolean> {

        private final String id;
        deleteMarkerTask(String id) {
            this.id = id;
        }

        /**
         * Method used for performing a background task that sends information from the App to the API for deleting a users marker
         * @param params - a marker id that is sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject locationUpdateParams = new JSONObject();
            // Adding required variables to the JSONObject array
            try {
                locationUpdateParams.put("id", id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for deleting a marker
            String apiUrl = LoginActivity.URL+"/api/marker/deleteMarker";
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a DELETE request to the apiUrl destination
            resultObj = apiCaller.executeDelete(apiUrl,locationUpdateParams.toString());
            // Handling the results returned by the FTW API - either "message" or "err"
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

        /**
         * Method called after the background task for further execution of the application
         * Clearing the overlays and updating all data using attemptGetMyMarker chain
         * @param success return statement from doInBackground()
         */
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

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            deleteTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to update the users friend requests calling the FTW API
     */
    public class getRequestsTask extends AsyncTask<Void, Void, Boolean> {
        private final String myUsername;
        getRequestsTask(String username) {
            myUsername = username;
        }

        /**
         * Method used for performing a background task that sends information from the App to the API for updateing requests
         * @param params - a username that will be sent to the API
         * @return - true when the API is reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining the API route called for getting all friend request related information
            String apiUrl = LoginActivity.URL+"/api/user/getRequests/"+myUsername;
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a GET request to the apiUrl destination
            resultObj = apiCaller.executeGet(apiUrl);
            // Handling the results returned by the FTW API
            if(resultObj == null){
                return false;
            }
            else{
                resultRequests = resultObj;
                return true;
            }
        }

        /**
         * Method called after the background task for further execution of the application
         * Adding data to requests array dividing information of the result depending on being
         * a sentRequest or a openRequest
         * If the new results array has something inside, the "Request" button will be visible, if not
         * it will be hidden
         * @param success return statement from doInBackground()
         */
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

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            requestsTask = null;
            //showProgress(false);
        }
    }

    /**
     * Represents an asynchronous task used to sort all markers in the myOverviews array by distance
     */
    public class mySortingTask extends AsyncTask<Void, Void, Boolean> {
        private ArrayList<String> arrList;

        mySortingTask(ArrayList<String> arrList) {
            this.arrList = arrList;

        }

        /**
         * Asynchronous task used to sort all Overview elements (friends, friend markers and personal markers)
         * Using a Comperator
         * @param params - arrList representing myOverviews
         * @return true when everything was okay
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Collections.sort(arrList, new Comparator<String>() {
                    public int compare(String s1, String s2) {
                        String dist1 = s1.substring(2);
                        String[] splitted1 = dist1.split("km");
                        String save1 = splitted1[0].replace(',', '.');
                        double d1 = Double.parseDouble(save1);
                        String dist2 = s2.substring(2);
                        String[] splitted2 = dist2.split("km");
                        String save2 = splitted2[0].replace(',', '.');
                        double d2 = Double.parseDouble(save2);
                        return Double.compare(d1, d2);
                    }
                });
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
            return true;
        }

        /**
         * Method called after the background task for further execution of the application
         * Starting the OverviewActivity and awaiting its result
         * @param success return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            mySortTask = null;
            if (success) {
                Intent myIntent = new Intent(MainActivity.this, OverviewActivity.class);
                myIntent.putStringArrayListExtra(EXTRA_MESSAGE3, sendIt);
                startActivityForResult(myIntent, 4);
            } else {
                // Do nothing
            }
        }

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            mySortTask = null;
        }
    }

    /**
     * Method that calls the updateMyLocationTask for updating a users location
     */
    private void attemptUpdateLocation() {
        if (myLocTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {
            myLocTask = new MainActivity.updateMyLocationTask(myUsername, myLatitude, myLongitude);
            myLocTask.execute((Void) null);
        }
    }

    /**
     * Method that calls the getMyFriendsTask for updating friends
     */
    private void attemptGetFriends() {
        if (myFriendsTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {
            myFriendsTask = new MainActivity.getMyFriendsTask(myUsername);
            myFriendsTask.execute((Void) null);
        }
    }

    /**
     * Method used for updating the friends markers by calling the getMyFriendsMarkerTask
     */
    private void attemptGetFriendsMarker() {
        if (myFriendsMarkerTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {
            myFriendsMarkerTask = new MainActivity.getMyFriendsMarkerTask(myUsername);
            myFriendsMarkerTask.execute((Void) null);
        }
    }

    /**
     * Method that calls the getMyMarkerTask for updating the users markers using the FTW API
     */
    private void attemptGetMyMarker() {
        if (myMarkerTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {
            myMarkerTask = new MainActivity.getMyMarkerTask(myUsername);
            myMarkerTask.execute((Void) null);
        }
    }

    /**
     * Method used for sorting an arraylist by a distance value
     * @param markerInformation - an array containing strings that contain a special format with a distance included as a string as well
     */
    private void attemptSortInformation(ArrayList<String> markerInformation) {
        if (mySortTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {
            mySortTask = new MainActivity.mySortingTask(markerInformation);
            mySortTask.execute((Void) null);
        }
    }

    /**
     * Method that calls the getRequestsTask for updating the requests
     */
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

    /**
     * Method that calls the addMarkerTask for adding a marker using the FTW API
     * @param lat - latitude of the new marker
     * @param lon - longitude of the new marker
     * @param description - description of the new marker
     */
    private void attemptAddMarker(double lat, double lon, String description) {
        if (addTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            Toast.makeText(MainActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {
            addTask = new MainActivity.addMarkerTask(myUsername, lat,lon, description);
            addTask.execute((Void) null);
        }
    }

    /**
     * Method that calls the deleteMarkerTaks for deleting a marker
     * @param id - id of the marker that should be deleted
     */
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

    /**
     * Method used for updating the myOverviews array
     * Uses the friends, friends marker and personal marker information, calculates the distance of those points to the users geo location and
     * saves the results in the myOverviews array. After filling the array, attemptSortInformation is called to sort the input values by their distances
     */
    private void updateOverviews() {
        String distance = "";
        if(myMarkerMarker.size()>0) {
            for (HashMap.Entry<String, SpecialMarker> mymarker : myMarkerMarker.entrySet()) {
                distance = distanceBetween(myLatitude, myLongitude, mymarker.getValue().getPosition().getLatitude(), mymarker.getValue().getPosition().getLongitude());
                castedMyMarker = new Marker(osm);
                castedMyMarker.setPosition(mymarker.getValue().getPosition());
                castedMyMarker.setTitle("mm" + distance + " km:\n" + mymarker.getValue().getTitle());
                myOverviews.put(castedMyMarker.getTitle(),castedMyMarker);
            }
        }
        if(myFriends.size()>0){
            for (HashMap.Entry<String, Marker> friend: myFriends.entrySet()){
                distance = distanceBetween(myLatitude, myLongitude, friend.getValue().getPosition().getLatitude(), friend.getValue().getPosition().getLongitude());
                myOverviews.put("fp" + distance + " km:\n" + friend.getKey(), friend.getValue());
            }
        }
        if(myFriendsMarkerMarker.size()>0){
            for( HashMap.Entry<String, Marker> friendMarker: myFriendsMarkerMarker.entrySet()){
                distance = distanceBetween(myLatitude, myLongitude, friendMarker.getValue().getPosition().getLatitude(), friendMarker.getValue().getPosition().getLongitude());
                myOverviews.put("fm" + distance + " km:\n"+friendMarker.getKey(), friendMarker.getValue());
            }
        }

        if(myOverviews.size()>0) {
            for (HashMap.Entry<String, Marker> marker : myOverviews.entrySet()) {
                sendIt.add(marker.getKey());
            }
        }
        attemptSortInformation(sendIt);
    }

    /**
     * Method that calculates a distance in km between to geoPoints using latitudes and longitudes from two separated points
     * Using the arcuscosinus and the Radius of the world defined as 1.609344 km
     * Further information of this calculation can be found in the FriendsTrackerWorldwide documentation!
     * @param lat1 - latitude of the first point
     * @param lon1 - longitude of the first point
     * @param lat2 - latitude of the second point
     * @param lon2 - longitude of the second point
     * @return - calculated distance in km between the given lat-lon-pairs
     */
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

    /**
     * Method used for converting deg to rad
     * @param deg - the deg value that shall be converted
     * @return - to rad converted deg value
     */
    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }

    /**
     * Method used for converting rad to deg
     * @param rad - the rad value that shall be converted
     * @return - to deg converted rad value
     */
    private double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }
}
