package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.IStringPass {
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    protected ArrayList<String> markerInformation;
    Intent intent;
    private ApiCaller apiCaller = new ApiCaller();
    Intent newIntent;
    String test="lol";
    private Button backButton;
    private NotificationAdapter myNotificationAdapter;
    private ArrayList<MarkerCompareHelper> helperArrayList = new ArrayList<MarkerCompareHelper>();
    private ArrayList<String> resultCalc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        markerInformation = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE3);

        backButton = (Button) findViewById(R.id.backNotificationButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        myNotificationAdapter = new NotificationAdapter(NotificationActivity.this,markerInformation);
        ListView notificationListView = (ListView) findViewById(R.id.listNotificationList);
        notificationListView.setAdapter(myNotificationAdapter);
    }

    @Override
    public void passString(String content) {
        newIntent = new Intent();
        newIntent.putExtra(EXTRA_MESSAGE3, content);
        setResult(RESULT_OK, newIntent);
        finish();
       // Toast.makeText(NotificationActivity.this, content, Toast.LENGTH_SHORT);
    }

}
