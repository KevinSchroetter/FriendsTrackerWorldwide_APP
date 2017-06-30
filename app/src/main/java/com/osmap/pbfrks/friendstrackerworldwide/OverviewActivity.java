package com.osmap.pbfrks.friendstrackerworldwide;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class OverviewActivity extends AppCompatActivity implements OverviewAdapter.IStringPass {
    public static final String EXTRA_MESSAGE3 = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE3";
    protected ArrayList<String> markerInformation;
    Intent intent;
    private ApiCaller apiCaller = new ApiCaller();
    Intent newIntent;
    String test="lol";
    private Button backButton;
    private OverviewAdapter myOverviewAdapter;
    private ArrayList<MarkerCompareHelper> helperArrayList = new ArrayList<MarkerCompareHelper>();
    private ArrayList<String> resultCalc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        intent = getIntent();
        Bundle bundle = intent.getExtras();
        markerInformation = intent.getStringArrayListExtra(MainActivity.EXTRA_MESSAGE3);

        backButton = (Button) findViewById(R.id.backOverviewButton);
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
        myOverviewAdapter = new OverviewAdapter(OverviewActivity.this,markerInformation);
        ListView notificationListView = (ListView) findViewById(R.id.listOverviewList);
        notificationListView.setAdapter(myOverviewAdapter);
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

    @Override
    public void passString(String content) {
        newIntent = new Intent();
        newIntent.putExtra(EXTRA_MESSAGE3, content);
        setResult(RESULT_OK, newIntent);
        finish();
       // Toast.makeText(OverviewActivity.this, content, Toast.LENGTH_SHORT);
    }

}
