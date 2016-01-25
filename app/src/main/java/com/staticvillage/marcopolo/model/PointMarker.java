package com.staticvillage.marcopolo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by joelparrish
 */
public class PointMarker extends SugarRecord<PointMarker> implements Parcelable {
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
    private List<String> data;

    /**
     * Data type
     */
    private String type;

    /**
     * Marker trigger radius
     */
    private int radius;

    /**
     * Marker message
     */
    private String message;

    public PointMarker(){
        data = new LinkedList<>();
        markerIndex = -1;
    }

    protected PointMarker(Parcel in) {
        timestamp = in.readLong();
        markerIndex = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        data = in.createStringArrayList();
        type = in.readString();
        radius = in.readInt();
        message = in.readString();
    }

    public static final Creator<PointMarker> CREATOR = new Creator<PointMarker>() {
        @Override
        public PointMarker createFromParcel(Parcel in) {
            return new PointMarker(in);
        }

        @Override
        public PointMarker[] newArray(int size) {
            return new PointMarker[size];
        }
    };

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

    public List<String> getData() {
        return data;
    }

    public void addData(String data) {
        this.data.add(data);
    }

    public void setData(List<String> data) {
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

    public String getMessage() {
        if(message == null)
            return "";

        return message;
    }

    public void setMessage(String message) {
        if(message == null)
            message = "";

        this.message = message;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(timestamp);
        out.writeInt(markerIndex);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeStringList(data);
        out.writeString(type);
        out.writeInt(radius);
        out.writeString(message);
    }
}
