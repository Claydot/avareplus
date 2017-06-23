/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ds.avareplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.GpsStatus;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.support.v4.app.Fragment;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.avareplus.gps.GpsInterface;
import com.ds.avareplus.place.Destination;
import com.ds.avareplus.place.Plan;
import com.ds.avareplus.storage.Preferences;
import com.ds.avareplus.storage.StringPreference;
import com.ds.avareplus.utils.DecoratedAlertDialogBuilder;
import com.ds.avareplus.utils.GenericCallback;
import com.ds.avareplus.utils.Helper;
import com.ds.avareplus.webinfc.WebAppPlanInterface;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import static java.security.AccessController.getContext;
import java.util.List;
/**
 * @author zkhan
 * An activity that deals with flight plans - loading, creating, deleting and activating
 */
public class PlanActivity extends FragmentActivity implements OnStartDragListener {

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onBackPressed()
     */

    private int currentWaypoint;
    private StorageService mService;
    private Preferences mPref;


    private Button mBSearch;
    private RecyclerView mRecyclerView;
    private ListView mListView;
    private EditText mSearchInput;




    private ItemTouchHelper mItemTouchHelper;
    private ItemAdapter mAdapter;
    private Plan mPlan;
    private LinkedHashMap<String, String> mSavedPlans;
    private Context mContext;
    private SearchTask mSearchTask;


    ArrayList<String> mSearchList = new ArrayList<>();
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter mSearchAdapter;


    @Override
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
        mContext = this;

        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);





        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.planplus, null);
        setContentView(view);

        mSearchInput = (EditText) view.findViewById(R.id.editText);




        mBSearch = (Button) view.findViewById(R.id.Search_Button);
        mBSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               mListView.setVisibility(View.VISIBLE);
               String searchVal = mSearchInput.getText().toString();
                mSearchList.clear();
               search(searchVal);
            }



        });


        //new code
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ItemAdapter(this, null, this);
        ItemTouchHelper.Callback callback =
                new EditItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);


        //end code


        mListView = (ListView) view.findViewById(R.id.search_plan);
        mSearchAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mSearchList);
        mListView.setAdapter(mSearchAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                        Toast.LENGTH_SHORT).show();


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

    public void up() {


    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }



    public void search(String value) {

    	/*
         * If text is 0 length or too long, then do not search, show last list
         */
        if(0 == value.length()) {
            return;
        }

        if(null != mSearchTask) {
            if (!mSearchTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
                /*
                 * Cancel the last query
                 */
                mSearchTask.cancel(true);
            }
        }

        mSearchTask = new SearchTask();
        mSearchTask.execute(value);
    }





































    public List<Destination> planToList(Plan plan) {

        List<Destination> list = new ArrayList<>();
        int i = 0;

        while(plan.getDestination(i) != null) {
            list.add(plan.getDestination(i));
        }
        return list;
    }


    public void updatePlan() {

        mAdapter.updateList(planToList(mPlan));
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
//        mSavedPlans = Plan.getAllPlans(mService, mPref.getPlans());
        mPlan = mService.getPlan();
        Log.d("Connect", "mService Connected");
        updatePlan();
    }




    private class SearchTask extends AsyncTask<Object, Void, Boolean> {

        String selection[] = null;

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Boolean doInBackground(Object... vals) {

            Thread.currentThread().setName("Search");

            String srch = ((String)vals[0]).toUpperCase(Locale.US);
            if(null == mService) {
                return false;
            }

            /*
             * This is a geo coordinate?
             */
            if(Helper.isGPSCoordinate(srch)) {

                selection = new String[1];
                selection[0] = (new StringPreference(Destination.GPS, Destination.GPS, Destination.GPS, srch)).getHashedName();
                return true;
            }

            LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();

            mService.getDBResource().search(srch, params, true);
            mService.getUDWMgr().search(srch, params);			// From user defined points of interest
            if(params.size() > 0) {
                selection = new String[params.size()];
                int iterator = 0;
                for(String key : params.keySet()){
                    selection[iterator] = StringPreference.getHashedName(params.get(key), key);
                    iterator++;
                }
            }
            return true;
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(Boolean result) {
            /*
             * Set new search adapter
             */

            if(null == selection || false == result) {
                return;
            }

            /*
             * Add each to the plan search
             */
            for (int num = 0; num < selection.length; num++) {
                String val = selection[num];

                mSearchList.add(searchParser(val));
                mSearchAdapter.notifyDataSetChanged();
            }
        }
    }

    private String searchParser(String val) {

        return val;
    }






}
