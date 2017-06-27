package com.osmap.pbfrks.friendstrackerworldwide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private  AlertDialog.Builder removeFriendMessageBuilder;
    private AlertDialog removeFriendMessage;
    private AlertDialog removeFriendMessageConfirm;
    private int index;
    private String friendName;
    public MyArrayAdapter(Context context, ArrayList<String> values, String username) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;
        this.myUsername = username;
    }
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
                        attemptDeleteFriend(myUsername, friendName, context);
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
        textView.setText(values.get(position));
        if(values.get(position).contains("#")){
            delButton.setVisibility(View.INVISIBLE);
        }

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
                removeFriendMessageBuilder.setTitle(friend);
                removeFriendMessageBuilder.setMessage("...deleted!");
                removeFriendMessageBuilder.setCancelable(false);
                removeFriendMessageBuilder.setNegativeButton(null, null);
                removeFriendMessageBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                removeFriendMessageConfirm = removeFriendMessageBuilder.create();
                removeFriendMessageConfirm.show();
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
