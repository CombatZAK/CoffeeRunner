package com.combatzak.coffeerunner.control;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import com.combatzak.coffeerunner.util.MissingKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RollingAverageTeamControllerTest {
    private ITeamStorageContext teamStorageContext;
    private RollingAverageTeamController controller;

    @BeforeEach
    public void setupTest() {
        teamStorageContext = mock(ITeamStorageContext.class);
        controller = new RollingAverageTeamController(teamStorageContext);
    }

    @Test
    public void testLoad_HappyPath() throws DuplicateKeyException {
        // FOR
        List<Teammate> mockTeamList = new ArrayList<>();
        mockTeamList.add(new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));

        mockTeamList.add(new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                null
        ));
        when(teamStorageContext.fetchTeamMembers()).thenReturn(mockTeamList);

        // WHEN
        controller.load();

        // THEN
        assertEquals(2, controller.team.size());
    }

    @Test
    public void testLoad_EmptyList() throws DuplicateKeyException {
        // FOR
        List<Teammate> mockTeamList = new ArrayList<>();
        when(teamStorageContext.fetchTeamMembers()).thenReturn(mockTeamList);


        // WHEN
        controller.load();

        // THEN
        assertEquals(0, controller.team.size());
    }

    @Test
    public void testLoad_DuplicateNames() {
        // FOR
        List<Teammate> mockTeamList = new ArrayList<>();
        mockTeamList.add(new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));

        mockTeamList.add(new Teammate(
                "Jim",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                null
        ));
        when(teamStorageContext.fetchTeamMembers()).thenReturn(mockTeamList);

        // THEN
        assertThrows(DuplicateKeyException.class, controller::load);
    }

    @Test
    public void testSave_HappyPath() throws DuplicateKeyException {
        // FOR
        List<Teammate> mockTeamList = new ArrayList<>();
        mockTeamList.add(new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));

        mockTeamList.add(new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                null
        ));
        when(teamStorageContext.fetchTeamMembers()).thenReturn(mockTeamList);
        controller.load();

        // WHEN
        controller.save();

        // THEN
        verify(teamStorageContext).saveTeamMembers(argThat(collection -> collection.size() == 2));
    }

    @Test
    public void testProcessOrder_FirstPurchase() throws MissingKeyException {
        // FOR
        controller.team.put("Jim", new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));

        controller.team.put("Bob", new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                null
        ));
        LocalDate today = LocalDate.parse("2025-07-26");

        Map<String, DrinkOrder> orderList = new HashMap<>();

        // WHEN
        Teammate result;
        try (MockedStatic<BaseTeamController.DateController> staticController = mockStatic(BaseTeamController.DateController.class)) {
            staticController.when(BaseTeamController.DateController::getNewDate).thenReturn(today);

            result = controller.processOrder(orderList);
        }

        // THEN
        assertEquals("Bob", result.getName());
        assertEquals(today, result.getLastPurchase());
        assertEquals(2, orderList.size());

        assertEquals(8.0, result.getTotalPaid());
        assertEquals(6.0, result.getTotalDrinkCost());
        assertEquals(1, result.getDaysPaid());
        assertEquals(1, result.getDaysParticipated());

        assertEquals(0.0, controller.team.get("Jim").getTotalPaid());
        assertEquals(2.0, controller.team.get("Jim").getTotalDrinkCost());
        assertEquals(0, controller.team.get("Jim").getDaysPaid());
        assertEquals(1, result.getDaysParticipated());
    }

    @Test
    public void testProcessOrder_NewDay() throws MissingKeyException {
        // FOR
        controller.team.put("Jim", new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));
        controller.team.get("Jim").setDaysParticipated(1);
        controller.team.get("Jim").setTotalDrinkCost(2.0);

        controller.team.put("Bob", new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                LocalDate.parse("2025-07-25")
        ));
        controller.team.get("Bob").setDaysParticipated(1);
        controller.team.get("Bob").setTotalDrinkCost(6.0);
        controller.team.get("Bob").setTotalPaid(8.0);
        controller.team.get("Bob").setDaysPaid(1);

        Map<String, DrinkOrder> orderList = new HashMap<>();

        LocalDate today = LocalDate.parse("2025-07-26");

        // WHEN
        Teammate result;
        try (MockedStatic<BaseTeamController.DateController> staticController = mockStatic(BaseTeamController.DateController.class)) {
            staticController.when(BaseTeamController.DateController::getNewDate).thenReturn(today);

            result = controller.processOrder(orderList);
        }

        // THEN
        assertEquals("Jim", result.getName());
        assertEquals(today, result.getLastPurchase());
        assertEquals(2, orderList.size());

        assertEquals(8.0, result.getTotalPaid());
        assertEquals(1, result.getDaysPaid());
        assertEquals(4.0, result.getTotalDrinkCost());
        assertEquals(2, result.getDaysParticipated());

        assertEquals(8.0, controller.team.get("Bob").getTotalPaid());
        assertEquals(1, controller.team.get("Bob").getDaysPaid());
        assertEquals(12.0, controller.team.get("Bob").getTotalDrinkCost());
        assertEquals(2, controller.team.get("Bob").getDaysParticipated());
    }

    @Test
    public void testProcessOrder_PartialTeam() throws MissingKeyException {
        // FOR
        controller.team.put("Jim", new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                LocalDate.parse("2025-07-26")
        ));
        controller.team.get("Jim").setDaysParticipated(2);
        controller.team.get("Jim").setTotalDrinkCost(4.0);
        controller.team.get("Jim").setTotalPaid(12.0);
        controller.team.get("Jim").setDaysPaid(1);

        controller.team.put("Bob", new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                LocalDate.parse("2025-07-25")
        ));
        controller.team.get("Bob").setDaysParticipated(2);
        controller.team.get("Bob").setTotalDrinkCost(12.0);
        controller.team.get("Bob").setTotalPaid(12.0);
        controller.team.get("Bob").setDaysPaid(1);

        controller.team.put("Luke", new Teammate(
                "Luke",
                new DrinkOrder("Mocha", null, 4.0),
                true,
                null
        ));
        controller.team.get("Luke").setDaysParticipated(2);
        controller.team.get("Luke").setTotalDrinkCost(8.0);

        Map<String, DrinkOrder> orderList = new HashMap<>();
        orderList.put("Jim", null);
        orderList.put("Bob", new DrinkOrder("Coffee 20oz", "decaf", 2.5));

        LocalDate today = LocalDate.parse("2025-07-27");

        // WHEN
        Teammate result;
        try (MockedStatic<BaseTeamController.DateController> staticController = mockStatic(BaseTeamController.DateController.class)) {
            staticController.when(BaseTeamController.DateController::getNewDate).thenReturn(today);

            result = controller.processOrder(orderList);
        }

        // THEN
        assertEquals("Bob", result.getName());
        assertEquals(today, result.getLastPurchase());
        assertEquals(2, orderList.size());

        assertEquals(16.5, result.getTotalPaid());
        assertEquals(2, result.getDaysPaid());
        assertEquals(14.5, result.getTotalDrinkCost());
        assertEquals(3, result.getDaysParticipated());

        assertEquals(12.0, controller.team.get("Jim").getTotalPaid());
        assertEquals(1, controller.team.get("Jim").getDaysPaid());
        assertEquals(6.0, controller.team.get("Jim").getTotalDrinkCost());
        assertEquals(3, controller.team.get("Jim").getDaysParticipated());

        assertEquals(2, controller.team.get("Luke").getDaysParticipated());
    }

    @Test
    public void testProcessOrder_SingleParticipant() {
        // FOR
        controller.team.put("Jim", new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));

        controller.team.put("Bob", new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                null
        ));

        Map<String, DrinkOrder> orderList = new HashMap<>();
        orderList.put("Jim", null);

        // WHEN
        assertThrows(IllegalArgumentException.class, () -> controller.processOrder(orderList));
    }

    @Test
    public void testProcessOrder_InvalidTeammate() {
        // FOR
        controller.team.put("Jim", new Teammate(
                "Jim",
                new DrinkOrder("Coffee 16oz", null, 2.0),
                true,
                null
        ));

        controller.team.put("Bob", new Teammate(
                "Bob",
                new DrinkOrder("Cappuccino", "2% milk", 6.0),
                true,
                null
        ));

        Map<String, DrinkOrder> orderList = new HashMap<>();
        orderList.put("Jim", null);
        orderList.put("Luke", null);

        // WHEN
        assertThrows(MissingKeyException.class, () -> controller.processOrder(orderList));
    }
}
