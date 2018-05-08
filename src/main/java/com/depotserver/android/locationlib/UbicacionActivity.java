package com.depotserver.android.locationlib;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.depotserver.android.locationlib.pojo.GoogleMapsGeoCoder;
import com.depotserver.android.locationlib.pojo.Result;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UbicacionActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // The app-specific constant when requesting the location permission
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    final static int request_Code = 601;

    private String GOOGLE_API_KEY = "";
    private ProgressBar progress;
    private RecyclerView mRecyclerView;
    private UbicacionAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LocationManager locationManager;

    private List<Direccion> direcciones;

    // The client object for connecting to the Google API
    private GoogleApiClient mGoogleApiClient;

    private String googleMapsApiURL = "https://maps.googleapis.com";

    // The RecyclerView and associated objects for displaying the nearby coffee spots
    private RecyclerView placePicker;
    private LinearLayoutManager placePickerManager;
    private RecyclerView.Adapter placePickerAdapter;
    private String provider;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        // Requests for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.app_requiere_permiso), Toast.LENGTH_LONG).show();
            finish();
        }

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            GOOGLE_API_KEY = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("GOOGLE_MAPS_API", "Failed to load meta-data, NameNotFound: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Debe registrar una clave de Google API en gradle.properties", Toast.LENGTH_LONG).show();
        } catch (NullPointerException e) {
            Log.e("GOOGLE_MAPS_API", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        progress = (ProgressBar) findViewById(R.id.progressBar);
        direcciones = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new UbicacionAdapter(direcciones, getApplicationContext(), new Direccion.OnItemClickListener() {
            public void onItemOkClick(Direccion item) {
                // Toast.makeText(getBaseContext(), "Objeto seleccionado", Toast.LENGTH_LONG).show();
                Intent data = new Intent();
                data.putExtra("direccion", item.toJson());
                setResult(RESULT_OK, data);
                finish();
            }

            public void onItemEditClick(Direccion item) {
                iniciarEditor(item);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        initializeData();

    }

    @Override
    public void onConnected(Bundle connectionHint) {

        // Checks for location permissions at runtime (required for API >= 23)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Makes a Google API request for the user's last known location
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {

                String userLL = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();
                double userLLAcc = mLastLocation.getAccuracy();

                try {

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(googleMapsApiURL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    LocationService location = retrofit.create(LocationService.class);
                    Call<GoogleMapsGeoCoder> stpCall = location.snapToPlace(userLL, userLLAcc, GOOGLE_API_KEY);

                    stpCall.enqueue(new Callback<GoogleMapsGeoCoder>() {
                        @Override
                        public void onResponse(Call<GoogleMapsGeoCoder> call, Response<GoogleMapsGeoCoder> response) {
                            int statusCode = response.code();
                            GoogleMapsGeoCoder fjson = response.body();

                            direcciones.clear();
                            List<Result> opciones = fjson.getResults();
                            for (int i = 0; i < opciones.size(); i++) {
                                direcciones.add(new Direccion(opciones.get(i)));
                            }
                            mAdapter.notifyDataSetChanged();
                            progress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(Call<GoogleMapsGeoCoder> call, Throwable t) {

                            opcionLocal();
                            //Toast.makeText(getApplicationContext(), "No se ha obtenido respuesta del servidor!", Toast.LENGTH_LONG).show();
                            //finish();
                        }

                    });

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    finish();
                }

            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.ubicacion_no_encontrada), Toast.LENGTH_LONG).show();
                finish();
            }

        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.permiso_ubicacion_rechazado), Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        opcionLocal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reconnects to the Google API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Disconnects from the Google API
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    private void opcionLocal(){

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle( getResources().getString(R.string.conexion_no_disponible))
                .setMessage(getResources().getString(R.string.guardar_actual))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(mLastLocation != null){

                            Direccion direccion = new Direccion("SIN CONEXION", "", "", "",
                                    "", "", "", "", getResources().getString(R.string.sin_conexion), (float)mLastLocation.getLatitude(),
                                    (float)mLastLocation.getLongitude(), mLastLocation.getAccuracy());
                            Intent data = new Intent();
                            data.putExtra("direccion", direccion.toJson() );
                            setResult(RESULT_OK,data);
                            finish();

                        }else {

                            LocationListener mlocListener = new DS_LocationListener();
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                finish();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_por_permisos_o_conexion), Toast.LENGTH_LONG).show();
                            }
                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Criteria criteria = new Criteria();
                            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                            criteria.setAccuracy(Criteria.ACCURACY_FINE);
                            provider = locationManager.getBestProvider(criteria, true);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void initializeData() {

        progress.setIndeterminate(true);
        progress.animate();

        // Creates a connection to the Google API for location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void iniciarEditor(Direccion item){

        Intent intent = new Intent(getApplicationContext(), EditorActivity.class);
        intent.putExtra("direccion", item.toJson());
        startActivityForResult(intent, request_Code);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == request_Code) {
            if (resultCode == RESULT_OK) {
                Intent dataR = new Intent();
                dataR.putExtra("direccion", data.getStringExtra("direccion") );
                setResult(RESULT_OK,data);
                finish();
            }
        }
    }

    public class DS_LocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            loc.getLatitude();
            loc.getLongitude();
            //String Text = "My current location is: " + "Latitude = "
            //        + loc.getLatitude() + "Longitude = " + loc.getLongitude();
            //Toast.makeText( getApplicationContext(), Text, Toast.LENGTH_SHORT)
            //        .show();
            Log.d("TAG", "Starting..");

            Direccion direccion = new Direccion("SIN CONEXION", "", "", "",
                    "", "", "", "", getResources().getString(R.string.sin_conexion), (float)loc.getLatitude(),
                    (float)loc.getLongitude(), loc.getAccuracy());
            Intent data = new Intent();
            data.putExtra("direccion", direccion.toJson() );
            setResult(RESULT_OK,data);
            locationManager.removeUpdates(this);
            finish();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(), "Gps Enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }
}
