package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A calss used for designing a custom array adapter that is used together with the RequestActivity for displaying requests in a list
 * It uses the friendlist_entry.xml as layout for list entries to show an open friend request with two buttons for either
 * declining or confirming an open request. Also sent requests from the user are displayed only in a textfield.
 * When a open request is declined or confirmed, the entry will be deleted out of the list
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */

public class RequestAdapter extends ArrayAdapter<String> {
    /** Used for making RESTFUL API calls */
    private ApiCaller myApiCaller;
    /** the context of the FriendActivity intent */
    private final Context context;
    /** Arraylist containing the list values resembling requests given from the RequestActivity */
    private final ArrayList<String> values;
    /** Reference to the textfield where requests are displayed */
    private TextView textView;
    /** A Task for deleting a friendrequest using the FTW API */
    private denyFriendTask myDenyTask;
    /** A Task used for confirming a friendrequest and adding the friend to the friend list using the FTW API */
    private confirmFriendTask myConfirmTask;
    /** The username givne by the RequestActivity */
    private String myUsername;
    /** Index used foor addressing the correct list entry */
    private int index;
    /** Name of the request displayed */
    private String requestName;

    /**
     * Constructor containing relevant variables that allocates the variables to the private variables of the class
     * @param context - Context of the Activity where the Adapter belongs to
     * @param values - ArrayList of names that shall be displayed
     * @param username - Name of the user that uses the applicaton
     */
    public RequestAdapter(Context context, ArrayList<String> values, String username) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;
        this.myUsername = username;
    }

    /**
     * Method for displaying the requests and the deny/confirm buttons in a list of the activity
     * @param position - Position of the list entry
     * @param convertView - View of entry element
     * @param parent - ViewGroup of the entry elements
     * @return - The Combines view of the text field and button
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View requestView = inflater.inflate(R.layout.requestlist_entry, parent, false);
        myApiCaller = new ApiCaller();
        textView = (TextView) requestView.findViewById(R.id.requestName);
        Button declineButton = (Button) requestView.findViewById(R.id.declineButton);
        declineButton.setTag(new Integer(position));
        declineButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                index = (int)v.getTag();
                String chosen = values.get(index).toString();
                String devided[] = chosen.split("#");
                requestName = devided[1];
                values.remove(index);
                // Attempting to deny a request
                attemptDenyFriend(myUsername,requestName,context);
                // Notifying the list that something changed for update
                notifyDataSetChanged();
            }
        });
        Button confirmButton = (Button) requestView.findViewById(R.id.confirmButton);
        confirmButton.setTag(new Integer(position));
        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                index = (int)v.getTag();
                String chosen = values.get(index).toString();
                String devided[] = chosen.split("#");
                requestName = devided[1];
                values.remove(index);
                // Attempting to accept a request
                attemptConfirmFriend(myUsername,requestName,context);
                // Notifying the list that something changed for update
                notifyDataSetChanged();
            }
        });
        // Deviding open requests from sent requests for different display
        String splitted[] = null;
        splitted = values.get(position).split("#");
        if(splitted[0].equals("Y")){
            textView.setText("You sent a request to: "+splitted[1]+"\n");
            declineButton.setVisibility(View.INVISIBLE);
            confirmButton.setVisibility(View.INVISIBLE);
        }
        else{
            textView.setText("Request from:\n"+splitted[1]);
        }
        return requestView;
    }

    /**
     * Represents an asynchronous task used to deny a friendrequest calling the FTW API
     */
    public class denyFriendTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the variables used for the deletion
        private final String myUsername;
        private final String friend;
        private final Context myContext;
        denyFriendTask(String username, String friend, Context context) {
            myUsername = username;
            this.friend = friend;
            this.myContext = context;
        }

        /**
         * Method used for performing a background task that sends information from the Application to the API for deny a friend request
         * Performing denying a friend request
         * @param params - a username that denies the request and a friend that shall be denied
         * @return - true when the API was reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the API uses JSONObjects for handling requests
            JSONObject locationUpdateParams = new JSONObject();
            // Adding required variables to the JSONObecjt array
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for denying a friend request
            String apiUrl = LoginActivity.URL+"/api/user/denyFriendRequest";
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a DELETE request to the apiURL destination
            resultObj = myApiCaller.executeDelete(apiUrl,locationUpdateParams.toString());
            // Handling the results returned by the FTW API - either "message" or "err"
            try {
                if(resultObj.get("message")!=null){
                    return true;
                }
                else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         * Method called after the background task for further execution of the application
         * @param success - return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myDenyTask = null;
            if (success) {
                // Do nothing since the list will update anyways
            } else {
                Toast.makeText(context, "Could not delete friend!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Handling of cancelling the task
         */
        @Override
        protected void onCancelled() {
            myDenyTask = null;
        }
    }

    /**
     * Represents an asynchronous task used to accept a friendrequest calling the FTW API
     */
    public class confirmFriendTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the variables used for the deletion
        private final String myUsername;
        private final String friend;
        private final Context myContext;
        confirmFriendTask(String username, String friend, Context context) {
            myUsername = username;
            this.friend = friend;
            this.myContext = context;
        }

        /**
         * Method used for performing a background task that sends information from the Application to the API for deny a friend request
         * Performing denying a friend request
         * @param params - a username that accepts the request and a friend that shall be accepted
         * @return - true when the API was reached
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the API uses JSONObjects for handling requests
            JSONObject locationUpdateParams = new JSONObject();
            // Adding required variables to the JSONObecjt array
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for accepting a friend request
            String apiUrl = LoginActivity.URL+"/api/user/confirmFriendRequest";
            // Using an ApiCaller for executing a DELETE request to the apiURL destination
            JSONObject resultObj = new JSONObject();
            resultObj = myApiCaller.executePut(apiUrl,locationUpdateParams.toString());
            // Handling the results returned by the FTW API - either "message" or "err"
            try {
                if(resultObj.get("message")!=null){
                    return true;
                }
                else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myConfirmTask = null;
            if (success) {
                // Do nothing since the list will be updated anyways
            } else {
                Toast.makeText(context, "Could not confirm friend!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Handling of cancelling the task
         */
        @Override
        protected void onCancelled() {
            myConfirmTask = null;
        }
    }

    /**
     * Attempts to deny a friend request using the denyFriendTask
     * @param username - the username of the user that wants to deny a request
     * @param friend - the name of the friend whos request is denied
     * @param context - context of the activity
     */
    private void attemptDenyFriend(String username, String friend, Context context) {
        if (myDenyTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
           // do nothing
        } else {
            myDenyTask = new RequestAdapter.denyFriendTask(username, friend, context);
            myDenyTask.execute((Void) null);
        }
    }

    /**
     * Attempts to confirm a friend request using the confirmFriendTask
     * @param username - username of the user that wants to accept a request
     * @param friend - name of the friend whos request is accepted
     * @param context - context of the activity
     */
    private void attemptConfirmFriend(String username, String friend, Context context) {
        if (myConfirmTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            // do nothing
        } else {
            myConfirmTask = new RequestAdapter.confirmFriendTask(username, friend, context);
            myConfirmTask.execute((Void) null);
        }
    }
}

