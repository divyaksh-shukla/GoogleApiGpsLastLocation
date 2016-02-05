package com.example.admin.googleapigpslastlocation;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class GpsLastLocation extends ActionBarActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "basic-location-sample";
    protected static final long UPDATE_INTERVAL = 1000;
    protected static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL/2;
    protected static final float SMALLEST_DISPLACEMENT = 5;
    protected boolean mRequestingLocationUpdates;
    private static final String MUMMY_PHONE = "";
    private static final String PAPA_PHONE = "";
    private static final String MAPS_URL = "I am here:\nhttp://maps.google.com/maps?q=";

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected Location mCurrentLocation;

    protected LocationRequest mLocationRequest;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected Button btn_Send_sms;
    protected Button btn_Start_Location_Updates;
    protected Button btn_Stop_Location_Updates;

    String strTextMessage;
    String str_latitude;
    String str_longitude;

    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_last_location);

        mLatitudeText = (TextView) findViewById(R.id.LatitudeValue);
        mLongitudeText = (TextView) findViewById(R.id.LongitudeValue);
        btn_Send_sms = (Button) findViewById(R.id.sendSMS);
        btn_Start_Location_Updates = (Button) findViewById(R.id.start_updates);
        btn_Stop_Location_Updates = (Button) findViewById(R.id.stop_updates);

        mRequestingLocationUpdates = false;

        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();

        btn_Send_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSms();
            }
        });
        btn_Start_Location_Updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startUpdatesButtonHandler(view);
            }
        });
        btn_Stop_Location_Updates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopUpdatesButtonHandler(view);
            }
        });
    }

    private void updateValuesFromBundle(Bundle savedInstanceState){
        Log.i(TAG, "Updating Values From Bundle");
        if (savedInstanceState!=null){
            if(savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)){
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            if (savedInstanceState.keySet().contains(LOCATION_KEY)){
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)){

            }

            updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient(){

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        createLocationRequest();

    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startUpdatesButtonHandler(View view){
        if (!mRequestingLocationUpdates){
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
        Log.i(TAG,"Started Updates");
    }

    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
        Log.i(TAG,"Stopped Updates");
    }

    protected void startLocationUpdates(){
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    private void setButtonsEnabledState(){
        if(mRequestingLocationUpdates){
            btn_Start_Location_Updates.setEnabled(false);
            btn_Stop_Location_Updates.setEnabled(true);
        }
        else {
            btn_Start_Location_Updates.setEnabled(true);
            btn_Stop_Location_Updates.setEnabled(false);
        }
    }

    private void updateUI(){
        if (mCurrentLocation!=null){
            str_latitude = String.valueOf(mCurrentLocation.getLatitude());
            str_longitude = String.valueOf(mCurrentLocation.getLongitude());
            mLatitudeText.setText(str_latitude);
            mLongitudeText.setText(str_longitude);
        }
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void sendSms(){
        Log.i(TAG, "sendSMS()");
        strTextMessage = MAPS_URL+str_latitude+","+str_longitude;
        Log.i(TAG, "Framing Message");
        try{
            Log.i(TAG, "Entering try of sendSMS()");
            SmsManager smsManager = SmsManager.getDefault();
            Log.i(TAG, "getDefault()");
            smsManager.sendTextMessage(MUMMY_PHONE,null,strTextMessage,null,null);
            smsManager.sendTextMessage(PAPA_PHONE,null,strTextMessage,null,null);
            Log.i(TAG, "Sending SMS");
            Toast.makeText(this,"Message Sent",Toast.LENGTH_LONG).show();
        }

        catch (Exception e){
            Toast.makeText(this,"Cannot send SMS",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    public void onPause(){
        super.onResume();
        if (mGoogleApiClient.isConnected()){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint){
        Log.i(TAG,"Connected to GoogleApiClient");
        if(mCurrentLocation==null){
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mCurrentLocation !=null)
        {
            updateUI();
        }

        else {
            Toast.makeText(this, "No Location Detected", Toast.LENGTH_LONG).show();
        }

        if(mRequestingLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location){
        mCurrentLocation = location;
        updateUI();
        Toast.makeText(this,"Location Updated",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended( int cause ) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gps_last_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
