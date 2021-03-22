package com.example.nearfriends;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Represents the information of a contact.
 * Should be able to connect with Contacts api and get contact data
 * Should be able to write back and set contact data
 * Should be able to convert city/state to lat/long coordinate of city center
 *
 * @author Millad Nooristani
 * @version 1.0
 */
public class Contact {
    private String name;
    private String city;
    private String state;
    private double latitude;
    private double longitude;
    private OptionalDouble distance;

    public Contact(String name, String city, String state, double latitude, double longitude, OptionalDouble distance) {
        this.name = name;
        this.city = city;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public OptionalDouble getDistance() {
        return distance;
    }

    public void setDistance(OptionalDouble distance) {
        this.distance = distance;
    }

    /**
     * @return contact name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return city name
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return state name
     */
    public String getState() {
        return state;
    }

    /**
     * @param state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return latitude location coordinate
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude location coordinate
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return longitude location coordinate
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude location coordinate
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
