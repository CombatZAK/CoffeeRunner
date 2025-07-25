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
}
