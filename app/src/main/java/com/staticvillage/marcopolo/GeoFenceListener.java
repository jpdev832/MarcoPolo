package com.staticvillage.marcopolo;

/**
 * Created by joelparrish
 */
public interface GeoFenceListener {
    /**
     * GeoFence trigger event
     * @param id marker id of triggered event
     */
    void onGeoFence(int id);
}
