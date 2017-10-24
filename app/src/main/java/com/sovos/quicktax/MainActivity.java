package com.sovos.quicktax;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Bart JV on 12/29/2016.
 */
public class MainActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public final static String GROSS_AMOUNT = "com.sovos.quicktax.AMOUNT";
    public final static String LAT_LNG = "com.sovos.quicktax.LATLNG";
    boolean hasPermission=true;
    private String[] PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION};
    private TextView txtOutputLat, txtOutputLon;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String latitude;
    private String longitude;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!hasPermissions(this, PERMISSIONS)){
                ActivityCompat.requestPermissions(this, PERMISSIONS, 99);
            }
        }

        buildGoogleApiClient();

    }

    /** Called when the user clicks the Calc button */
    public void executeProcess(View view) {

        Intent intent = new Intent(this, CalcResultActivity.class);
        EditText editGrossAmout = (EditText) findViewById(R.id.edit_gross_amount);
        String message = editGrossAmout.getText().toString();
        boolean isValidMessage = hasValidGrossAmountFormat(message);
        if(isValidMessage){
            intent.putExtra(GROSS_AMOUNT, message);
            intent.putExtra(LAT_LNG, latitude+","+longitude);
            startActivity(intent);
        }

    }

    public void futureImprovementMessage(View view){
        Toast toast;
        toast = Toast.makeText(getApplicationContext(), "Wouldn't it be awesome to add the GSC?!", Toast.LENGTH_LONG);
        if(toast!=null){
            toast.show();
        }
    }

    private boolean hasValidGrossAmountFormat(String message){
        boolean hasValidFormat = true;
        try{
            Double.parseDouble(message);
        }catch (NumberFormatException e){

            Toast toast = Toast.makeText(getApplicationContext(), "Invalid Input. Please type a valid amount.", Toast.LENGTH_LONG);
            TextView toastMessage = (TextView)toast.getView().findViewById(android.R.id.message);
            toastMessage.setTextColor(Color.BLACK);
            toast.getView().setBackgroundColor(Color.RED);
            toast.show();
            hasValidFormat = false;
        }

        return hasValidFormat;
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = String.valueOf(mLastLocation.getLatitude());
            longitude = String.valueOf(mLastLocation.getLongitude());

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
    }
}