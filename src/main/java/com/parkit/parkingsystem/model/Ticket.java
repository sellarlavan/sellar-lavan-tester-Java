package com.parkit.parkingsystem.model;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;

import java.util.Calendar;
import java.util.Date;

public class Ticket {
    public static final int TRENTE_MINUTES = 30 * 60 * 1000;
    public static final double DISCOUNT = 0.95;
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private double price;
    private Date inTime;
    private Date outTime;


    private boolean discount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = parkingSpot;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getInTime() {
        return inTime;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public Date getOutTime() {
        return outTime;
    }

    public void setOutTime(Date outTime) {
        this.outTime = outTime;
    }

    public boolean isDiscount() {
        return discount;
    }

    public void setDiscount(boolean discount) {
        this.discount = discount;
    }

    public boolean isInvalid() {
        return outTime == null || outTime.before(inTime);
    }

    public void calculatePrice(double ratePerHour) {
        long inHour = inTime.getTime();
        long outHour = outTime.getTime();
        long durationTime = outHour - inHour;

        if (isLessThan30Minutes(durationTime)) {
            setPrice(0);
        } else {
            double duration = (double) durationTime / (60 * 60 * 1000);
            setPrice(discount ? DISCOUNT * ratePerHour * duration : ratePerHour * duration);
        }

    }

    private boolean isLessThan30Minutes(long durationTime) {
        return durationTime <= TRENTE_MINUTES;
    }
}
