package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;

import java.util.Comparator;
import java.util.Map;

public class RollingAverageTeamController extends BaseTeamController {
    public RollingAverageTeamController(ITeamStorageContext teamStorageContext) {
        super(teamStorageContext);
    }

    @Override
    public Teammate selectPayerForOrder(Map<String, DrinkOrder> orderList) {
        Teammate buyer = this.team.values().stream().filter(t -> orderList.containsKey(t.getName()))
                .min(Teammate.rollingAverageComparaer).orElseThrow();

        for (Map.Entry<String, DrinkOrder> entry : orderList.entrySet()) {
            Teammate teammate = this.team.get(entry.getKey());

            teammate.incrementDaysParticipated();
            teammate.incrementTotalCost(entry.getValue().getPrice());

            buyer.incrementTotalPaid(entry.getValue().getPrice());
        }

        buyer.incrementDaysPaid();
        buyer.setLastPurchase(DateController.getNewDate());

        return buyer;
    }
}
