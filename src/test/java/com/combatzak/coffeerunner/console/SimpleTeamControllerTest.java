package com.combatzak.coffeerunner.console;

import com.combatzak.coffeerunner.control.BaseTeamController;
import com.combatzak.coffeerunner.control.ITeamStorageContext;
import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import com.combatzak.coffeerunner.util.MissingKeyException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleTeamControllerTest {
    /**
     * A test class that allows us to look at the state of the in-memory team
     */
    private static class TestSimpleTeamController extends SimpleTeamController {
        public TestSimpleTeamController(ITeamStorageContext storageContext) {
            super(storageContext);
        }

        public Map<String, Teammate> getTeam() {
            return this.team;
        }
    }

    private Collection<Teammate> validTeam;
    private Collection<Teammate> invalidTeam;

    private ITeamStorageContext teamStorageContext;
    private TestSimpleTeamController controller;

    @BeforeEach
    public void setupTest() {
        teamStorageContext = mock(ITeamStorageContext.class);

        controller = new TestSimpleTeamController(teamStorageContext);

        validTeam = new ArrayList<>();
        validTeam.add(new Teammate("Jim", new DrinkOrder("Coffee 16oz", null, 2.0), true, 2.0, LocalDate.parse("2025-07-20")));
        validTeam.add(new Teammate("Bob", new DrinkOrder("Cappuccino", "2% milk", 6.0), true, 0.0, LocalDate.parse("2025-07-21")));

        invalidTeam = new ArrayList<>();
        invalidTeam.add(new Teammate("Jim", new DrinkOrder("Coffee 16oz", null, 2.0), true, 2.0, LocalDate.parse("2025-07-20")));
        invalidTeam.add(new Teammate("Jim", new DrinkOrder("Coffee 16oz", null, 2.0), true, 2.0, LocalDate.parse("2025-07-20")));
    }

    @Test
    public void testLoad_HappyPath() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);

        // WHEN
        controller.load();

        // THEN
        verify(teamStorageContext).fetchTeamMembers();
    }

    @Test
    public void testLoad_DuplicateKeys() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(invalidTeam);

        // WHEN
        assertThrows(DuplicateKeyException.class, () -> controller.load());

        // THEN
        verify(teamStorageContext).fetchTeamMembers();
    }

    @Test
    public void testSave_HappyPath() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();

        // WHEN
        controller.save();

        // THEN
        verify(teamStorageContext).saveTeamMembers(argThat((collection) -> collection.size() == 2));
    }

    @Test
    public void testProcessOrder_default() throws MissingKeyException, DuplicateKeyException {
        // FOR
        Map<String, DrinkOrder> testOrderList = new HashMap<>();
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();
        LocalDate today = LocalDate.parse("2025-07-23");

        // WHEN
        Teammate result;
        try (MockedStatic<BaseTeamController.DateController> mockedDateController = mockStatic(BaseTeamController.DateController.class)) {
            mockedDateController.when(BaseTeamController.DateController::getNewDate).thenReturn(today);
            result = controller.processOrder(testOrderList);
        }

        // THEN
        assertEquals("Bob", result.getName());
        assertEquals(today, result.getLastPurchase());
        assertEquals(0.0, result.getTotalDrinkCost());

        assertEquals(2, testOrderList.size());
        assertEquals("Coffee 16oz", testOrderList.get("Jim").getName());
        assertEquals("Cappuccino", testOrderList.get("Bob").getName());

        assertEquals(4.0, controller.getTeam().get("Jim").getTotalDrinkCost());
    }

    @Test
    public void testProcessOrder_orderOverride() throws DuplicateKeyException, MissingKeyException {
        // FOR
        Map<String, DrinkOrder> testOrderList = new HashMap<>();
        testOrderList.put("Jim", new DrinkOrder("Mocha", null, 4.5));
        testOrderList.put("Bob", null);
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();
        LocalDate today = LocalDate.parse("2025-07-23");

        // WHEN
        Teammate result;
        try (MockedStatic<BaseTeamController.DateController> mockedDateController = mockStatic(BaseTeamController.DateController.class)) {
            mockedDateController.when(BaseTeamController.DateController::getNewDate).thenReturn(today);
            result = controller.processOrder(testOrderList);
        }

        // THEN
        assertEquals("Jim", result.getName());
        assertEquals(today, result.getLastPurchase());
        assertEquals(0.0, result.getTotalDrinkCost());

        assertEquals(2, testOrderList.size());
        assertEquals("Mocha", testOrderList.get("Jim").getName());
        assertEquals("Cappuccino", testOrderList.get("Bob").getName());

        assertEquals(6.0, controller.getTeam().get("Bob").getTotalDrinkCost());
    }

    @Test
    public void testProcessOrder_groupOverride() throws DuplicateKeyException, MissingKeyException {
        // FOR
        Map<String, DrinkOrder> testOrderList = new HashMap<>();
        testOrderList.put("Jim", null);
        testOrderList.put("Greg", null);

        Collection<Teammate> teamLoader = new ArrayList<>();
        teamLoader.add(new Teammate("Jim", new DrinkOrder("Coffee 16oz", null, 2.0), true, 4.0, LocalDate.parse("2025-07-20")));
        teamLoader.add(new Teammate("Bob", new DrinkOrder("Cappuccino", "2% milk", 6.0), true, 0.0, LocalDate.parse("2025-07-21")));
        teamLoader.add(new Teammate("Greg", new DrinkOrder("Mocha", null, 4.5), true, 9.0, LocalDate.parse("2025-07-19")));

        when(teamStorageContext.fetchTeamMembers()).thenReturn(teamLoader);
        controller.load();

        LocalDate today = LocalDate.parse("2025-07-23");

        // WHEN
        Teammate result;
        try (MockedStatic<BaseTeamController.DateController> mockedDateController = mockStatic(BaseTeamController.DateController.class)) {
            mockedDateController.when(BaseTeamController.DateController::getNewDate).thenReturn(today);
            result = controller.processOrder(testOrderList);
        }

        // THEN
        assertEquals("Greg", result.getName());
        assertEquals(today, result.getLastPurchase());
        assertEquals(0.0, result.getTotalDrinkCost());

        assertEquals(2, testOrderList.size());
        assertEquals("Coffee 16oz", testOrderList.get("Jim").getName());
        assertEquals("Mocha", testOrderList.get("Greg").getName());

        assertEquals(6.0, controller.getTeam().get("Jim").getTotalDrinkCost());
        assertEquals(0.0, controller.getTeam().get("Bob").getTotalDrinkCost());
    }

    @Test
    public void testProcessOrder_singleOrder() throws DuplicateKeyException {
        // FOR
        Map<String, DrinkOrder> testOrderList = new HashMap<>();
        testOrderList.put("Jim", null);

        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();

        // WHEN
        assertThrows(IllegalArgumentException.class, () -> controller.processOrder(testOrderList));
    }

    @Test
    public void testProcessOrder_invalidTeammate() throws DuplicateKeyException {
        // FOR
        Map<String, DrinkOrder> testOrderList = new HashMap<>();
        testOrderList.put("Greg", new DrinkOrder("Mocha", null, 4.5));
        testOrderList.put("Bob", null);

        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();

        // WHEN
        assertThrows(MissingKeyException.class, () -> controller.processOrder(testOrderList));
    }

    @Test
    public void testAddOrUpdateTeammate_newTeammate() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();
        Teammate testTeammate = new Teammate(
                "Greg",
                new DrinkOrder("Mocha", null, 4.5),
                true,
                0.0,
                null);

        // WHEN
        controller.addOrUpdateTeammate(testTeammate);

        // THEN
        assertEquals(3, controller.getTeam().size());
        assertEquals(testTeammate, controller.getTeam().get("Greg"));
    }

    @Test
    public void testAddOrUpdateTeammate_updateTeammate() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();
        Teammate testTeammate = new Teammate(
                "Bob",
                new DrinkOrder("Americano", null, 3.0),
                false,
                0.0,
                null);

        // WHEN
        controller.addOrUpdateTeammate(testTeammate);

        // THEN
        assertEquals(2, controller.getTeam().size());
        assertEquals(testTeammate, controller.getTeam().get("Bob"));
    }

    @Test
    public void testAddOrUpdateTeammate_nullRef() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();

        // WHEN
        assertThrows(IllegalArgumentException.class, () -> controller.addOrUpdateTeammate(null));
    }

    @Test
    public void testAddOrUpdateTeammate_emptyName() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();
        Teammate testTeammate = new Teammate(
                "",
                new DrinkOrder("Americano", null, 3.0),
                false,
                0.0,
                null);


        // WHEN
        assertThrows(IllegalArgumentException.class, () -> controller.addOrUpdateTeammate(testTeammate));
    }

    @Test
    public void testAddOrUpdateTeammate_nullDrink() throws DuplicateKeyException {
        // FOR
        when(teamStorageContext.fetchTeamMembers()).thenReturn(validTeam);
        controller.load();
        Teammate testTeammate = new Teammate(
                "Greg",
                null,
                true,
                0.0,
                null);

        // WHEN
        assertThrows(IllegalArgumentException.class, () -> controller.addOrUpdateTeammate(null));
    }
}
