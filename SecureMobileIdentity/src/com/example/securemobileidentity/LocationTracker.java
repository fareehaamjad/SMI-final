package com.example.securemobileidentity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public final class LocationTracker extends Service implements ConnectionCallbacks, OnConnectionFailedListener
{

	protected static final String TAG = "LocationTracker";

	/**
	 * Provides the entry point to Google Play services.
	 */
	protected static GoogleApiClient mGoogleApiClient;

	private static LocationTracker context;


	@Override
	public void onCreate() 
	{
		super.onCreate();

		Log.i(TAG, "onCreate");

		context = this;

		// Kick off the process of building a GoogleApiClient and requesting the LocationServices
		// API.
		if (checkPlayServices())
		{
			buildGoogleApiClient();
		}

	}

	@Override
	public void onStart(Intent intent, int startId) 
	{      
		super.onStart(intent, startId);
		Log.i(TAG, "onStart");

		mGoogleApiClient.connect();
	}

	@Override
	public void onDestroy() 
	{       
		// handler.removeCallbacks(sendUpdatesToUI);     
		super.onDestroy();
		Log.v("STOP_SERVICE", "DONE");
		mGoogleApiClient.disconnect();
		
		Constants.isGooglePlayConnected = false;
	}   



	/**
	 * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
	 */
	protected synchronized void buildGoogleApiClient() 
	{
		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(LocationServices.API)
		.build();
	}


	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		// Provides a simple way of getting a device's location and is well suited for
		// applications that do not require a fine-grained location and that do not need location
		// updates. Gets the best and most recent location currently available, which may be null
		// in rare cases when a location is not available.
		Constants.mGPS = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		if (Constants.mGPS == null)  
		{
			Toast.makeText(Constants.currentContext, "No Location detected", Toast.LENGTH_LONG).show();
		}
		
		Constants.isGooglePlayConnected = true;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
		
		Constants.isGooglePlayConnected = false;
	}


	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason. We call connect() to
		// attempt to re-establish the connection.
		Log.i(TAG, "Connection suspended");
		mGoogleApiClient.connect();
		
		Constants.isGooglePlayConnected = false;
	}


	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}

	/**
	 * Method to verify google play services on the device
	 * */
	private boolean checkPlayServices() 
	{
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) 
			{
				GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) MainActivity.context, 1000).show();
			} else 
			{
				Log.i(TAG, "This device is not supported");
				Toast.makeText(getApplicationContext(),
						"This device is not supported.", Toast.LENGTH_LONG)
						.show();
			}
			return false;
		}
		return true;
	}

}