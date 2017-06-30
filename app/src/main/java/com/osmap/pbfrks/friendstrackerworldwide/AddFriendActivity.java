package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity used for adding a new friend by entering a name and then sending the information to the FTW API
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */
public class AddFriendActivity extends AppCompatActivity {
    /** String used for sending added friend name to the FriendActivity for updating the displayed listView */
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    /** ApiCaller used for sending an addFriend request to the FTW API */
    ApiCaller friendApiCaller;
    /** UI references */
    Button cancelButton;
    Button addFriendButton;
    TextView addFriendText;
    /** username given from FriendActivity used for adding a new friend */
    private String myUsername;
    /** List of all friends listed in FriendActivity. Used for updating list data in FriendActivity by sending new arrayList to FriendActivity */
    private ArrayList<String> myFriendsArrayList;
    /** Intent referencing the activity that started AddFriendActivity */
    private Intent intent;
    /** Intent used for sending new arrayList to another activity */
    private Intent newIntent;
    /** Task used for adding a friend using the FTW API */
    private AddFriendTask myTask;
    /** Reference for temporary saving the result of the API call */
    private JSONObject resultAddedFriend;

    /** Regex patterns used for validation of input fields */
    private String whitespace_chars =  ""       /* dummy empty string for homogeneity */
            + "\\u0009" // CHARACTER TABULATION
            + "\\u000A" // LINE FEED (LF)
            + "\\u000B" // LINE TABULATION
            + "\\u000C" // FORM FEED (FF)
            + "\\u000D" // CARRIAGE RETURN (CR)
            + "\\u0020" // SPACE
            + "\\u0085" // NEXT LINE (NEL)
            + "\\u00A0" // NO-BREAK SPACE
            + "\\u1680" // OGHAM SPACE MARK
            + "\\u180E" // MONGOLIAN VOWEL SEPARATOR
            + "\\u2000" // EN QUAD
            + "\\u2001" // EM QUAD
            + "\\u2002" // EN SPACE
            + "\\u2003" // EM SPACE
            + "\\u2004" // THREE-PER-EM SPACE
            + "\\u2005" // FOUR-PER-EM SPACE
            + "\\u2006" // SIX-PER-EM SPACE
            + "\\u2007" // FIGURE SPACE
            + "\\u2008" // PUNCTUATION SPACE
            + "\\u2009" // THIN SPACE
            + "\\u200A" // HAIR SPACE
            + "\\u2028" // LINE SEPARATOR
            + "\\u2029" // PARAGRAPH SEPARATOR
            + "\\u202F" // NARROW NO-BREAK SPACE
            + "\\u205F" // MEDIUM MATHEMATICAL SPACE
            + "\\u3000" // IDEOGRAPHIC SPACE
            ;
    private Pattern regexUsername = Pattern.compile("[(<$&+"+whitespace_chars+",':;=?@#|>)]");

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
                Matcher matcher = regexUsername.matcher(added);
                if (matcher.find()){
                    addFriendText.setError("Invalid Name!");
                }
                else{
                    attemptAddFriend(added);
                    addFriendText.setError(null);
                }
            }
        });
    }

    /**
     * Represents an asynchronous task used to send friend requests or adding a friend calling the FTW API
     */
    public class AddFriendTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the varaibles used for friend adding/requesting
        private final String myUsername;
        private final String myFriend;
        AddFriendTask(String username, String friend) {
            myUsername = username;
            myFriend = friend;
        }

        /**
         * Mehtod used for performing a background task that sends information from the Application to the API
         * Performing an addFriend request
         * @param params - username that wants to add a friend, friend that should be added
         * @return - true when the API was reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for API call
            JSONObject locationUpdateParams = new JSONObject();
            // Adding required variables to the JSONObject array
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", myFriend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining API route for calling the addFriend request
            String apiUrl = LoginActivity.URL+"/api/user/addFriend";
            JSONObject resultObj = new JSONObject();
            // Using api caller for PUT request for adding a friend
            resultObj = friendApiCaller.executePut(apiUrl,locationUpdateParams.toString());
            // Handling the results returned by the FTW API
            if(resultObj==null){
                return false;
            }
            else{
                resultAddedFriend = resultObj;
                return true;
            }
        }

        /**
         * Method used for further computation when the background task was successfull
         * The result of the API call will be used for sending the received result to the friend
         * Distinguishes between a received ADDED friend and a message for adding a friend REQUEST
         * @param success - Result of doInBackground
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myTask = null;
            if (success) {
                if (resultAddedFriend.has("message")) {
                    try {
                        if (resultAddedFriend.get("message").toString().equalsIgnoreCase("Friend added")) {
                            newIntent = new Intent();
                            newIntent.putExtra(EXTRA_MESSAGE3, myFriend);
                            setResult(RESULT_OK, newIntent);
                            addFriendText.setError(null);
                            finish();
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

        /**
         * Handling of cancelling the task
         */
        @Override
        protected void onCancelled() {
            myTask = null;
            //showProgress(false);
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

    /**
     * Attempts to add a friend / send a request to a friend using AddFriendTask
     * @param friend - name of the friend that shall be added
     */
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
