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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.support.v4.app.Fragment;

import com.ds.avareplus.gps.GpsInterface;
import com.ds.avareplus.utils.DecoratedAlertDialogBuilder;
import com.ds.avareplus.utils.GenericCallback;
import com.ds.avareplus.utils.Helper;
import com.ds.avareplus.webinfc.WebAppPlanInterface;

import java.util.ArrayList;
import java.util.List;
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

    private Button mBSearch;
    private View frag;
    private RecyclerView mRecyclerView;
    private ItemTouchHelper mItemTouchHelper;


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

        Helper.setTheme(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.planplus, null);
        setContentView(view);

        Button mBSearch = (Button) view.findViewById(R.id.Search_Button);
        mBSearch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                frag.setVisibility(View.VISIBLE);
            }



        });


        //new code
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        List<ItemModel> list = new ArrayList<>();
        list.add(new ItemModel("ATL", "AP", "5", "12", "132", "13","23","75"));
        list.add(new ItemModel("JFK", "AP", "1000", "121", "10", "124","432","100"));
        list.add(new ItemModel("LGA", "AP", "12", "142", "10", "13","263","75"));
        list.add(new ItemModel("SAT", "AP", "24", "122", "1132", "13","253","75"));
        list.add(new ItemModel("OKC", "AP", "53", "124", "134", "12","43","75"));
        list.add(new ItemModel("SAN", "AP", "15", "112", "15", "15","13","75"));
        list.add(new ItemModel("LAX", "AP", "98", "52", "122", "1453","23","75"));
        list.add(new ItemModel("DEN", "AP", "34", "42", "2", "113","23","75"));
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ItemAdapter mAdapter = new ItemAdapter(this, list, this);
        ItemTouchHelper.Callback callback =
                new EditItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);


        //end code


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


}
