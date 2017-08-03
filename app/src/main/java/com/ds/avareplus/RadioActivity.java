package com.ds.avareplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.avareplus.place.Destination;
import com.ds.avareplus.place.Plan;
import com.ds.avareplus.storage.DataSource;
import com.ds.avareplus.storage.Preferences;
import com.ds.avareplus.storage.StringPreference;
import com.ds.avareplus.utils.Helper;
import com.sromku.polygon.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clayd on 7/23/2017.
 */

public class RadioActivity extends Activity {


    private Button toggle;
    private DataSource mDataSource;
    private StorageService mService;
    int displayState;   //display state 0 shows plan frequencies
                        //display state 1 shows destination frequencies
    private LinearLayout mLinearLayout;


    public void onBackPressed() {
        ((MainActivity) this.getParent()).showMapTab();
    }


    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.radio, null);
        setContentView(view);

        mLinearLayout = (LinearLayout) view.findViewById(R.id.radio_layout);

        mDataSource = new DataSource(getApplicationContext());



        toggle = (Button) view.findViewById(R.id.toggle_radio);
        toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (displayState == 0) {
                    upDatePlan();
                    toggle.setText("Plan Frequencies");
                    displayState = 1; //corresponds to plan frequencies
                } else {
                    //insert for destination freq if null then get last element in plan

                    destinationFreq();
                    toggle.setText("Destination Frequencies");
                    displayState = 0;
                }

                ResearchFile.append("Toggle");


            }



        });



    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        Helper.setOrientationAndOn(this);
        Intent intent = new Intent(this, StorageService.class);
        getApplicationContext().bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
        //reset all frequencies
        refresh();


    }


    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    public void destinationFreq() {
        if (mService == null) {
            return;
        }
        mLinearLayout.removeAllViewsInLayout();
        Destination d = mService.getDestination();
        if (d !=null) {
            mLinearLayout.addView(createNewTextView(d.getID()));
            mLinearLayout.addView(createNewListView(
                    new ArrayList<String>(mDataSource.findFrequencies(d.getID()))));
        }
    }



    public void refresh() {
        if(displayState == 1) {
            upDatePlan();
        } else {
            destinationFreq();
        }
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        /*
         * (non-Javadoc)
         *
         * @see
         * android.content.ServiceConnection#onServiceConnected(android.content
         * .ComponentName, android.os.IBinder)
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            /*
             * We've bound to LocalService, cast the IBinder and get
             * LocalService instance
             */
            StorageService.LocalBinder binder = (StorageService.LocalBinder) service;
            connect(binder.getService());

            /*
             * When both service and page loaded then prceed.
             * The plan will be loaded either from here or from page load end event
             */
            //mTimer = new Timer();
            // TimerTask sim = new UpdateTask();
            //mTimer.scheduleAtFixedRate(sim, 0, 1000);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * android.content.ServiceConnection#onServiceDisconnected(android.content
         * .ComponentName)
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    private void connect(StorageService s) {
        mService = s;
    }

    private void showPlan() {
        //enter data here to show plan

    }

    public void upDatePlan() {
        //inflate plan data TODO
        //completely clear out screen before hand
        mLinearLayout.removeAllViewsInLayout();
        if (mService == null) return;
        Plan plan = mService.getPlan();
        for(int i = 0; i < plan.getDestinationNumber(); i++) {
            Destination d = plan.getDestination(i);
            if (d !=null) {
                mLinearLayout.addView(createNewTextView(d.getID()));
                mLinearLayout.addView(createNewListView(
                        new ArrayList<String>(mDataSource.findFrequencies(d.getID()))));
            }

        }

    }

    private TextView createNewTextView(String text) {
        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setLayoutParams(lparams);
        textView.setText(text);
        return textView;
    }
    private ListView createNewListView(ArrayList<String> freq) {

        final ViewGroup.LayoutParams lparams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 200);
        final ListView frequencyList = new ListView(this);
        frequencyList.setLayoutParams(lparams);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                freq);
        frequencyList.setAdapter(adapter);
        frequencyList.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        return frequencyList;

    }
}
