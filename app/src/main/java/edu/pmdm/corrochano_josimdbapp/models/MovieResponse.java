package edu.pmdm.corrochano_josimdbapp.models;

public class MovieResponse {
    private Movie data;
    private boolean status;
    private String message;

    public Movie getData() {
        return data;
    }

    public void setData(Movie data) {
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

