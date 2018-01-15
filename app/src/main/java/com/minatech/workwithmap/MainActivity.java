package com.minatech.workwithmap;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleMapOptions options;
    private List<MarkerItem> items = new ArrayList<>();
    private ClusterManager<MarkerItem> clusterManager;
    private FusedLocationProviderClient client;
    private Location location;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDtectionClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_TERRAIN)
                .compassEnabled(true)
                .zoomControlsEnabled(true);
        client = LocationServices.getFusedLocationProviderClient(this);
        mGeoDataClient = Places.getGeoDataClient(this,null);
        mPlaceDtectionClient = Places.getPlaceDetectionClient(this, null);

        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        clusterManager = new ClusterManager<MarkerItem>(this, mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        checkedPermission();
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        getLastLocation();
       /* LatLng myPlace = new LatLng(23.753632, 90.3792723);
        mMap.addMarker(new MarkerOptions().position(myPlace).title("My Home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace,17));*/

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

               /* items.add(new MarkerItem(latLng, "SELECTED","Place"));
                clusterManager.addItems(items);
                clusterManager.cluster();*/
            }
        });

    }

    private void getLastLocation() {

        checkedPermission();
        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isComplete() && task!=null){
                    location = task.getResult();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),location.getLongitude()),24
                    ));
                }


            }
        });

    }

    private void checkedPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},0);

            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        switch (item.getItemId()){
            case R.id.current_place:
                getLikelihoodPlaces();
                break;
            case R.id.place_picker:
                break;
            case R.id.autocomplete_place:
                    break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getLikelihoodPlaces() {
        checkedPermission();
        mPlaceDtectionClient.getCurrentPlace(null).addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {

                if (task.isComplete() && task!= null){
                    PlaceLikelihoodBufferResponse bufferResponse = task.getResult();
                    int count = bufferResponse.getCount();
                    String[] names = new String[count];
                    String[] address = new String[count];
                    LatLng[] latLngs = new LatLng[count];



                    for (int i=0; i<count; i++){

                       PlaceLikelihood placeLikelihood= bufferResponse.get(i);
                       names[i] = (String) placeLikelihood.getPlace().getName();
                       address[i] = (String) placeLikelihood.getPlace().getAddress();
                       latLngs[i] = placeLikelihood.getPlace().getLatLng();

                       items.add(new MarkerItem(latLngs[i],names[i],address[i]));
                       clusterManager.addItems(items);
                       clusterManager.cluster();
                    }

                    showLikeliHoodPlace(names,address,latLngs);
                }
            }
        });

    }

    private void showLikeliHoodPlace(final String[] names, final String[] address, final LatLng[] latLngs) {



        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                LatLng latLng = latLngs[i];
                String name = names[i];
                String add = address[i];

                mMap.addMarker(new MarkerOptions().position(latLng).title(name).snippet(add));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
            }
        };

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setItems(names, listener)
                .show();
    }




}
