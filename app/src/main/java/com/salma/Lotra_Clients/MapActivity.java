package com.salma.Lotra_Clients;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION = 43;
    private DatabaseReference mDatabaseRef;
    private ChildEventListener mChildEventListener;
    FirebaseDatabase mFireBaseDatabase;
    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_main);

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
                        .snippet("# OF Passengers: " + driverModel.NumberOfPassenger)
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



    @Override
    public void onMapReady(GoogleMap googleMap) {


        mGoogleMap = googleMap;

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final MarkerParceModel markerParceModel = new MarkerParceModel();
                markerParceModel.mLat = marker.getPosition().latitude;
                markerParceModel.mLng = marker.getPosition().longitude;
                markerParceModel.mKey = marker.getTitle();

                final AlertDialog dialog = new AlertDialog.Builder(MapActivity.this)
                        .create();
                LayoutInflater layoutInflater = LayoutInflater.from(MapActivity.this);
                dialog.setView(layoutInflater.inflate(R.layout.message_dialog, null));
                dialog.show();
                Button okButton = (Button) dialog.findViewById(R.id.ok);
                Button cancelButton = (Button) dialog.findViewById(R.id.cancel);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), LocationLiveTrackActivity.class);
                        intent.putExtra("key", markerParceModel);
                        startActivity(intent);
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                return false;
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission is required !")
                        .setMessage("The app will shut down if the permission denied! ")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }

        } else {
            mGoogleMap.setMyLocationEnabled(true);


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mGoogleMap.setMyLocationEnabled(true);
                    }


                } else {

                }
                return;
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.sign_out_menu:

                cleanUp();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void cleanUp() {

        ProgressDialog pd = new ProgressDialog(MapActivity.this);
        pd.setMessage("loading");
        pd.show();
        AuthUI.getInstance()
                .signOut(this)

                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                            finish();
                        }
                    }
                });
    }

}