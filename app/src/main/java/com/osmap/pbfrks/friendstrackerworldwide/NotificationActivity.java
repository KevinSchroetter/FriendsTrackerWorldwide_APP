package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NotificationActivity extends AppCompatActivity implements NotificationAdapter.IStringPass {
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    ArrayList<String> markerInformation;
    Intent intent;
    Intent newIntent;
    private Button backButton;
    private NotificationAdapter myNotificationAdapter;
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

        if(markerInformation.size()>1) {
            Collections.sort(markerInformation, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    String dist1 = s1.substring(2);
                    String[] splitted1 = dist1.split("km");
                    double d1 = Double.parseDouble(splitted1[0]);
                    String dist2 = s2.substring(2);
                    String[] splitted2 = dist2.split("km");
                    double d2 = Double.parseDouble(splitted2[0]);
                    return Double.compare(d1, d2);
                }
            });
        }

        myNotificationAdapter = new NotificationAdapter(NotificationActivity.this,markerInformation);
        //return inflater.inflate(R.layout.activity_friend, container, false);
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
