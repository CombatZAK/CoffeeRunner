package com.combatzak.coffeerunner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Serializable object representing a teammates drink order
 */
public class DrinkOrder {
    @JsonProperty(value = "name", index = 0, required = true)
    private String name;
    @JsonProperty(value = "drink_options", index = 1)
    private String drinkOptions;
    @JsonProperty(value = "price", index = 2, required = true)
    private double price;

    /**
     * Parameterized constructor initializes all serializable properties
     *
     * @param name name of drink (ex, drip coffee)
     * @param drinkOptions additional options for drink (ex, sugar, half-and-half)
     * @param price total price of drink (gratuity and tax included)
     */
    public DrinkOrder(String name, String drinkOptions, double price) {
        this.setName(name);
        this.setDrinkOptions(drinkOptions);
        this.setPrice(price);
    }

    /**
     * Default constructor
     */
    public DrinkOrder() {
        this(null, null, 0.0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDrinkOptions() {
        return drinkOptions;
    }

    public void setDrinkOptions(String drinkOptions) {
        this.drinkOptions = drinkOptions;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
