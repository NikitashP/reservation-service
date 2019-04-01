package com.loyalty.reservation.service;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class Hotel {

    private static final Logger LOGGER = LoggerFactory.getLogger(Hotel.class);

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
    public boolean reservationIsPossible(int numberOfRoomsNeeded, Long customerBonusPointsBalance){

        if (availableRooms < numberOfRoomsNeeded) {
            LOGGER.error("Available rooms {}, while requested rooms {}",availableRooms,numberOfRoomsNeeded);
            return false;
        }

        if (customerBonusPointsBalance < requiredBonusPoints) {
            LOGGER.error("Customer's available bonus points {}, while required points {}",customerBonusPointsBalance,requiredBonusPoints);
            return false;
        }

        return true;
    }
}
