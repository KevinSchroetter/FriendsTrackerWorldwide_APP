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
 * Created by kevin on 22-Jun-17.
 */

public class MyArrayAdapterRequest extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;
    private TextView textView;
    private ApiCaller myApiCaller;
    private denyFriendTask myDenyTask;
    private confirmFriendTask myConfirmTask;
    private String myUsername;
    private int index;
    private String requestName;
    public MyArrayAdapterRequest(Context context, ArrayList<String> values, String username) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;
        this.myUsername = username;
    }
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
                attemptDenyFriend(myUsername,requestName,context);
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
                attemptConfirmFriend(myUsername,requestName,context);
                notifyDataSetChanged();
            }
        });
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
    public class denyFriendTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final String friend;
        private final Context myContext;
        denyFriendTask(String username, String friend, Context context) {
            myUsername = username;
            this.friend = friend;
            this.myContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/denyFriendRequest";
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
            myDenyTask = null;
            //showProgress(false);

            if (success) {

            } else {
                Toast.makeText(context, "Could not delete friend!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            myDenyTask = null;
            //showProgress(false);
        }
    }
    private void attemptDenyFriend(String username, String friend, Context context) {

        if (myDenyTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
           // do nothing
        } else {

            myDenyTask = new MyArrayAdapterRequest.denyFriendTask(username, friend, context);
            myDenyTask.execute((Void) null);

        }
    }
    public class confirmFriendTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final String friend;
        private final Context myContext;
        confirmFriendTask(String username, String friend, Context context) {
            myUsername = username;
            this.friend = friend;
            this.myContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            JSONObject locationUpdateParams = new JSONObject();
            try {
                locationUpdateParams.put("username", myUsername);
                locationUpdateParams.put("friend", friend);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/confirmFriendRequest";
            JSONObject resultObj = new JSONObject();
            resultObj = myApiCaller.executePut(apiUrl,locationUpdateParams.toString());
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
            //showProgress(false);

            if (success) {

            } else {
                Toast.makeText(context, "Could not confirm friend!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            myConfirmTask = null;
            //showProgress(false);
        }
    }
    private void attemptConfirmFriend(String username, String friend, Context context) {

        if (myConfirmTask != null) {
            return;
        }
        boolean cancel = false;
        if (cancel) {
            // do nothing
        } else {

            myConfirmTask = new MyArrayAdapterRequest.confirmFriendTask(username, friend, context);
            myConfirmTask.execute((Void) null);

        }
    }
}

