package com.example.nearfriends;

/**
 * Represents the information of a contact.
 * Should be able to connect with Contacts api and get contact data
 * Should be able to write back and set contact data
 * Should be able to convert city/state to lat/long coordinate of city center
 * @author Millad Nooristani
 * @version 1.0
 */
public class Contact {
    private String city;
    private String state;
    private double latitude;
    private double longitude;

    public Contact(String city, String state, double latitude, double longitude){
        this.city = city;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @return city name
     */
    public String getCity(){
        return city;
    }

    /**
     * @param city
     */
    public void setCity(String city){
        this.city = city;
    }

    /**
     * @return state name
     */
    public String getState(){
        return state;
    }

    /**
     * @param state
     */
    public void setState(String state){
        this.state = state;
    }

    /**
     * @return latitude location coordinate
     */
    public double getLatitude(){
        return latitude;
    }

    /**
     * @param latitude location coordinate
     */
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    /**
     * @return longitude location coordinate
     */
    public double getLongitude(){
        return longitude;
    }

    /**
     * @param longitude location coordinate
     */
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
}
