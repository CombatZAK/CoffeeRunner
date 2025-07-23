package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import com.combatzak.coffeerunner.util.MissingKeyException;

import java.util.Map;

public interface ITeamController {
    /**
     * Loads the team data from storage
     *
     * @throws DuplicateKeyException When the storage context contains two teammates with the same name
     */
    void load() throws DuplicateKeyException;

    /**
     * Saves the team data to storage
     */
    void save();

    /**
     * Processes a coffee order for the team
     *
     * @param orderList Drink orders for each teammate, mapped by unique name; these names MUST exist in the loaded team
     *                  context. Null drink orders imply using default drink; an empty or null map implies all ACTIVE
     *                  teammates are ordering their default drink.
     * @return Teammate who will be buying drinks for this order.
     * @exception MissingKeyException Thrown when a name is specified in the orderList which is not found in the team
     */
    Teammate processOrder(Map<String, DrinkOrder> orderList) throws MissingKeyException;

    /**
     * Adds a new teammmate or updates an existing teammate (based on unique key - name)
     *
     * @param newTeammate new teammate information
     */
    void addOrUpdateTeammate(Teammate newTeammate);
}
