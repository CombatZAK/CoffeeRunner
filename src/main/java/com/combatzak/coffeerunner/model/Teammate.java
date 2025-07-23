package com.combatzak.coffeerunner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Serializable object representing a team member
 */
public class Teammate {
    /**
     * Teammates name, must be unique in the team collection
     */
    @JsonProperty("name")
    private String name;

    /**
     * Teammates "default" DrinkOrder, used if no special drink is included
     */
    @JsonProperty("regular_order")
    private DrinkOrder regularOrder;

    /**
     * Set to true if the teammate should be part of a "default" order
     */
    @JsonProperty("is_active")
    private boolean isActive;

    /**
     * Represents the running sum value  of all drinks the teammate has received since they last paid for drinks
     */
    @JsonProperty("weight")
    private double weight = 0.0;

    /**
     * Parameterized constructor setting all serializable values
     * @param name Teammate's name (must be unique)
     * @param regularOrder A DrinkOrder object representing teammates "default" order
     * @param isActive Set to true if this teammate should be part of a "default" coffee run
     */
    public Teammate(String name, DrinkOrder regularOrder, boolean isActive) {
        this.setName(name);
        this.setRegularOrder(regularOrder);
        this.setActive(isActive);
    }

    public Teammate() {
        this(null, null, true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DrinkOrder getRegularOrder() {
        return regularOrder;
    }

    public void setRegularOrder(DrinkOrder regularOrder) {
        this.regularOrder = regularOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void incrementWeight(double weight) {
        this.weight += weight;
    }
}
