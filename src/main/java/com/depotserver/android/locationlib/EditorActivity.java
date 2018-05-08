package com.depotserver.android.locationlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class EditorActivity extends AppCompatActivity implements OnMapReadyCallback{

    EditText txtdireccion, txtnumExterno, txtnumInterno,txtcolonia, txtmunicipio, txtciudad,
            txtestado, txtcp, txtformateada, txtlatitud, txtlongitud, txtprecision;

    Direccion direccion;
    Button btnMapa, btnGuardar;
    Marker marker;
    private int request_Code = 202;
    private SupportMapFragment mapFragment;
    private GoogleMap mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        txtdireccion = (EditText) findViewById(R.id.txtdireccion);
        txtnumExterno = (EditText) findViewById(R.id.txtnumExterno);
        txtnumInterno = (EditText) findViewById(R.id.txtnumInterno);
        txtcolonia = (EditText) findViewById(R.id.txtcolonia);
        txtmunicipio = (EditText) findViewById(R.id.txtmunicipio);
        txtciudad = (EditText) findViewById(R.id.txtciudad);
        txtestado = (EditText) findViewById(R.id.txtestado);
        txtcp = (EditText) findViewById(R.id.txtcp);
        txtformateada = (EditText) findViewById(R.id.txtformateada);
        txtlatitud = (EditText) findViewById(R.id.txtlatitud);
        txtlongitud = (EditText) findViewById(R.id.txtlongitud);
        txtprecision = (EditText) findViewById(R.id.txtprecision);
        btnMapa = (Button) findViewById(R.id.btnMapa);
        btnGuardar = (Button) findViewById(R.id.btnGuardar);

        Bundle obtenerExtra = getIntent().getExtras();
        if (obtenerExtra != null) {
            String direccionJSON = obtenerExtra.getString("direccion");
            direccion = new Direccion(direccionJSON);
        }else{
            finish();
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
                intent.putExtra("direccion", direccion.toJson());
                startActivityForResult(intent, request_Code);
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                direccion.direccion = txtdireccion.getText().toString();
                direccion.numExterno= txtnumExterno.getText().toString();
                direccion.numInterno = txtnumInterno.getText().toString();
                direccion.colonia = txtcolonia.getText().toString();
                direccion.municipio = txtmunicipio.getText().toString();
                direccion.ciudad = txtciudad.getText().toString();
                direccion.estado = txtestado.getText().toString();
                direccion.cp = txtcp.getText().toString();
                direccion.formateada = direccion.direccion + " " + direccion.numExterno + " " + direccion.numInterno + ", " + direccion.colonia + ", " + direccion.municipio + ", " + direccion.ciudad + ", " + direccion.estado + ", " + direccion.cp;

                Intent data = new Intent();
                data.putExtra("direccion", direccion.toJson() );
                setResult(RESULT_OK,data);
                finish();
            }
        });

        actualizarInformacion();
    }

    private void actualizarInformacion() {
        txtdireccion.setText(direccion.direccion);
        txtnumExterno.setText(direccion.numExterno);
        txtnumInterno.setText(direccion.numInterno);
        txtcolonia.setText(direccion.colonia);
        txtmunicipio.setText(direccion.municipio);
        txtciudad.setText(direccion.ciudad);
        txtestado.setText(direccion.estado);
        txtcp.setText(direccion.cp);
        txtformateada.setText(direccion.formateada);
        txtlatitud.setText(direccion.latitud + "");
        txtlongitud.setText(direccion.longitud + "");
        txtprecision.setText(direccion.precision + "");

        try{
            LatLng ubicacion = new LatLng(direccion.latitud, direccion.longitud);
            marker.setPosition(ubicacion);
            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 17.0f));
        }catch (Exception e){}
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mapa = googleMap;
        LatLng ubicacion = new LatLng(direccion.latitud, direccion.longitud);
        marker = googleMap.addMarker(new MarkerOptions().position(ubicacion).title(getResources().getString(R.string.ubicacion_detectada) ));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 17.0f));
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == request_Code) {
            if (resultCode == RESULT_OK) {
                String direccionJSON = data.getStringExtra("direccion");
                direccion = new Direccion(direccionJSON);
                actualizarInformacion();
            }
        }
    }

}
