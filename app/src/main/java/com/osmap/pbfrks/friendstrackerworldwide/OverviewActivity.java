package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * OverviewActivity is used for displaying marker obejects from personal markers, marker of friends and friend positions in a
 * list and enabling a button to click on for centering the map of the MainActivity on the preferred item
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */
public class OverviewActivity extends AppCompatActivity implements OverviewAdapter.IStringPass {
    /** String used for communication between activities */
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    /** ArrayList containing all required marker information for proper display */
    private ArrayList<String> markerInformation;
    /** Intent of the activity that called OverviewActivity */
    private Intent intent;
    /** Used for passing information to another activity */
    private Intent newIntent;
    /** UI reference */
    private Button backButton;
    /** ArrayAdapter used for custom representation of marker list entries */
    private OverviewAdapter myOverviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        markerInformation = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE3);
        backButton = (Button) findViewById(R.id.backOverviewButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        myOverviewAdapter = new OverviewAdapter(OverviewActivity.this,markerInformation);
        ListView notificationListView = (ListView) findViewById(R.id.listOverviewList);
        notificationListView.setAdapter(myOverviewAdapter);
    }

    /**
     * Interface used for updating the centered object in the MainActivity.
     * The information of the content string is given by the OverviewAdapter
     * @param content - Marker information used for centering by sendint it to the MainActivity
     */
    @Override
    public void passString(String content) {
        newIntent = new Intent();
        newIntent.putExtra(EXTRA_MESSAGE3, content);
        setResult(RESULT_OK, newIntent);
        finish();
    }

}
