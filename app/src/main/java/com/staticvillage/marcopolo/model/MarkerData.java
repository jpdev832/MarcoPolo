package com.staticvillage.marcopolo.model;

import com.orm.SugarRecord;

/**
 * Created by joelparrish.
 */
public class MarkerData extends SugarRecord<MarkerData> {
    /**
     * Associated PointMarker Id
     */
    private long pointMarkerId;

    /**
     * Data
     */
    private String data;

    public long getPointMarkerId() {
        return pointMarkerId;
    }

    public void setPointMarkerId(long pointMarkerId) {
        this.pointMarkerId = pointMarkerId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
