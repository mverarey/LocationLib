package com.depotserver.android.locationlib;

import android.location.Address;

import com.depotserver.android.locationlib.pojo.AddressComponent;
import com.depotserver.android.locationlib.pojo.Geometry;
import com.depotserver.android.locationlib.pojo.Result;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

/**
 * Created by mauricio on 02/01/18.
 */

public class Direccion {

    public String direccion, numExterno, numInterno, colonia, municipio, ciudad, estado, cp, formateada;
    public float latitud, longitud, precision;

    public interface OnItemClickListener {
        void onItemOkClick(Direccion item);
        void onItemEditClick(Direccion item);
    }

    public Direccion(String direccion, String numExterno, String numInterno, String colonia,
              String municipio, String ciudad, String estado, String cp, String formateada, float latitud,
              float longitud, float precision){
        this.direccion = direccion;
        this.numExterno = numExterno;
        this.numInterno = numInterno;
        this.colonia = colonia;
        this.municipio = municipio;
        this.ciudad = ciudad;
        this.estado = estado;
        this.cp = cp;
        this.formateada = formateada;
        this.latitud = latitud;
        this.longitud = longitud;
        this.precision = precision;

    }

    public Direccion(String json){
        Direccion a = this.fromJson(json);
        this.direccion = a.direccion;
        this.numExterno = a.numExterno;
        this.numInterno = a.numInterno;
        this.colonia = a.colonia;
        this.municipio = a.municipio;
        this.ciudad = a.ciudad;
        this.estado = a.estado;
        this.cp = a.cp;
        this.formateada = a.formateada;
        this.latitud = a.latitud;
        this.longitud = a.longitud;
        this.precision = a.precision;
    }

    public Direccion(Result opcion){

        List<AddressComponent> opciones = opcion.getAddressComponents();
        for (int i=0; i< opciones.size(); i++){
            AddressComponent opcActual = opciones.get(i);
            switch ( (opcActual.getTypes()).get(0) ){
                case "street_number":
                    this.numExterno = opcActual.getLongName();
                    break;

                case "route":
                    this.direccion = opcActual.getLongName();
                    break;

                case "neighborhood": case "locality":
                    this.ciudad = opcActual.getLongName();
                    break;

                case "administrative_area_level_1":
                    this.estado = opcActual.getLongName();
                    break;

                case "administrative_area_level_3":
                    this.municipio = opcActual.getLongName();
                    break;

                case "political":
                    this.colonia = opcActual.getLongName();
                    break;

                case "postal_code":
                    this.cp = opcActual.getLongName();
                    break;
            }
        }

        this.formateada = opcion.getFormattedAddress();
        Geometry geo = opcion.getGeometry();
        this.latitud = (float) (geo.getLocation().getLat());
        this.longitud= (float) (geo.getLocation().getLng());

    }

    public Direccion(Address dir){
        this.direccion = dir.getThoroughfare();
        this.numExterno = dir.getSubThoroughfare();
        this.numInterno = "";
        this.colonia = dir.getSubLocality();
        this.ciudad = dir.getLocality();
        this.municipio = dir.getSubAdminArea();
        this.cp = dir.getPostalCode();
        this.estado = dir.getAdminArea();
        this.formateada = dir.getAddressLine(0);
        this.latitud = (float)dir.getLatitude();
        this.longitud = (float)dir.getLongitude();
        this.precision = 0;
    }

    public String toJson(){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        builder.serializeNulls();
        return gson.toJson(this);
    }

    public Direccion fromJson(String json){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, Direccion.class);
    }

    public String getLatLng(){
        return this.latitud + "," + this.longitud;
    }
}
