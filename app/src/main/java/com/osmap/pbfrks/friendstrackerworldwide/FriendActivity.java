package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import static com.osmap.pbfrks.friendstrackerworldwide.AddFriendActivity.EXTRA_MESSAGE3;

/**
 * The FriendActivity is used for displaying friends in a list as well as adding and removing
 * them by navigating with buttons
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */
public class FriendActivity extends AppCompatActivity {
    /** String used for sending data between activities */
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    /**UI references */
    private Button backButton;
    private Button addButton;
    /** Reference to the activity that started FriendActivity */
    private Intent intent;
    /** ArrayList of friend names given by the MainActivity */
    private ArrayList<String> myFriendsArrayList;
    /** Username of the user given by the MainActivity */
    private String myUsername;
    /** Adapter used for managing the listView */
    private FriendsAdapter friendListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        myFriendsArrayList = new ArrayList<String>();
        // Allocating data from MainActivity for intent, username and friend names
        intent = getIntent();
        myFriendsArrayList = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE);
        myUsername = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);
        addButton = (Button) findViewById(R.id.addFriendButton);
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener(){
            /**
             * This method is used for starting the AddFriendList activity which is supposed to add a friend by calling the FTW API
             * It also sends some required strings to the Activity which are used for the API communication
             * AddFriendActivity is started with the expectation of a result for communication with MainActivity
             * @param v - View of the element that was clicked on
             */
            @Override
            public void onClick(View v){
                Intent newIntent = new Intent(FriendActivity.this, AddFriendActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE,myFriendsArrayList);
                newIntent.putExtra(EXTRA_MESSAGE2,myUsername);
                startActivityForResult(newIntent, 3);
            }
        });
        friendListAdapter = new FriendsAdapter(FriendActivity.this,myFriendsArrayList, myUsername);
        ListView friendListView = (ListView) findViewById(R.id.listFriendList);
        friendListView.setAdapter(friendListAdapter);
    }

    /**
     * This method is used for communication with other activities.
     * When an activity is called awaiting a request, this method will then
     * take the result to inform the ArrayAdapter that the data has changed and is ready
     * for an update by using a string delivered from AddFriendActivity
     * @param requestCode - The request code sent by the other called activity determining the request code called by the FriendActivity
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            String added = data.getStringExtra(EXTRA_MESSAGE3);
            if(added.startsWith("#")){
                myFriendsArrayList.add("Requested "+added);
            }
            else {
                myFriendsArrayList.add(added);
            }
            friendListAdapter.notifyDataSetChanged();
        }
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

