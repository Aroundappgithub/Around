package com.example.nearfriends;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Represents the information of a single contact.
 * Should be able to read/write (get/set)
 *
 * @author Millad Nooristani
 * @version 1.0
 */
public class Contact {
    private String name;
    private OptionalDouble latitude;
    private OptionalDouble longitude;
    private OptionalDouble distance;
    private Optional<String> address;
    private Optional<String> group;

    public Contact(String name, OptionalDouble latitude, OptionalDouble longitude, OptionalDouble distance, Optional<String> address, Optional<String> group) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.address = address;
        this.group = group;
    }

    /**
     * @return group this contact belongs to
     */
    public Optional<String> getGroup() {
        return group;
    }

    /**
     * @param group (friend, client, family, etc)
     */
    public void setGroup(Optional<String> group) {
        this.group = group;
    }

    /**
     * @return contact address
     */
    public Optional<String> getAddress() {
        return address;
    }

    /**
     * @param address
     */
    public void setAddress(Optional<String> address) {
        this.address = address;
    }

    /**
     * @return calculated distance from user
     */
    public OptionalDouble getDistance() {
        return distance;
    }

    /**
     * @param distance in miles
     */
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
     * @return latitude location coordinate
     */
    public OptionalDouble getLatitude() {
        return latitude;
    }

    /**
     * @param latitude location coordinate
     */
    public void setLatitude(OptionalDouble latitude) {
        this.latitude = latitude;
    }

    /**
     * @return longitude location coordinate
     */
    public OptionalDouble getLongitude() {
        return longitude;
    }

    /**
     * @param longitude location coordinate
     */
    public void setLongitude(OptionalDouble longitude) {
        this.longitude = longitude;
    }
}
