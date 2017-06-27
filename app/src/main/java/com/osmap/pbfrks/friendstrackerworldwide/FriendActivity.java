package com.osmap.pbfrks.friendstrackerworldwide;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;

import static android.R.id.list;
import static com.osmap.pbfrks.friendstrackerworldwide.AddFriendActivity.EXTRA_MESSAGE3;

public class FriendActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    public static final String EXTRA_MESSAGE2 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE2";
    protected ArrayList<String> myFriendsArrayList;
    private String myUsername;
    private Button backButton;
    private Button addButton;
    private Intent intent;
    private Intent newestIntent;
    private int checkFriendAmount;
    private boolean changedDataset = false;
    private MyArrayAdapter friendListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        myFriendsArrayList = new ArrayList<String>();
        intent = getIntent();
        myFriendsArrayList = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE);
        myUsername = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);
        checkFriendAmount = myFriendsArrayList.size();
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        addButton = (Button) findViewById(R.id.addFriendButton);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent newIntent = new Intent(FriendActivity.this, AddFriendActivity.class);
                newIntent.putExtra(EXTRA_MESSAGE,myFriendsArrayList);
                newIntent.putExtra(EXTRA_MESSAGE2,myUsername);
                startActivityForResult(newIntent, 3);
            }
        });
        friendListAdapter = new MyArrayAdapter(FriendActivity.this,myFriendsArrayList, myUsername);
        ListView friendListView = (ListView) findViewById(R.id.listFriendList);
        friendListView.setAdapter(friendListAdapter);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            String added = data.getStringExtra(EXTRA_MESSAGE3);
            if(added.startsWith("#")){
                myFriendsArrayList.add("Requested "+added);
                changedDataset = true;
            }
            else {
                myFriendsArrayList.add(added);
            }
            friendListAdapter.notifyDataSetChanged();
        }
    }
}

