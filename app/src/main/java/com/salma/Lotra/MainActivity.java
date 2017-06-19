package com.salma.Lotra;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    FirebaseDatabase mFireBaseDatabase;
    private GoogleMap mGoogleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServiceAvailable()) {
            setContentView(R.layout.activity_main);
        }
        initMap();
        mFireBaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseRef = mFireBaseDatabase.getInstance().getReference().child("DriverInfo");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                DriverModel driverModel = dataSnapshot.getValue(DriverModel.class);
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(driverModel.Latitude, driverModel.Longitude))
                        .anchor(0.5f, 0.5f)
                        .title(dataSnapshot.getKey())
                        .snippet("Bus Number: " + driverModel.BusNumber)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_2)));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {


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
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment_id);
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
        Toast.makeText(this, "map is ready!", Toast.LENGTH_SHORT).show();

        //  mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getApplicationContext(), LocationLiveTrackActivity.class);
                MarkerParce markerParce = new MarkerParce();
                markerParce.mLat = marker.getPosition().latitude;
                markerParce.mLng = marker.getPosition().longitude;
                markerParce.mKey = marker.getTitle();
                intent.putExtra("key", markerParce);
                startActivity(intent);
                return false;
            }
        });
    }
}