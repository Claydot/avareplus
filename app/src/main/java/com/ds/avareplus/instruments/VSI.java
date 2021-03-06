/*
Copyright (c) 2014, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avareplus.instruments;

import com.ds.avareplus.gps.GpsParams;

/*** 
 * A simple vertical speed indicator instrument
 * @author Ron
 *
 */

public class VSI {
	private GpsParams	mVSIParams;
	private double		mVSI;
	
	public VSI() {
		mVSI = 0;
		mVSIParams = new GpsParams(null);
	}
	
	// Called to indicate a change in position. 
	public void updateValue(GpsParams gpsParams) {
        double tdiff = ((double)(gpsParams.getTime() - mVSIParams.getTime()) / 1000.0);
    	// Calculate the instantaneous vertical speed in ft/min if it's been more than a minute
    	if(tdiff > 1) {
    		mVSI = ((double)(gpsParams.getAltitude() - mVSIParams.getAltitude())) * (60 / tdiff);
    		mVSIParams = gpsParams;
    	}
	}
	
	// Return the current VSI value
	public double getValue() {
		return mVSI;
	}
}
