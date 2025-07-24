package com.combatzak.coffeerunner.simulation;

import com.combatzak.coffeerunner.console.SimpleTeamController;
import com.combatzak.coffeerunner.control.BaseTeamController;
import com.combatzak.coffeerunner.control.ITeamController;
import com.combatzak.coffeerunner.control.ITeamStorageContext;
import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import com.combatzak.coffeerunner.util.MissingKeyException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class SimulationTest {
    protected static class UserRecord {
        public int daysPaid = 0;
        public double moneyPaid = 0.0;
        public double received = 0.0;
    }

    @Test
    public void testRepeatedOrders() throws DuplicateKeyException, MissingKeyException {
        // FOR
        Map<String, Teammate> testTeam = new HashMap<>();
        testTeam.put("Jim", new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                0.0,
                null
        ));

        testTeam.put("Bob", new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                0.0,
                null
        ));

        testTeam.put("Dustin", new Teammate(
                "Dustin",
                new DrinkOrder("Coffee 20oz", "Espresso shot", 4.75),
                true,
                0.0,
                null
        ));

        testTeam.put("Irinna", new Teammate(
                "Irinna",
                new DrinkOrder("Americano", "2 sugar", 5.60),
                true,
                0.0,
                null
        ));

        testTeam.put("Adrian", new Teammate(
                "Adrian",
                new DrinkOrder("Double espresso", null, 5.25),
                true,
                0.0,
                null
        ));

        testTeam.put("Alex", new Teammate(
                "Alex",
                new DrinkOrder("Mocha", null, 4.0),
                true,
                0.0,
                null
        ));

        testTeam.put("Luke", new Teammate(
                "Luke",
                new DrinkOrder("Mocha", null, 4.0),
                true,
                0.0,
                null
        ));

        ITeamStorageContext mockStorageContext = mock(ITeamStorageContext.class);
        ITeamController teamController = new SimpleTeamController(mockStorageContext);

        LocalDate runningDate = LocalDate.parse("2000-01-01");
        long iteration = 0;

        Map<String, UserRecord> resultData = new HashMap<>();
        for (String name : testTeam.keySet()) {
            resultData.put(name, new UserRecord());
        }

        double orderTotal = 0.0;
        for (Teammate teammate : testTeam.values()) {
            orderTotal += teammate.getRegularOrder().getPrice();
        }

        when(mockStorageContext.fetchTeamMembers()).thenReturn(testTeam.values());
        teamController.load();
        HashMap<String, DrinkOrder> orderList = new HashMap<>();

        try (MockedStatic<BaseTeamController.DateController> staticController = mockStatic(BaseTeamController.DateController.class)) {
            staticController.when(BaseTeamController.DateController::getNewDate).thenReturn(runningDate.plusDays(iteration));

            for (; iteration < 3653; iteration++) {
                Teammate payer = teamController.processOrder(orderList);

                resultData.get(payer.getName()).daysPaid++;
                resultData.get(payer.getName()).moneyPaid += orderTotal;

                for (Teammate teammate : testTeam.values()) {
                    if (teammate == payer) {
                        continue;
                    }

                    resultData.get(teammate.getName()).received += teammate.getRegularOrder().getPrice();
                }
            }
        }

        for (Map.Entry<String, UserRecord> entry : resultData.entrySet()) {
            System.out.printf("NAME: %1s\n", entry.getKey());
            System.out.printf("DAYS PAID: %1s\n", entry.getValue().daysPaid);
            System.out.printf("MONEY PAID: %1.2f\n", entry.getValue().moneyPaid);
            System.out.printf("VALUE RECEIVED: %1.2f\n", entry.getValue().received);

            double averageCost = entry.getValue().moneyPaid / (double)3653;
            System.out.printf("AVG PAID: %1.2f\n\n", averageCost);
        }
    }
}
