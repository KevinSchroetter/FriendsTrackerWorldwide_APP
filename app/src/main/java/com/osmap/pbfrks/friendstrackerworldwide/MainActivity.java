package com.osmap.pbfrks.friendstrackerworldwide;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity {
    private MapView osm;
    private MapController mc;
    private TextView textGeoLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    Location location;
    private String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        myUsername = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        osm = (MapView) findViewById(R.id.mapView);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);
        mc = (MapController) osm.getController();
        textGeoLocation = (TextView) findViewById(R.id.geoLocationText);
        Button resetButton = (Button) findViewById(R.id.buttonReset);
        resetButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                osm.getOverlays().clear();
                osm.invalidate();
                Toast.makeText(MainActivity.this, "Map resetted", Toast.LENGTH_SHORT).show();
            }
        });
        Button updateButton = (Button) findViewById(R.id.buttonUpdate);
        updateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

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
                textGeoLocation.setText("Latitude: "+ updatedPoint.getLatitude()+"\nLongitude: "+updatedPoint.getLongitude());
                addMyMarker(updatedPoint);
                mc.setZoom(14);
                mc.animateTo(updatedPoint);

                GeoPoint newPoint = new GeoPoint(48.5684, 9.33708);
                Marker marker = new Marker(osm);
                marker.setPosition(newPoint);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                osm.getOverlays().add(marker);
                osm.invalidate();
                textGeoLocation.append("\n1");
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
        location = locationManager.getLastKnownLocation(GPS_PROVIDER);
    }

    public void addMyMarker(GeoPoint point){
        Marker marker = new Marker(osm);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        /*
         * You can add ADDITIONAL MARKER IMAGES by adding PNG files to the path:
         * app/src/main/res/drawable
         */
        marker.setIcon(ContextCompat.getDrawable(this, R.drawable.position));
        marker.setTitle("YOU");
        osm.getOverlays().clear(); //maybe remove this for adding additional markers
        osm.getOverlays().add(marker);
        osm.invalidate();
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
                Toast.makeText(this, "Friends selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings_notifications:
                Toast.makeText(this, "Notifications selected", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}
