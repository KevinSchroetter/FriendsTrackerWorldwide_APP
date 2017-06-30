package com.osmap.pbfrks.friendstrackerworldwide;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
 * A class used for designing a custom array adapter that is used together with the
 * FriendActivity. It uses the friendlist_entry.xml layout to display a listentry
 * that displays a textfield for displaying text together with a button enabling a delete
 * function to delete the friends from within the list using the FTW API to execute the task of deleting
 * When a friend is deleted, the entry will be deleted out of the list as well and the current view gets an update
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */

public class FriendsAdapter extends ArrayAdapter<String> {
    /** Used for making RESTFUL API calls */
    private ApiCaller myApiCaller;
    /** the context of the FriendActivity intent */
    private final Context context;
    /** Arraylist containing the list values resembling friend names given from the FriendActivity */
    private final ArrayList<String> values;
    /** Username given from the FriendActivity to manage API calls */
    private String myUsername;
    /** Reference to the textfield where friend names are displayed */
    private TextView textView;
    /** A Task for deleting a friend from the friend list using the FTW API */
    private deleteFriendTask myFriendTask;
    /** Builder for displaying a dialog to confirm or cancel deletion */
    private  AlertDialog.Builder removeFriendMessageBuilder;
    /** Object for the deletion message */
    private AlertDialog removeFriendMessage;
    /** Object for confirming the confirmation message after deleting as a feedback for the user */
    private AlertDialog removeFriendMessageConfirm;
    /** Integer used for addressing the right Button in the list entry corresponding to the friend name */
    private int index;
    /** Name of the friend displayed */
    private String friendName;

    /**
     * Constructor containing relevant variables that allocates the variables to the private variables of the class
     * @param context - Context of the Activity where the Adapter belongs to
     * @param values - ArrayList of names that shall be displayed
     * @param username - Name of the user that uses the applicaton
     */
    public FriendsAdapter(Context context, ArrayList<String> values, String username) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;
        this.myUsername = username;
    }

    /**
     * Method for displaying the friendnames and the delete buttons in a list of the activity
     * @param position - Position of the list entry
     * @param convertView - View of entry element
     * @param parent - ViewGroup of the entry elements
     * @return - The Combines view of the text field and button
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View friendView = inflater.inflate(R.layout.friendlist_entry, parent, false);
        myApiCaller = new ApiCaller();
        textView = (TextView) friendView.findViewById(R.id.friendName);
        Button delButton = (Button) friendView.findViewById(R.id.deleteButton);
        delButton.setTag(new Integer(position));
        delButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                index = (int)v.getTag();
                friendName = values.get(index).toString();
                removeFriendMessageBuilder = new AlertDialog.Builder(context);
                removeFriendMessageBuilder.setTitle("Delete "+friendName+"?");
                removeFriendMessageBuilder.setMessage("Are you sure you want to remove "+friendName+" from your Friendlist?\n\nThis action cannot be undone!");
                removeFriendMessageBuilder.setCancelable(false);
                removeFriendMessageBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        values.remove(index);
                        // Attempting to delete a friend
                        attemptDeleteFriend(myUsername, friendName, context);
                        // Notifying the list that something changed so it can update the display of the friend entrys
                        notifyDataSetChanged();
                    }
                });
                removeFriendMessageBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do Nothing
                    }
                });
                removeFriendMessage = removeFriendMessageBuilder.create();
                removeFriendMessage.show();
            }
        });
        // Hiding the deletion button in case the list was loaded again after sending a friend request
        // In that case a friend is not yet added, but are request was sent. Basically feedback for the user
        textView.setText(values.get(position));
        if(values.get(position).contains("#")){
            delButton.setVisibility(View.INVISIBLE);
        }
        return friendView;
    }

    /**
     * Represents an asynchronous task used to delete a friend calling the FTW API
     */
    public class deleteFriendTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the variables used for the deletion
        private final String myUsername;
        private final String friend;
        private final Context myContext;

        deleteFriendTask(String username, String friend, Context context) {
            myUsername = username;
            this.friend = friend;
            this.myContext = context;
        }

        /**
         * Method used for performing a background task that sends information from the Application to the API for deleting a friend
         * Performing a friend deletion
         * @param params - a username that sends the delete request and a friend that shall be deleted
         * @return - true the API was reached
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
            // Defining the API route called for deleting a friend
            String apiUrl = LoginActivity.URL+"/api/user/deleteFriend";
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
         * Displaying a message after delete is complete
         * @param success - return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myFriendTask = null;
            if (success) {
                removeFriendMessageBuilder.setTitle(friend);
                removeFriendMessageBuilder.setMessage("...deleted!");
                removeFriendMessageBuilder.setCancelable(false);
                removeFriendMessageBuilder.setNegativeButton(null, null);
                removeFriendMessageBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // do nothing for only confirming the action
                            }
                        });
                removeFriendMessageConfirm = removeFriendMessageBuilder.create();
                removeFriendMessageConfirm.show();
            } else {
                Toast.makeText(context, "Could not delete friend!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Handling of cancelling the task
         */
        @Override
        protected void onCancelled() {
            myFriendTask = null;
            //showProgress(false);
        }
    }

    /**
     * Attempts to delete a friend using the deleteFriendTask
     * @param username - uername of the user that wants to delete a friend
     * @param friend - name of the friend that shall be deleted
     * @param context - context of the activity
     */
    private void attemptDeleteFriend(String username, String friend, Context context) {
        if (myFriendTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
           // do nothing
        } else {
            myFriendTask = new FriendsAdapter.deleteFriendTask(username, friend, context);
            myFriendTask.execute((Void) null);
        }
    }
}
