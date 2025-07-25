package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import com.combatzak.coffeerunner.util.MissingKeyException;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports general behavior for loading a team into memory and contains the proposed logic for determining fairly who
 * should buy the current round of drinks.
 */
public abstract class BaseTeamController implements ITeamController {
    public static class DateController {
        public static LocalDate getNewDate() {
            return LocalDate.now();
        }
    }

    /**
     * Deserialized team state to be updated based on the order
     */
    protected final Map<String, Teammate> team;

    /**
     * Handler used to pull team data from non-volatile storage and saved back to that same storage
     */
    private final ITeamStorageContext teamStorageContext;

    /**
     * Base constructor
     * @param context Storage handler
     */
    public BaseTeamController(ITeamStorageContext context) {
        this.team = new HashMap<String, Teammate>();

        this.teamStorageContext = context;
    }

    @Override
    public void load() throws DuplicateKeyException {
        this.team.clear();
        Collection<Teammate> teammates = this.teamStorageContext.fetchTeamMembers();

        for (Teammate teammate : teammates) {
            // We need to dedupe the list by name since names have to be unique.
            if (team.containsKey(teammate.getName())) {
                throw new DuplicateKeyException(
                        String.format("Team collection already has a member with name '%1s'", teammate.getName()),
                        "team",
                        this.team);
            }

            this.team.put(teammate.getName(), teammate);
        }
    }

    @Override
    public void save() {
        this.teamStorageContext.saveTeamMembers(this.team.values());
    }

    @Override
    public Teammate processOrder(Map<String, DrinkOrder> orderList) throws MissingKeyException {
        if (orderList == null) {
            orderList = new HashMap<>();
        }

        // Checks that the order is valid and gets it ready for processing if so
        checkOrderValid(orderList);

        return selectPayerForOrder(orderList);
    }

    @Override
    public void addOrUpdateTeammate(Teammate newTeammate) {
        if (newTeammate == null || newTeammate.getName() == null || newTeammate.getName().isBlank()) {
            throw new IllegalArgumentException("Invalid teammate must be initialized with a non-empty name");
        }

        if (newTeammate.getRegularOrder() == null) {
            throw new IllegalArgumentException("Invalid teammmate must have an initialized regular drink order");
        }

        if (this.team.containsKey(newTeammate.getName())) {
            //we want to make sure that we don't edit the purchase date or weight when updating an existing teammate
            Teammate oldTeammate = this.team.get(newTeammate.getName());
            newTeammate.setTotalDrinkCost(oldTeammate.getTotalDrinkCost());;
            newTeammate.setLastPurchase(oldTeammate.getLastPurchase());
        }

        this.team.put(newTeammate.getName(), newTeammate);
    }

    /**
     * Checks whether the specified order is allowed under the rules
     *
     * @param orderList Order containing participating team members and any order overrides
     * @throws MissingKeyException Thrown if a teammate name is specified but not found in storage
     */
    public void checkOrderValid(Map<String, DrinkOrder> orderList) throws MissingKeyException {
        // empty collections are valid, but we inject all active team members here
        if (orderList.isEmpty()) {
            for (Teammate teammate : this.team.values().stream().filter(Teammate::isActive).toList()) {
                // TODO there is probably a better way to do this but this is too foreign to LINQ
                orderList.put(teammate.getName(), teammate.getRegularOrder());
            }
        }

        if (orderList.size() == 1)  {
            throw new IllegalArgumentException("We are not handling your solo coffee run; buy your own coffee");
        }

        for (Map.Entry<String, DrinkOrder> entry : orderList.entrySet()) {
            if (!this.team.containsKey(entry.getKey())) {
                // TODO collection traversal is probably simpler than I'm making it
                throw new MissingKeyException(
                        String.format("Team collection does not have a teammate named '%1s'", entry.getKey()),
                        entry.getKey(),
                        this.team);
            }

            if (entry.getValue() == null) { //set all default orders as needed
                entry.setValue(this.team.get(entry.getKey()).getRegularOrder());
            }
        }
    }

    /**
     * Updates all participating team members weights and selects the buyer of today's order
     *
     * @param orderList List of orders for today
     * @return The buyer of today's order
     */
    public Teammate selectPayerForOrder(Map<String, DrinkOrder> orderList) {
        for (Map.Entry<String, DrinkOrder> order : orderList.entrySet()) {
            Teammate teammate = this.team.get(order.getKey());
            teammate.incrementTotalCost(order.getValue().getPrice());
        }

        Teammate buyer = this.team.values().stream()
                //get all the teammmates with their adjusted weights
                .filter(t -> orderList.containsKey(t.getName()))
                //order descending and pick the highest weight/oldest purchase
                .max(Teammate.defaultComparer).orElseThrow();

        //reset buyer's purchase weight
        buyer.setTotalDrinkCost(0.0);
        // TODO quick and dirty - might need to change this to make testing easier
        buyer.setLastPurchase(DateController.getNewDate());

        return buyer;
    }
}
