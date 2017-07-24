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
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clayd on 7/23/2017.
 */

public class RadioActivity extends Activity {


    private Button toggle;
    private TextView nearestText;
    private ListView nearestList;
    private ArrayAdapter mNearestAdapt;
    ArrayList<String> mNearestList = new ArrayList<>();
    private DataSource mDataSource;
    private StorageService mService;



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

        mDataSource = new DataSource(getApplicationContext());



        toggle = (Button) view.findViewById(R.id.toggle_radio);
        toggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setNearest();
            }



        });



        nearestText = (TextView) view.findViewById(R.id.nearest_radio);


        nearestList = (ListView) view.findViewById(R.id.nearest_radio_list);
        mNearestAdapt = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mNearestList);
        nearestList.setAdapter(mNearestAdapt);



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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setNearest() {
        Destination d = mService.getDestination();


        if (d !=null) {
            mNearestList.clear();
            nearestText.setText(d.getID());
            mNearestList.addAll(new ArrayList<String>(mDataSource.findFrequencies(d.getID())));
            mNearestAdapt.notifyDataSetChanged();
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
        Log.d("Connect", "mService Connected");
    }














}
