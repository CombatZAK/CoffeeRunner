package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports general behavior for loading a team into memory and contains the proposed logic for determining fairly who
 * should buy the current round of drinks.
 */
public abstract class BaseTeamController implements ITeamController {
    /**
     * Deserialized team state to be updated based on the order
     */
    private Map<String, Teammate> team;

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
    public void Load() throws DuplicateKeyException {
        this.team.clear();
        Collection<Teammate> teammates = this.teamStorageContext.FetchTeamMembers();

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
    public void Save() {
        this.teamStorageContext.SaveTeamMembers(this.team.values());
    }

    @Override
    public abstract void ProcessOrder(Map<String, DrinkOrder> orderList);

    public void CheckOrderValid(Map<String, DrinkOrder> orderList) {
        // null or empty collections are valid
        if (orderList == null || orderList.isEmpty()) {
            return;
        }

        if (orderList.size() == 1)  {
            throw new IllegalArgumentException("We are not handling your solo coffee run; buy your own coffee");
        }

        for (Map.Entry<String, DrinkOrder> entry : orderList.entrySet()) {
            if (!this.team.containsKey(entry.getKey())) {
                throw new MissingKeyException(
                        String.format("Team collection does not have a teammate named '%1s'", entry.getKey()),
                        entry.getKey(),
                        this.team);
            }
        }
    }
}
