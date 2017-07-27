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

import android.annotation.SuppressLint;
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
import android.text.InputType;
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

import com.ds.avareplus.externalFlightPlan.ExternalFlightPlan;
import com.ds.avareplus.gps.GpsInterface;
import com.ds.avareplus.place.Destination;
import com.ds.avareplus.place.DestinationFactory;
import com.ds.avareplus.place.Plan;
import com.ds.avareplus.storage.Preferences;
import com.ds.avareplus.storage.StringPreference;
import com.ds.avareplus.utils.DecoratedAlertDialogBuilder;
import com.ds.avareplus.utils.GenericCallback;
import com.ds.avareplus.utils.Helper;
import com.ds.avareplus.webinfc.WebAppPlanInterface;

import java.util.ArrayList;
import java.util.Iterator;
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
    private String currentPlan;



    private Button mBSearch;
    private RecyclerView mRecyclerView;
    private ListView mListView;
    private ListView mLoadTable;
    private EditText mSearchInput;
    private Button mActivateButton;
    private Button mLoadButton;
    private Button mSaveButton;
    private Button mDeleteButton;
    private Button mUndoButton;
    private Button mRedoButton;

    private TextView mTotalEte;
    private TextView mTotalFuel;
    private TextView mTotalDistance;




    private ItemTouchHelper mItemTouchHelper;
    private ItemAdapter mAdapter;
    private Plan mPlan;
    private LinkedHashMap<String, String> mSavedPlans;
    private Context mContext;
    private SearchTask mSearchTask;
    ArrayList<String> mPlanList = new ArrayList<>();


    ArrayList<String> mSearchList = new ArrayList<>();
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter mSearchAdapter;
    ArrayAdapter mLoadAdapter;


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


        mPref = new Preferences(this.getApplicationContext());

        getPlanNames(10);
        mContext = this;

        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        new Undo();


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


        mLoadButton = (Button) view.findViewById(R.id.load_button);
        mLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLoadTable.getVisibility() == View.VISIBLE) {
                    mLoadTable.setVisibility(View.GONE);
                } else {
                    mLoadTable.setVisibility(View.VISIBLE);
                }
                getPlanNames(10);
                mLoadAdapter.notifyDataSetChanged();

            }
        });
        mSaveButton = (Button) view.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog();
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

        /*
        mSearchAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mSearchList);

        */
        mSearchAdapter = new SearchListAdapter(this, mSearchList);

        mListView.setAdapter(mSearchAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                String val = mSearchList.get((int) id);

                Toast.makeText(getApplicationContext(), val,
                        Toast.LENGTH_SHORT).show();

                String name = StringPreference.parseHashedNameId(val);
                String type = StringPreference.parseHashedNameDestType(val);
                String dbtype = StringPreference.parseHashedNameDbType(val);
                addToPlan(name, type, dbtype);
                mListView.setVisibility(View.GONE);
            }
        });

        mLoadTable = (ListView) view.findViewById(R.id.load_plan);
        mLoadAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                mPlanList);
        mLoadTable.setAdapter(mLoadAdapter);
        mLoadTable.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String val = ((TextView) view).getText().toString();
                currentPlan = val;
                loadPlan(val);
                mLoadTable.setVisibility(View.GONE);
            }
        });


        mDeleteButton = (Button) view.findViewById(R.id.delete_plan);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(currentPlan != null) {
                    deletePlan(currentPlan);
                }
            }


        });

        mActivateButton = (Button) view.findViewById(R.id.activate_button);
        mActivateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Plan plan = mService.getPlan();

                if (plan.isActive()) {
                    plan.makeInactive();
                    mService.setDestination(null);
                    mActivateButton.setText("Deactivate");
                } else {

                    plan.clear();
                    List<Destination> dList = mAdapter.getDestinationList();
                    for(Destination d: dList) {
                        plan.appendDestination(d);
                    }

                    plan.makeActive(mService.getGpsParams());
                    if (plan.getDestination(plan.findNextNotPassed()) != null) {
                        mService.setDestinationPlanNoChange(plan.getDestination(plan.findNextNotPassed()));
                        mActivateButton.setText(getString(R.string.Active));
                    }
                }
            }


        });

        mRedoButton = (Button) view.findViewById(R.id.redo_plan);
        mRedoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                redo();
            }
        });


        mUndoButton = (Button) view.findViewById(R.id.undo_plan);
        mUndoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                undo();
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
            i++;

        }
        return list;
    }


    public void updatePlan() {
        if (mPlan != null) {
            mAdapter.updateList(planToList(mPlan));
        } else {

            mAdapter.updateList( new ArrayList<Destination>());
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

        mSavedPlans = Plan.getAllPlans(mService, mPref.getPlans());
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
        //need to make custom adapter for Search List
    }








    private void addToPlan(String id, String type, String subtype)  {
        if (mPlan == null) {
            mPlan = new Plan(this, mService);
        }
        //add destination
    	/*
    	 * Add from JS search query
    	 */
            mPlan.clear();
            List<Destination> dList = mAdapter.getDestinationList();
            for(Destination d: dList) {
                mPlan.appendDestination(d);
            }

        Destination d = DestinationFactory.build(mService, id, type);
        d.find(subtype);

        mPlan.appendDestination(d);
        updatePlan();
        Undo.change(this, mService, planToList(mPlan));
    }


    //additional Functions to add
    //TODO also add delete and clear
    //TODO also add way to reorder list

    private void activatePlan() {
        Plan plan = mService.getPlan();
        if(plan.isActive()) {
            plan.makeInactive();
            mService.setDestination(null);
            mActivateButton.setText("Activate");
        }
        else {
            plan.makeActive(mService.getGpsParams());
            if(plan.getDestination(plan.findNextNotPassed()) != null) {
                mService.setDestinationPlanNoChange(plan.getDestination(plan.findNextNotPassed()));
            }
            mActivateButton.setText("Deactivate");
        }
    }

    private void savePlan(String name) {

        Plan plan = mService.getPlan();
        if(plan.getDestinationNumber() < 2) {
            // Anything less than 2 is not a plan
            return;
        }

        plan.setName(name);
        String format = plan.putPlanToStorageFormat();
        mSavedPlans.put(name, format);
        mPref.putPlans(Plan.putAllPlans(mService, mSavedPlans));
        //setFilteredSize();

        //newSavePlan();
    }

    public void loadPlan(String name) {

        // If we have an active plan, we need to turn it off now since we are
        // loading a new one.

        //remove waypoints from list TODO


        Plan plan = mService.getPlan();
        if(null != plan) {
            plan.makeInactive();

            // If it is an external plan, tell it to unload
            ExternalFlightPlan efp = mService.getExternalPlanMgr().get(plan.getName());
            if(null != efp) {
                efp.unload(mService);
            }
        }

        // Clear out any destination that may have been set.
        mService.setDestination(null);

        // If this is defined as an external flight plan, then tell it we
        // are loading into memory.
        ExternalFlightPlan efp = mService.getExternalPlanMgr().get(name);
        if(null != efp) {
            efp.load(mService);
        }

        mService.newPlanFromStorage(mSavedPlans.get(name), false);
        mService.getPlan().setName(name);
        newPlan();
    }

    //use to display Plan names

    public void addPlanNames() {

    }

    public void getPlanNames(int dispQty) {
        mPlanList.clear();

        if (mSavedPlans == null) {
            return;
        }
        //Init some local variables we will be using
        int planIdx = 0;	// Used to count where we are in the plan list
        ArrayList<String> planNames = new ArrayList<String>();
        Iterator<String> it = mSavedPlans.keySet().iterator();

        // As long as we have items in the list and need items for display
        while(it.hasNext() && dispQty > 0) {

            // Get the plan name from the iterator
            String planName = it.next();

            // Does this name qualify for display ?
            if(true) {

                // Is our walking index passed our current display index ?
                if(++planIdx > 0) {

                    // Add the name to the collection for display
                    mPlanList.add(planName);

                    // Adjust our displayed item counter
                    dispQty--;
                }
            }
        }

        // Our collection is complete
    }

    public void refreshPlanList() {
        mService.getExternalPlanMgr().forceReload();
        mSavedPlans = Plan.getAllPlans(mService, mPref.getPlans());
    }
    /**
     * New plan when the plan changes.
     */


    public void newPlan() {
        mPlan.clear();
        updatePlan();
        Plan plan = mService.getPlan();
        int num = plan.getDestinationNumber();
        for(int dest = 0; dest < num; dest++) {
            Destination d = plan.getDestination(dest);
            addToPlan(d.getID(), d.getType(), d.getFacilityName());
        }
    }

    public void deletePlan(String name) {
        if (mPlan != null) {
            mPlan.clear();
        }
            updatePlan();
        mSavedPlans.remove(name);
        mPref.putPlans(Plan.putAllPlans(mService, mSavedPlans));

    }

    public void newDestination(Destination d) {
        mService.setDestination(d);
    }

    public void undo() {
        mPlan = Undo.undo();
        updatePlan();

    }

    public void redo(){
        mPlan = Undo.redo();
        updatePlan();
    }

    public void delete() {
        mPlan.clear();
        List<Destination> dList = mAdapter.getDestinationList();
        for(Destination d: dList) {
            mPlan.appendDestination(d);
        }
        Undo.change(this, mService, planToList(mPlan));
        updatePlan();


    }
    public void save(String name) {
        savePlan(name);
        getPlanNames(10);
        mLoadAdapter.notifyDataSetChanged();


    }
    public void saveDialog() {
        InputDialog.planSaveDialog(this);
    }

}
