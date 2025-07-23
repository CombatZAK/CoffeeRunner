package com.combatzak.coffeerunner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Serializable object representing a teammates drink order
 */
public class DrinkOrder {
    @JsonProperty("name")
    private String name;
    @JsonProperty("drink_options")
    private String drinkOptions;
    @JsonProperty("price")
    private double price;

    /**
     * Parameterized contructor initializes all serializable properties
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
