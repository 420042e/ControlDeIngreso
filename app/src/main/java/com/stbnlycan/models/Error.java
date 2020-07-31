package com.stbnlycan.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Error implements Serializable {

    @SerializedName("timestamp")
    @Expose
    String timestamp;

    @SerializedName("status")
    @Expose
    int status;

    @SerializedName("error")
    @Expose
    String error;

    @SerializedName("message")
    @Expose
    String message;

    @SerializedName("path")
    @Expose
    String path;

    @SerializedName("trace")
    @Expose
    String trace;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
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

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public Error() {
    }

    public Error(String timestamp, int status, String error, String message, String path, String trace) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.trace = trace;
    }
}
