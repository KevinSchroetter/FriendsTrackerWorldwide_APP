package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class RequestActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    public static final String EXTRA_MESSAGE4 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE4";
    protected ArrayList<String> myRequests;
    private Intent intent;
    private String myUsername;
    private int checkRequestAmount;
    private Button backButton;
    private RequestAdapter requestListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        myRequests = new ArrayList<String>();
        intent = getIntent();
        myRequests = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE4);

        myUsername = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);
       checkRequestAmount = myRequests.size();
        String test = ""+checkRequestAmount;
        backButton = (Button) findViewById(R.id.backButtonRequests);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myRequests != null & myRequests.size()!=checkRequestAmount){
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else{
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }

            }
        });
        requestListAdapter = new RequestAdapter(RequestActivity.this,myRequests, myUsername);
        ListView requestListView = (ListView) findViewById(R.id.listRequestList);
        requestListView.setAdapter(requestListAdapter);
    }
}
