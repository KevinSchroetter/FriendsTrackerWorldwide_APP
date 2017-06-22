package com.osmap.pbfrks.friendstrackerworldwide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by kevin on 22-Jun-17.
 */

public class MyArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;
    private TextView textView;
    private ApiCaller myApiCaller;
    private deleteFriendTask myFriendTask;
    private String myUsername;
    public MyArrayAdapter(Context context, ArrayList<String> values, String username) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;
        this.myUsername = username;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context. getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View friendView = inflater.inflate(R.layout.friendlist_entry, parent, false);
        myApiCaller = new ApiCaller();
        textView = (TextView) friendView.findViewById(R.id.friendName);
        Button delButton = (Button) friendView.findViewById(R.id.deleteButton);
        delButton.setTag(new Integer(position));
        delButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int index = (int)v.getTag();
                String friendName = values.get(index).toString();
                values.remove(index);
                attemptDeleteFriend(myUsername, friendName, context);
                notifyDataSetChanged();
            }
        });
        textView.setText(values.get(position));
        return friendView;
    }
    public class deleteFriendTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final String friend;
        private final Context myContext;
        deleteFriendTask(String username, String friend, Context context) {
            myUsername = username;
            this.friend = friend;
            this.myContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/deleteFriend";
            JSONObject resultObj = new JSONObject();
            resultObj = myApiCaller.executeDelete(apiUrl,locationUpdateParams.toString());


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
            myFriendTask = null;
            //showProgress(false);

            if (success) {
                Toast.makeText(context, "Friend "+friend+" deleted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Could not delete friend!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            myFriendTask = null;
            //showProgress(false);
        }
    }
    private void attemptDeleteFriend(String username, String friend, Context context) {

        if (myFriendTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
           // do nothing
        } else {

            myFriendTask = new MyArrayAdapter.deleteFriendTask(username, friend, context);
            myFriendTask.execute((Void) null);

        }
    }
}
