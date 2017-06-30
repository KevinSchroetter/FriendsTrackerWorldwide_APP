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
 * Activity used for displaying markers of friends, personal markers and friend positions in a list
 * with a clickable button for showing and displaying the specific item on the map
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */
public class RequestActivity extends AppCompatActivity {
    /** String used for communication between activities */
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    public static final String EXTRA_MESSAGE4 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE4";
    /** ArrayList containing all information of markers that this activity displays as Strings */
    private ArrayList<String> myRequests;
    /** Reference to the activity where the call of RequestActivity came from */
    private Intent intent;
    /** Username given by MainActivity */
    private String myUsername;
    /** Integer used for updating information */
    private int checkRequestAmount;
    /** UI references */
    private Button backButton;
    /** Adapter used for displaying markers in a custom list */
    private RequestAdapter requestListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
        myRequests = new ArrayList<String>();
        intent = getIntent();
        // Retrieving information from MainActivity
        myRequests = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE4);
        myUsername = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);
       checkRequestAmount = myRequests.size();
        String test = ""+checkRequestAmount;
        backButton = (Button) findViewById(R.id.backButtonRequests);
        backButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Method used for sending feedback to MainActivity for further computing
             * @param v - View of the item where the user clicked on
             */
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

    /**
     * Adressing the keyevent of the mobile devices button press event.
     * This method in particular is for the "back" button of the mobile device, which should be
     * disabled here because the app is not supposed to only close the activity, but it SHOULD
     * perform an update of friends information by using the extra designed "BACK TO MAP" button
     * @param keyCode - Pressed key
     * @param event - keyPress event
     * @return - false for deactivating the button completely
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
