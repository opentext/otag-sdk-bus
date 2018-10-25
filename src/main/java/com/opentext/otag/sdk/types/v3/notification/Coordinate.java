package com.opentext.otag.sdk.types.v3.notification;

import java.io.Serializable;

/**
 * A coordinate representation, 33.757621 -118.111634 for example.
 */
public class Coordinate implements Serializable {

    private Double longitude;

    private Double latitude;

    public Coordinate() {
    }

    public Coordinate(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public boolean hasData() {
        return longitude != null && latitude != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        return latitude != null ? latitude.equals(that.latitude) : that.latitude == null;
    }

    @Override
    public int hashCode() {
        int result = longitude != null ? longitude.hashCode() : 0;
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }

}
