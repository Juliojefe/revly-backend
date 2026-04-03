package com.example.revly.dto.request;

public class UpdateBusinessLocationRequest {
    private String address;
    private Double lat;
    private Double lon;

    public UpdateBusinessLocationRequest() {
    }

    public UpdateBusinessLocationRequest(String address, Double lat, Double lon) {
        this.address = address;
        this.lat = lat;
        this.lon = lon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
