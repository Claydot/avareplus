package com.ds.avareplus;

import android.content.Context;

import com.ds.avareplus.place.Destination;
import com.ds.avareplus.place.Plan;

import java.util.List;

/**
 * Created by clayd on 7/22/2017.
 */

public class Undo {


    public static Plan[] mPlans;
    public static int counter;

    //undo up to 5 spots

    public Undo() {
        mPlans = new Plan[5];
        counter = 0;
    }


    public static Plan undo() {
        if(counter < 5) counter++;
        return mPlans[counter];
    }

    public static Plan redo() {
        if(counter > 0) counter--;
        return mPlans[counter];
    }

    public static void change(Context cont, StorageService serve, List<Destination> dest) {
        Plan[] planBuffer = new Plan[4];
        for (int i = 0; i < 4; i++) {
            planBuffer[i] = mPlans[i];
        }
        mPlans[4] = null;
        Plan p = new Plan(cont, serve);

        for(Destination d: dest) {
            p.appendDestination(d);
        }


        mPlans[0] = p;



        for (int i = 1; i < 5; i++) {
            mPlans[i] = planBuffer[i - 1];
        }
    }

    public static void clear() {
        mPlans = new Plan[5];
    }
}
