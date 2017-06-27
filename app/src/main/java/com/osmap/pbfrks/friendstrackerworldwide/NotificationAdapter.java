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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import static android.app.Activity.RESULT_CANCELED;

/**
 * Created by kevin on 22-Jun-17.
 */

public class NotificationAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;
    private TextView textView;
    private ImageView imageView;
    IStringPass iStringPass;

    int index;
    String friendName;
    public NotificationAdapter(Context context, ArrayList<String> values) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context. getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View friendView = inflater.inflate(R.layout.notification_entry, parent, false);
        textView = (TextView) friendView.findViewById(R.id.markerName);
        Button showButton = (Button) friendView.findViewById(R.id.showButton);
        imageView = (ImageView) friendView.findViewById(R.id.markerSymbol);
        showButton.setTag(new Integer(position));
        showButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                index = (int)v.getTag();
                String result = values.get(index);
                try{
                    iStringPass = (IStringPass) context;
                }catch(ClassCastException e){
                    e.printStackTrace();
                }
                iStringPass.passString(result);

            }
        });
        textView.setText(values.get(position).substring(2));
        if(values.get(position).substring(0,2).equals("mm")){
            //textView.setBackgroundColor(context.getColor(R.color.myMarkerColor));
            imageView.setImageResource(R.drawable.marker2);
        }
        else if(values.get(position).substring(0,2).equals("fp")){
            //textView.setBackgroundColor(context.getColor(R.color.myFriendPositionColor));
            imageView.setImageResource(R.drawable.person_view);

        }
        else{
            //textView.setBackgroundColor(context.getColor(R.color.myFriendMarkerColor));
            imageView.setImageResource(R.drawable.marker3);
        }

        return friendView;
    }
    public interface IStringPass {
        public void passString(String content);
    }
}

