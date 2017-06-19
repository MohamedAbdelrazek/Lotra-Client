package com.salma.Lotra;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LocationLiveTrackActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    FirebaseDatabase mFireBaseDatabase;
    private GoogleMap mGoogleMap;
    private MarkerParce mMarkerParce;
    private String mKey;
    LatLng mLatLng;
    private ArrayList<Marker> mMarkersArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServiceAvailable()) {
            setContentView(R.layout.activity_location_live_track);
        }

        mMarkerParce = (MarkerParce) getIntent().getSerializableExtra("key");
        mKey = mMarkerParce.mKey;
        mLatLng = new LatLng(mMarkerParce.mLat, mMarkerParce.mLng);


        initMap();
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFireBaseDatabase.getInstance().getReference().child("DriverInfo");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mMarkersArray = new ArrayList<>();

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(mLatLng);
        mGoogleMap.moveCamera(cameraUpdate);
        mGoogleMap.addMarker(new MarkerOptions()
                .position(mLatLng)
                .anchor(0.5f, 0.5f).title(mKey)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_2)));
    }
}
