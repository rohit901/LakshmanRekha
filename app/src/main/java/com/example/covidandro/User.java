package com.example.covidandro;

import java.util.List;

public class User {

    public List<Double> Location;
    public String regImage;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(List<Double> location, String regImage) {
        this.Location = location;
        this.regImage = regImage;

    }

}