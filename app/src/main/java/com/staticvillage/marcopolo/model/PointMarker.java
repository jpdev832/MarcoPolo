package com.staticvillage.marcopolo.model;

import com.orm.SugarRecord;

/**
 * Created by joelparrish on 12/28/15.
 */
public class PointMarker extends SugarRecord<PointMarker> {
    /**
     * Timestamp marker was uploaded
     */
    private long timestamp;

    /**
     * Marker sequence index
     */
    private int markerIndex;

    /**
     * Center point latitude
     */
    private double latitude;

    /**
     * Center point longitude
     */
    private double longitude;

    /**
     * Data associated with marker
     */
    private String data;

    /**
     * Data type
     */
    private String type;

    /**
     * Marker trigger radius
     */
    private int radius;

    public PointMarker(){}

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getMarkerIndex() {
        return markerIndex;
    }

    public void setMarkerIndex(int markerIndex) {
        this.markerIndex = markerIndex;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
