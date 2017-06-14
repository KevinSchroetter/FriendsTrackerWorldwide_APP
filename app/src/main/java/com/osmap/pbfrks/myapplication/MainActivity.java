package com.osmap.pbfrks.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import static android.location.LocationManager.GPS_PROVIDER;
import static com.osmap.pbfrks.myapplication.R.id.add;
import static com.osmap.pbfrks.myapplication.R.id.textView1;

public class MainActivity extends AppCompatActivity {
    private MapView osm;
    private MapController mc;
    private TextView textView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Location location;
    GeoPoint myGeoPoint;


    @Override
    /*
     * This method uses the StrictMode Policy settings to allow
     * API calls via the Main Activity
     *
     * It also uses the main.xml as first shown layout
     *
     * It uses the MAPNIK map version of OpenStreetMap for drawing a map
     * and enables zoom-buttons as well as navigating using two fingers
     * on the mobile device
     */
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        osm = (MapView) findViewById(R.id.mapView);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);
        mc = (MapController) osm.getController();
        textView = (TextView) findViewById(R.id.textView1);
        GeoPoint point = new GeoPoint(48.5583, 9.33708);
        Marker marker = new Marker(osm);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
         mc.animateTo(point);
         mc.setZoom(14);
        /*
         * You can add ADDITIONAL MARKER IMAGES by adding PNG files to the path:
         * app/src/main/res/drawable
         */
        // marker.setIcon(ContextCompat.getDrawable(this, R.drawable.position));
        marker.setTitle("YOU");
        osm.getOverlays().clear(); //maybe remove this for adding additional markers
        osm.getOverlays().add(marker);
        osm.invalidate();
       // GeoPoint point = new GeoPoint(48.558299, 9.337079);
        //addMarker(point);
       // mc.animateTo(myGeoPoint);
       // mc.setZoom(14);

        /*
         * This part manages getting gps data whenever the location changes
         */
        //   locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     /*   locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //addMyLocation(location);
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
        showMap();*/
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
         * It the nstarts the locationUpdate requests as well as a first geo-location
         * via the lastKnownLocation
         */
    /*

        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
        location = locationManager.getLastKnownLocation(GPS_PROVIDER);
        addMyLocation(location);
    }
    /*
     * This Method combines myGeoPoint, addMarker and wirting down the longitude and latitude
     */
    /*
    public void addMyLocation(Location location){
        myGeoPoint = createGeoPointByLocation(location);
        addMarker(myGeoPoint);
        mc.animateTo(myGeoPoint);
        textView.setText("Your Longitude: "+location.getLongitude()+"\nYour Latitude: "+location.getLatitude());
        mc.setZoom(14);
    }
    /*
     * This method uses a location object for creating a GeoPoint
     */
    /*
    public GeoPoint createGeoPointByLocation(Location location){
        if(location != null) {
            GeoPoint point = new GeoPoint(location.getLongitude(), location.getLatitude());
            return point;
        }
        return null;
    }
    /*
     * This Method takes a GeoPoint as parameter
     * It then continues to create a Marker Object and setting its poistion
     * to the position of the GeoPoint.
     * Also it anchors the marker and draws it on an overlay of the osm MapView
     *
     * It is important to reload the view using osm.invalidate, otherwise the
     * app would not draw the marker
     */
    public void addMarker(GeoPoint point){
        Marker marker = new Marker(osm);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        /*
         * You can add ADDITIONAL MARKER IMAGES by adding PNG files to the path:
         * app/src/main/res/drawable
         */
       // marker.setIcon(ContextCompat.getDrawable(this, R.drawable.position));
        marker.setTitle("YOU");
        osm.getOverlays().clear(); //maybe remove this for adding additional markers
        osm.getOverlays().add(marker);
        osm.invalidate();
    }

}
