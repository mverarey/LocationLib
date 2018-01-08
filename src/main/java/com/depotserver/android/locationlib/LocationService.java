package com.depotserver.android.locationlib;

/**
 * Created by mauricio on 11/12/17.
 */

import com.depotserver.android.locationlib.pojo.GoogleMapsGeoCoder;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationService {


    // A request to snap the current user to a place via the Foursquare API.
    @GET("/maps/api/geocode/json")
    Call<GoogleMapsGeoCoder> snapToPlace(@Query("latlng") String latlong,
                                         @Query("latlngAcc") double latlongAcc,
                                         @Query("key") String key );


}
