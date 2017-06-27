package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFriendActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    ApiCaller friendApiCaller;
    Button cancelButton;
    Button addFriendButton;
    TextView addFriendText;
    private String myUsername;
    private Intent intent;
    private Intent newIntent;
    private AddFriendTask myTask;
    private JSONObject resultAddedFriend;
    protected ArrayList<String> myFriendsArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        friendApiCaller = new ApiCaller();
        myFriendsArrayList = new ArrayList<String>();
        intent = getIntent();
        addFriendText = (TextView) findViewById(R.id.addFriendTextField);
        myFriendsArrayList = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE);
        myUsername = intent.getStringExtra(MainActivity.EXTRA_MESSAGE2);
        cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
               finish();
            }
        });
        addFriendButton = (Button) findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String added = addFriendText.getText().toString();
                attemptAddFriend(added);
            }
        });
    }
    public class AddFriendTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final String myFriend;

        AddFriendTask(String username, String friend) {
            myUsername = username;
            myFriend = friend;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", myFriend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/addFriend";
            JSONObject resultObj = new JSONObject();
            resultObj = friendApiCaller.executePut(apiUrl,locationUpdateParams.toString());
                if(resultObj==null){
                    return false;
                }
                else{
                    resultAddedFriend = resultObj;
                    return true;
                }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myTask = null;
            //showProgress(false);

            if (success) {
                if (resultAddedFriend.has("message")) {
                    try {
                        if (resultAddedFriend.get("message").toString().equalsIgnoreCase("Friend added")) {
                            newIntent = new Intent();
                            newIntent.putExtra(EXTRA_MESSAGE3, myFriend);
                            setResult(RESULT_OK, newIntent);
                            addFriendText.setError(null);
                            finish();
                            //Toast.makeText(AddFriendActivity.this,"Friend already in Friendlist!", Toast.LENGTH_SHORT);

                        }
                        else if(resultAddedFriend.get("message").toString().equalsIgnoreCase("Already sent friendrequest to this user!")){
                            addFriendText.setError("Already sent request!");
                        }
                        else if(resultAddedFriend.get("message").toString().equalsIgnoreCase("added Friendrequest")){
                            newIntent = new Intent();
                            newIntent.putExtra(EXTRA_MESSAGE3, "#"+myFriend);
                            setResult(RESULT_OK, newIntent);
                            addFriendText.setError(null);
                            finish();
                        }
                        else if(resultAddedFriend.get("message").toString().equalsIgnoreCase("Friend already exists")){
                            addFriendText.setError("You already have that friend!");
                        }
                        else {
                            addFriendText.setError("Friend not in Database!");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    addFriendText.setError("Friend does not exist!");
                    //Toast.makeText(AddFriendActivity.this, "Could not add Friend! Friend not in database!", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(AddFriendActivity.this, "Database error!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            myTask = null;
            //showProgress(false);
        }
    }
    private void attemptAddFriend(String friend) {
        if (myTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            Toast.makeText(AddFriendActivity.this, "An error occured!", Toast.LENGTH_SHORT).show();
        } else {

            myTask = new AddFriendActivity.AddFriendTask(myUsername, friend);
            myTask.execute((Void) null);

        }
    }
}
