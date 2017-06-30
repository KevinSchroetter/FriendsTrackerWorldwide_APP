package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A class used for designing a custom array adapter that is used for displaying marker objects with description and a button to
 * center the map on the chosen marker
 * When "SHOW" button is pressed, the OverviewActivity will close and the MainActivity will center the map on the chosen entry
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */

public class OverviewAdapter extends ArrayAdapter<String> {
    /** Context of the OverviewActivity */
    private final Context context;
    /** Arraylist containing the values resembling marker descriptions */
    private final ArrayList<String> values;
    /** Text of the entry description in the list */
    private TextView textView;
    /** Reference to an image object showing small icons for the kind of marker */
    private ImageView imageView;
    /** Interface used for sending information to the OverviewActivity */
    IStringPass iStringPass;
    /** integer used for addressing the list entry */
    int index;
    /** Name used for displaying the text on the list */
    String friendName;

    /**
     * Constructor containing relevant variables that allocate the variables to the private variables of the class
     * @param context - Context of the Activity where the Adapter belongs to
     * @param values - ArrayList of descriptions of markers to display in the list
     */
    public OverviewAdapter(Context context, ArrayList<String> values) {
        super(context, -1,  values);
        this.context = context;
        this.values = values;

    }

    /**
     * Method for displaying the markers together with a button to center on the marker in the MainActivity
     * Using an IStringPass interface to send the information of the clicked marker name to the OverviewActivity to use
     * it for addressing the right marker element and then sending that information to the MainActivity for addressing the
     * correct marker element related to the clicked marker in the list managed by the OverviewAdapter for centering
     * the map onto that chosen element
     * @param position - Position of the list entry
     * @param convertView - View of entry element
     * @param parent - ViewGroup of the entry elements
     * @return - The Combines view of the text field and button
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context. getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View friendView = inflater.inflate(R.layout.overview_entry, parent, false);
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
        // Distinguishing between personal markers (mm), friend positions (fp) and friend markers
        // Distinguishing is used for adding corresponding icons for better understanding by the user
        textView.setText(values.get(position).substring(2));
        if(values.get(position).substring(0,2).equals("mm")){
            imageView.setImageResource(R.drawable.marker2);
        }
        else if(values.get(position).substring(0,2).equals("fp")){
            imageView.setImageResource(R.drawable.person_view);

        }
        else{
            imageView.setImageResource(R.drawable.marker3);
        }

        return friendView;
    }
    public interface IStringPass {
        public void passString(String content);
    }
}

