package com.staticvillage.marcopolo.model;

/**
 * Created by joelparrish on 1/2/16.
 */
public class DataStruct<T> {
    /**
     * Data name
     */
    private String name;

    /**
     * Data
     */
    private T data;

    public DataStruct(String name, T data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
