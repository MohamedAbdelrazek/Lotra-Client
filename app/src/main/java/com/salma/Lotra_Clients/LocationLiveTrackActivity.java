package com.salma.Lotra_Clients;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LocationLiveTrackActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    FirebaseDatabase mFireBaseDatabase;
    private GoogleMap mGoogleMap;
    private MarkerParceModel mMarkerParceModel;
    private String mKey;
    LatLng mLatLng;
    private LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServiceAvailable()) {
            setContentView(R.layout.activity_location_live_track);
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mMarkerParceModel = (MarkerParceModel) getIntent().getSerializableExtra("key");
        mKey = mMarkerParceModel.mKey;
        mLatLng = new LatLng(mMarkerParceModel.mLat, mMarkerParceModel.mLng);


        initMap();
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFireBaseDatabase.getInstance().getReference().child("DriverInfo");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().equals(mKey)) {
                    DriverModel driverModel = dataSnapshot.getValue(DriverModel.class);
                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(driverModel.Latitude, driverModel.Longitude))
                            .anchor(0.5f, 0.5f)
                            .title(driverModel.DriverName)
                            .snippet("Bus Number: " + driverModel.BusNumber)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.dotsimage)));

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseRef.addChildEventListener(mChildEventListener);


    }


    private void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_tracking_id);
        supportMapFragment.getMapAsync(this);

    }
    public Boolean googleServiceAvailable() {

        GoogleApiAvailability apiClient = GoogleApiAvailability.getInstance();
        int isAvailable = apiClient.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (apiClient.isUserResolvableError(isAvailable)) {
            Dialog dialog = apiClient.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant Connect to play services !", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    LocationRequest mLocationRequest;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(mLatLng);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(mLatLng)
                .anchor(0.5f, 0.5f).title(mKey)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_2)));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(Bundle bundle) {


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {

            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .anchor(0.5f, 0.5f)
                    .title("MY LOCATION")
                    .snippet("You are here!")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dotsimage)));
        }
    }
}
