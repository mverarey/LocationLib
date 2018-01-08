package com.depotserver.android.locationlib;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MarkerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private CameraPosition mCameraPosition;
    private Direccion direccion;
    private Marker marker;
    private GoogleMap.OnCameraMoveListener onCameraMoveListener;

    private Button btnAceptar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        btnAceptar = (Button) findViewById(R.id.btnAceptar);

        Bundle obtenerExtra = getIntent().getExtras();
        if (obtenerExtra != null) {
            String direccionJSON = obtenerExtra.getString("direccion");
            direccion = new Direccion(direccionJSON);
        } else {
            finish();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                LatLng latLng = mMap.getCameraPosition().target;
                Geocoder geocoder = new Geocoder(MarkerActivity.this);

                try {
                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addressList != null && addressList.size() > 0) {
                        String locality = addressList.get(0).getAddressLine(0);
                        String country = addressList.get(0).getCountryName();

                        Address ub = addressList.get(0);
                        Log.d("LOCACION", ub.toString());
                        if (!locality.isEmpty() && !country.isEmpty()) {
                            btnAceptar.setVisibility(View.VISIBLE);
                            btnAceptar.setText(locality + " " + country);
                            direccion = new Direccion(ub);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        onCameraMoveListener = new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                LatLng latLng = mMap.getCameraPosition().target;
                marker.setPosition(latLng);
            }

        };

        btnAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("direccion", direccion.toJson() );
                setResult(RESULT_OK,data);
                finish();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        LatLng posicion = new LatLng(direccion.latitud, direccion.longitud);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16.0f));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        marker = mMap.addMarker(new MarkerOptions().position(posicion).draggable(true));

        mMap.setOnCameraMoveListener(onCameraMoveListener);
        mMap.setOnCameraIdleListener(onCameraIdleListener);

    }


}
