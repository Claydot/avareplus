/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package com.ds.avareplus.weather;

import android.content.Context;
import android.util.SparseArray;

import com.ds.avareplus.StorageService;
import com.ds.avareplus.adsb.NexradBitmap;
import com.ds.avareplus.adsb.NexradImage;
import com.ds.avareplus.adsb.NexradImageConus;
import com.ds.avareplus.place.Destination;
import com.ds.avareplus.position.Origin;
import com.ds.avareplus.shapes.DrawingContext;
import com.ds.avareplus.storage.DataSource;
import com.ds.avareplus.storage.Preferences;
import com.ds.avareplus.utils.RateLimitedBackgroundQueue;
import com.ds.avareplus.utils.WeatherHelper;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * 
 * This is where all ADSB weather is collected.
 * @author zkhan
 *
 */
public class AdsbWeatherCache {

    private HashMap<String, Taf> mTaf;
    private HashMap<String, Metar> mMetar;
    private HashMap<String, Airep> mAirep;
    private HashMap<String, WindsAloft> mWinds;
    private NexradImage mNexrad;
    private NexradImageConus mNexradConus;
    private Preferences mPref;
    private RateLimitedBackgroundQueue mMetarQueue;

    /**
     * 
     */
    public AdsbWeatherCache(Context context, StorageService service) {
        mPref = new Preferences(context);
        mTaf = new HashMap<String, Taf>();
        mMetar = new HashMap<String, Metar>();
        mAirep = new HashMap<String, Airep>();
        mWinds = new HashMap<String, WindsAloft>();
        mNexrad = new NexradImage();
        mMetarQueue = new RateLimitedBackgroundQueue(service);
        mNexradConus = new NexradImageConus();
    }

    /**
     * 
     * @return
     */
    public NexradImage getNexrad() {
        return mNexrad;
    }
    
    /**
     * 
     * @return
     */
    public NexradImageConus getNexradConus() {
        return mNexradConus;
    }
    
    /**
     * 
     * @param time
     * @param location
     * @param data
     */
    public void putMetar(long time, String location, String data, String flightCategory) {
        if(!mPref.useAdsbWeather()) {
            return;
        }

        Metar m = new Metar();
        m.rawText = location + " " + data;
        m.stationId = location;
        Date dt = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("ddHHmm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        m.time = sdf.format(dt) + "Z";
        m.flightCategory = flightCategory;
        m.timestamp = System.currentTimeMillis();
        mMetar.put(location, m);
        mMetarQueue.insertMetarInQueue(m); // This will slowly make a metar map
    }


    /*
 * Determine if shape belong to a screen based on Screen longitude and latitude
 * and shape max/min longitude latitude
 */
    public static boolean isOnScreen(Origin origin, double lat, double lon) {

        double maxLatScreen = origin.getLatScreenTop();
        double minLatScreen = origin.getLatScreenBot();
        double minLonScreen = origin.getLonScreenLeft();
        double maxLonScreen = origin.getLonScreenRight();

        boolean isInLat = lat < maxLatScreen && lat > minLatScreen;
        boolean isInLon = lon < maxLonScreen && lon > minLonScreen;
        return isInLat && isInLon;
    }

    /**
     * Draw metar map from ADSB
     * @param ctx
     * @param map
     * @param shouldDraw
     */
    public static void drawMetars(DrawingContext ctx, HashMap<String, Metar> map, boolean shouldDraw) {
        if(0 == ctx.pref.showLayer() || (!shouldDraw) || (!ctx.pref.useAdsbWeather())) {
            // This shows only for metar layer, and when adsb is used
            return;
        }

        Set<String> keys = map.keySet();
        for(String key : keys) {
            Metar m = map.get(key);
            if(!isOnScreen(ctx.origin, m.lat, m.lon)) {
                continue;
            }
            float x = (float)ctx.origin.getOffsetX(m.lon);
            float y = (float)ctx.origin.getOffsetY(m.lat);
            String text = m.flightCategory;
            if (ctx.pref.isShowLabelMETARS()) {
                if(WeatherHelper.metarColorString(m.flightCategory).equals("white")) {
                    ctx.service.getShadowedText().drawAlpha(ctx.canvas, ctx.textPaint,
                            "NA", WeatherHelper.metarColor(m.flightCategory), x, y, ctx.pref.showLayer());
                }
                else {
                    ctx.service.getShadowedText().drawAlpha(ctx.canvas, ctx.textPaint,
                            text, WeatherHelper.metarColor(m.flightCategory), x, y, ctx.pref.showLayer());
                }
            }
            else {
                ctx.paint.setColor(0);
                ctx.paint.setAlpha(ctx.pref.showLayer());
                ctx.canvas.drawCircle(x, y, ctx.dip2pix * 9, ctx.paint);
                ctx.paint.setColor(WeatherHelper.metarColor(m.flightCategory));
                ctx.paint.setAlpha(ctx.pref.showLayer());
                ctx.canvas.drawCircle(x, y, ctx.dip2pix * 8, ctx.paint);
            }
            /*
            */
            ctx.paint.setAlpha(255);
        }
    }

    /**
     *
     * @return
     */
    public HashMap<String, Metar> getAllMetars() {
        return mMetar;
    }

    /**
     * 
     * @param time
     * @param location
     * @param data
     */
    public void putTaf(long time, String location, String data) {
        if(!mPref.useAdsbWeather()) {
            return;
        }    
        Taf f = new Taf();
        f.rawText = location + " " + data;
        f.stationId = location;
        Date dt = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("ddHHmm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        f.time = sdf.format(dt) + "Z";
        f.timestamp = System.currentTimeMillis();
        mTaf.put(location, f);        
    }
    
    /**
     * 
     * @param time
     * @param location
     * @param data
     */
    public void putAirep(long time, String location, String data, DataSource db) {
        if(!mPref.useAdsbWeather()) {
            return;
        }    
        String lonlat = db.findLonLat(location, Destination.BASE);
        if(null == lonlat) {
            return;
        }
        String tokens[] = lonlat.split(",");
        if(tokens.length != 2) {
            return;
        }
        
        Airep a = new Airep();
        a.lon = Float.parseFloat(tokens[0]);
        a.lat = Float.parseFloat(tokens[1]);
        a.rawText = data;
        a.reportType = "PIREP";
        Date dt = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("ddHHmm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        a.time = sdf.format(dt) + "Z";
        a.timestamp = System.currentTimeMillis();
        
        mAirep.put(location, a);
    }
    
    /**
     * 
     * @param time
     * @param location
     * @param data
     */
    public void putWinds(long time, String location, String data) {
        if(!mPref.useAdsbWeather()) {
            return;
        }    
        WindsAloft w = new WindsAloft();
        w.station = location;
        
        /*
         * Clear garbage spaces etc. Convert to Avare format
         */
        String winds[] = data.split(",");
        if(winds.length < 9) {
            return;
        }
        w.w3k = winds[0];
        w.w6k = winds[1];
        w.w9k = winds[2];
        w.w12k = winds[3];
        w.w18k = winds[4];
        w.w24k = winds[5];
        w.w30k = winds[6];
        w.w34k = winds[7];
        w.w39k = winds[8];
        Date dt = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("ddHHmm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        w.time = sdf.format(dt) + "Z";
        
        /*
         * Find lon/lat of station
         */
        float coords[] = new float[2];
        if(!Stations.getStationLocation(location, coords)) {
            return;
        }
        
        w.lon = coords[0];
        w.lat = coords[1];
        w.timestamp = System.currentTimeMillis();
        mWinds.put(location, w);
    }
    
    /**
     * 
     * @param time
     * @param block
     * @param empty
     * @param isConus
     * @param data
     * @param cols
     * @param rows
     */
    public void putImg(long time, int block, int empty[], boolean isConus, int data[], int cols, int rows) {
        if(!mPref.useAdsbWeather()) {
            return;
        }
        if(isConus) {
            mNexradConus.putImg(time, block, empty, isConus, data, cols, rows);            
        }
        else {
            mNexrad.putImg(time, block, empty, isConus, data, cols, rows);            
        }
    }

    /**
     * 
     * @param airport
     * @return
     */
    public Taf getTaf(String airport) {
        Taf taf = mTaf.get("K" + airport);
        return taf;
    }

    /**
     * 
     * @param airport
     * @return
     */
    public Metar getMETAR(String airport) {
        return mMetar.get("K" + airport);
    }

    /**
     * 
     * @param lon
     * @param lat
     * @return
     */
    public LinkedList<Airep> getAireps(double lon, double lat) {
        
        LinkedList<Airep> ret = new LinkedList<Airep>();
        
        /*
         * Find closest aireps
         */
        for(Airep a : mAirep.values()) {
            
            /*
             * Same formula as in database helper
             */
            if(
                    (a.lat > (lat - Airep.RADIUS)) && (a.lat < (lat + Airep.RADIUS)) &&
                    (a.lon > (lon - Airep.RADIUS)) && (a.lon < (lon + Airep.RADIUS))) {
                Airep n = new Airep(a);
                ret.add(n);
            }
            
        }

        
        return ret;
    }

    /**
     * 
     * @param lon
     * @param lat
     * @return
     */
    public WindsAloft getWindsAloft(double lon, double lat) {
        
        WindsAloft toret = null;
        double oldDistance = 1E10;
        
        /*
         * Find closest wind
         */
        for(WindsAloft w : mWinds.values()) {
            float mlon = w.lon;
            float mlat = w.lat;
            /*
             * Distance less? use this one
             */
            double dis = (mlon - lon) * (mlon - lon) + (mlat - lat) * (mlat - lat);
            if(oldDistance > dis) {
                oldDistance = dis;
                toret = w;
            } 
        }

        /*
         * Copy it because we change the title
         */
        if(null == toret) {
            return null;
        }
        WindsAloft w1 = new WindsAloft(toret);
        return w1;

    }

    /*
     * ALL ADSB weather should be kaput after expiry
     */
    public void sweep() {
        long now = System.currentTimeMillis();
        int expiry = mPref.getExpiryTime() * 60 * 1000;

        /*
         * Go at them one by one
         * LinkedList saves against concurrent modification exception
         */
        LinkedList<String> keys;
        
        /*
         * Winds
         */
        keys = new LinkedList<String>();
        for (String key : mWinds.keySet()) {
            WindsAloft w = mWinds.get(key);
            long diff = (now - w.timestamp) - expiry;
            if(diff > 0) {
                keys.add(key);
            }
        }
        for(String key : keys) {
            mWinds.remove(key);
        }
        
        /*
         * Taf
         */
        keys = new LinkedList<String>();
        for (String key : mTaf.keySet()) {
            Taf f = mTaf.get(key);
            long diff = (now - f.timestamp) - expiry;
            if(diff > 0) {
                keys.add(key);
            }
        }
        for(String key : keys) {
            mTaf.remove(key);
        }
        
        /*
         * Metar
         */
        keys = new LinkedList<String>();
        for (String key : mMetar.keySet()) {
            Metar m = mMetar.get(key);
            long diff = (now - m.timestamp) - expiry;
            if(diff > 0) {
                keys.add(key);
            }
        }
        for(String key : keys) {
            mMetar.remove(key);
        }

        /*
         * Airep
         */
        keys = new LinkedList<String>();
        for (String key : mAirep.keySet()) {
            Airep a = mAirep.get(key);
            long diff = (now - a.timestamp) - expiry;
            if(diff > 0) {
                keys.add(key);
            }
        }
        for(String key : keys) {
            mAirep.remove(key);
        }
        
        /*
         * Nexrad
         */
        LinkedList<Integer>keyi = new LinkedList<Integer>();
        SparseArray<NexradBitmap> img = mNexrad.getImages();
        for(int i = 0; i < img.size(); i++) {
            NexradBitmap n = img.valueAt(i);
            long diff = (now - n.timestamp) - expiry;
            if(diff > 0) {
                keyi.add(img.keyAt(i));
            }
        }
        for(Integer key : keyi) {
            img.remove(key);
        }
    }
}
