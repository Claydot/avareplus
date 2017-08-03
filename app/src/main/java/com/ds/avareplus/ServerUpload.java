package com.ds.avareplus;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 * Created by clayd on 7/30/2017.
 */

public class ServerUpload {
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonS3 s3;
    private TransferUtility transferUtility;


    ServerUpload(Context context) {
        //code for amazon s3 server
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "us-east-2:fc468750-e1b5-4759-812c-1181c37fbc3b", // Identity pool ID
                Regions.US_EAST_2
        );
        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, context);


    }

    //end code for amazon s3 server
    public void upload(String filePath) {

        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(
                "claydodsonresearch",     /* The bucket to upload to */
                file.getName(),    /* The key for the uploaded object */
                file        /* The file where the data to upload exists */
        );


    }
}
