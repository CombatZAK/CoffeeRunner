package com.combatzak.coffeerunner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;

/**
 * Serializable object representing a team member
 */
public class Teammate {
    public static final Comparator<Teammate> defaultComparer = (o1, o2) -> {
        int compareWeight = Double.compare(o1.getWeight(), o2.getWeight());

        if (compareWeight != 0) {
            return compareWeight;
        }

        // counter-intuitively, we want to sort by weight ascending and then last-purchase descending since we're using
        // max(...) to pick who should buy.
        if (o1.getLastPurchase() == null) {
            return 1;
        }

        if (o2.getLastPurchase() == null) {
            return -1;
        }

        return o1.getLastPurchase().compareTo(o2.getLastPurchase()) * -1;
    };

    @JsonProperty("name")
    private String name;

    @JsonProperty("regular_order")
    private DrinkOrder regularOrder;

    @JsonProperty("is_active")
    private boolean isActive;

    @JsonProperty("weight")
    private double weight = 0.0;

    @JsonProperty("last_purchase")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate lastPurchase;

    /**
     * Parameterized constructor setting all serializable values
     * @param name Teammate's name (must be unique)
     * @param regularOrder A DrinkOrder object representing teammates "default" order
     * @param isActive Set to true if this teammate should be part of a "default" coffee run
     * @param lastPurchase Last date the team member purchased drinks
     */
    public Teammate(String name, DrinkOrder regularOrder, boolean isActive, double weight, LocalDate lastPurchase) {
        this.name = name;
        this.regularOrder = regularOrder;
        this.isActive = isActive;
        this.weight = weight;
        this.lastPurchase = lastPurchase;
    }

    public Teammate() {
        this(null, null, true, 0.0, null);
    }

    /**
     * Teammates name, must be unique in the team collection
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Teammates "default" DrinkOrder, used if no special drink is included
     */
    public DrinkOrder getRegularOrder() {
        return regularOrder;
    }

    public void setRegularOrder(DrinkOrder regularOrder) {
        this.regularOrder = regularOrder;
    }

    /**
     * Set to true if the teammate should be part of a "default" order
     */
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Represents the running sum value  of all drinks the teammate has received since they last paid for drinks
     */
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void incrementWeight(double weight) {
        this.weight += weight;
    }

    /**
     * Last date the team member purchased drinks
     */
    public LocalDate getLastPurchase() {
        return lastPurchase;
    }

    public void setLastPurchase(LocalDate lastPurchase) {
        this.lastPurchase = lastPurchase;
    }
}
