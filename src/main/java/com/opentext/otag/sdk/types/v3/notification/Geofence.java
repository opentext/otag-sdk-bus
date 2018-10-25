package com.opentext.otag.sdk.types.v3.notification;

import java.io.Serializable;

/**
 * Defines a circular boundary using physical coordinates.
 */
public class Geofence implements Serializable {

    private Coordinate origin;

    /**
     * Radius in km.
     */
    private Double radius;

    public Geofence() {
    }

    public Geofence(Coordinate origin, Double radius) {
        this.origin = origin;
        this.radius = radius;
    }

    public Coordinate getOrigin() {
        return origin;
    }

    public void setOrigin(Coordinate origin) {
        this.origin = origin;
    }

    /**
     * Get the radius (from the origin) in km.
     * 
     * @return radius
     */
    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    public boolean hasData() {
        return origin != null && origin.hasData() && radius != null && radius > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Geofence geofence = (Geofence) o;

        if (origin != null ? !origin.equals(geofence.origin) : geofence.origin != null) return false;
        return radius != null ? radius.equals(geofence.radius) : geofence.radius == null;
    }

    @Override
    public int hashCode() {
        int result = origin != null ? origin.hashCode() : 0;
        result = 31 * result + (radius != null ? radius.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Geofence{" +
                "origin=" + origin +
                ", radius=" + radius +
                '}';
    }
    
}
