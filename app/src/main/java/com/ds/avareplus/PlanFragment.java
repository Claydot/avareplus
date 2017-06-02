package com.ds.avareplus;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class PlanFragment extends Fragment {

    private FragListener listener;


    private Button mBUp;
    private Button mBDown;
    private Button mBDelete;
    private Button mBClose;
    private View mView;

    public interface FragListener {
        // This can be any number of events to be sent to the activity
        public void up();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mView = inflater.inflate(R.layout.plan_frag, container, false);
        return mView;

    }



    // Define the events that the fragment will use to communicate

/**
    // Store the listener (activity) that will have events fired once the fragment is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragListener) {
            listener = (FragListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement MyListFragment.OnItemSelectedListener");
        }

        mBUp = (Button) mView.findViewById(R.id.location_spinner_chart);
        mBUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                listener.up();
            }



        });

    }
    */

}

