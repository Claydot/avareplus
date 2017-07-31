package com.ds.avareplus;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by clayd on 7/30/2017.
 */

public class ResearchFile {


    private static File gpxfile;

    public static void createFile(Context mcoContext, String sFileName) {

        File file = new File(mcoContext.getFilesDir(),"research");
        if(!file.exists()){
            file.mkdir();
        }
        try{
            gpxfile = new File(file, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
        }catch (Exception e){

        }
    }


    public static String getFilePath() {
        return gpxfile.getAbsolutePath();
    }


    public static void append(String sBody){

        try{
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(sBody + '\n');
            writer.flush();
            writer.close();
        }catch (Exception e){

        }
    }
}
