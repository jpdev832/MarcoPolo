package com.staticvillage.marcopolo.model;

/**
 * Created by joelparrish on 1/3/16.
 */
public class Response {
    /**
     * Status string representation (e.g. SUCCESS, FAIL)
     */
    private String status;

    /**
     * Status integer representation (e.g. 200, 400)
     */
    private int status_code;

    /**
     * Response message
     */
    private String message;

    /**
     * Data path
     */
    private String path;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
