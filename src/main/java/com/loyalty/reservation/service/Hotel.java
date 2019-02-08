package com.loyalty.reservation.service;


import lombok.Getter;

@Getter
public class Hotel {

    private String id;

    private long requiredBonusPoints;

    private String name;

    private long availableRooms;

    public Hotel(String id, long requiredBonusPoints, String name, long availableRooms) {
        this.id = id;
        this.requiredBonusPoints = requiredBonusPoints;
        this.name = name;
        this.availableRooms = availableRooms;
    }

    public Hotel(){

    }

}
