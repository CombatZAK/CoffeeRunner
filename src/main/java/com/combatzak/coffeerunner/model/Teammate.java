package com.combatzak.coffeerunner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * Serializable object representing a team member
 */
@JsonIgnoreProperties(value = {"active", "averageDailySpend", "spendCostRatio", "averageOrder"})
public class Teammate {
    public static final Comparator<Teammate> defaultComparer = (o1, o2) -> {
        int compareWeight = Double.compare(o1.getTotalDrinkCost(), o2.getTotalDrinkCost());

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

    public static final Comparator<Teammate> rollingAverageComparaer = (o1, o2) -> {
        if (o1.getSpendCostRatio() == 0.0) {
            return (o2.getSpendCostRatio() == 0.0) ? 0 : -1;
        }
        if (o2.getSpendCostRatio() == 0.0) {
            return (o1.getSpendCostRatio() == 0.0) ? 0 : 1;
        }
        return  Double.compare(o1.getSpendCostRatio(), o2.getSpendCostRatio());
    };

    @JsonProperty(value = "name", index = 0, required = true)
    private String name;

    @JsonProperty(value = "regular_order", index = 1, required = true)
    private DrinkOrder regularOrder;

    @JsonProperty(value = "is_active", index = 2)
    private boolean isActive;

    @JsonProperty(value = "total_drink_cost", index = 3)
    private double totalDrinkCost = 0.0;

    @JsonProperty(value = "days_participated", index = 4)
    private int daysParticipated = 0;

    @JsonProperty(value = "days_paid", index = 5)
    private int daysPaid = 0;

    @JsonProperty(value = "total_paid", index = 6)
    private double totalPaid = 0.0;

    @JsonProperty(value = "last_purchase", index = 7)
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
    public Teammate(String name, DrinkOrder regularOrder, boolean isActive, LocalDate lastPurchase) {
        this.name = name;
        this.regularOrder = regularOrder;
        this.isActive = isActive;
        this.lastPurchase = lastPurchase;
    }

    public Teammate() {
        this(null, null, true, null);
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
     * Total cost of all drinks the teammate has ordered
     */
    public double getTotalDrinkCost() {
        return totalDrinkCost;
    }

    public void setTotalDrinkCost(double totalDrinkCost) {
        this.totalDrinkCost = totalDrinkCost;
    }

    public void incrementTotalCost(double weight) {
        this.totalDrinkCost += weight;
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

    public int getDaysParticipated() {
        return daysParticipated;
    }

    public void setDaysParticipated(int daysParticipated) {
        this.daysParticipated = daysParticipated;
    }

    public void incrementDaysParticipated() {
        ++this.daysParticipated;
    }

    public int getDaysPaid() {
        return daysPaid;
    }

    public void setDaysPaid(int daysPaid) {
        this.daysPaid = daysPaid;
    }

    public void incrementDaysPaid() {
        ++this.daysPaid;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public void incrementTotalPaid(double totalPaid) {
        this.totalPaid += totalPaid;
    }

    public double getAverageDailySpend() {
        if (daysParticipated == 0) {
            return 0;
        }

        return this.totalPaid / (double)this.daysParticipated;
    }

    public double getAverageOrder() {
        if (daysParticipated == 0) {
            return 0;
        }

        return this.totalDrinkCost / (double)this.daysParticipated;
    }

    public double getSpendCostRatio() {
        if (this.totalDrinkCost == 0.0) {
            return 1.0;
        }

        return this.totalPaid / this.totalDrinkCost;
    }
}
