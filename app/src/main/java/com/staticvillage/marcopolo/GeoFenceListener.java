package com.staticvillage.marcopolo;

/**
 * Created by joelparrish on 1/4/16.
 */
public interface GeoFenceListener {
    /**
     * GeoFence trigger event
     * @param id marker id of triggered event
     */
    public void onGeoFence(int id);
}
