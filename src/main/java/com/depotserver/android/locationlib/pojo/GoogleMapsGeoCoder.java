
package com.depotserver.android.locationlib.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GoogleMapsGeoCoder {

    @SerializedName("results")
    @Expose
    private List<Result> results = null;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("error_message")
    @Expose
    private String error_message;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public GoogleMapsGeoCoder withResults(List<Result> results) {
        this.results = results;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GoogleMapsGeoCoder withStatus(String status) {
        this.status = status;
        return this;
    }

}
