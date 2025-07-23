package com.combatzak.coffeerunner.console;

import com.combatzak.coffeerunner.control.BaseTeamController;
import com.combatzak.coffeerunner.control.ITeamStorageContext;
import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.MissingKeyException;

import java.util.HashMap;
import java.util.Map;

/**
 * A very basic handler for managing team drink orders
 */
public class SimpleTeamController extends BaseTeamController {
    public SimpleTeamController(ITeamStorageContext teamStorageContext) {
        super(teamStorageContext);
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
}
