package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if(ticket.isInvalid()){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.calculatePrice(Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.calculatePrice(Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }

    }

}