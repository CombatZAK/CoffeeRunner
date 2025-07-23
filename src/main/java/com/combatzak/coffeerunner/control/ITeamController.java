package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.util.DuplicateKeyException;

import java.util.Map;

public interface ITeamController {
    /**
     * Loads the team data from storage
     *
     * @throws DuplicateKeyException When the storage context contains two teammates with the same name
     */
    void Load() throws DuplicateKeyException;

    /**
     * Saves the team data to storage
     */
    void Save();

    /**
     * Processes a coffee order for the team
     *
     * @param orderList Drink orders for each teammate, mapped by unique name; these names MUST exist in the loaded team
     *                  context. Null drink orders imply using default drink; an empty or null map implies all ACTIVE
     *                  teammates are ordering their default drink.
     */
    void ProcessOrder(Map<String, DrinkOrder> orderList);
}
