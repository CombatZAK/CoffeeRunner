package com.combatzak.coffeerunner.console;

import com.combatzak.coffeerunner.control.ITeamController;
import com.combatzak.coffeerunner.control.ITeamStorageContext;
import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MainClassTest {

    @Test
    public void testInit_BlankFile() throws IOException {
        // FOR
        File tempFile = File.createTempFile("coffeerunner", "json");

        // WHEN
        try (MockedStatic<MainClass> mockFileController = mockStatic(MainClass.class)) {
            mockFileController.when(MainClass::getTeamFile).thenReturn(tempFile);
            mockFileController.when(() -> MainClass.isTeamJsonValid(null)).thenCallRealMethod();
            mockFileController.when(() -> MainClass.doInit(null)).thenCallRealMethod();

            MainClass.doInit(null);
        }

        // THEN
        assertEquals("[]", Files.readString(tempFile.toPath()));
    }

    @Test
    public void testInit_2Teammates() throws IOException {
        // FOR
        File tempFile = File.createTempFile("coffeerunner", "json");
        String teamJson = """
[{"name":"Jim","regular_order":{"name":"Coffee 16oz","drink_options":null,"price":2.0},"is_active":true},{"name":"Bob","regular_order":{"name":"Cappuccino","drink_options":"2% milk","price":6.0},"is_active":true}]""";

        // WHEN
        try (MockedStatic<MainClass> mockFileController = mockStatic(MainClass.class)) {
            mockFileController.when(MainClass::getTeamFile).thenReturn(tempFile);
            mockFileController.when(() -> MainClass.isTeamJsonValid(teamJson)).thenCallRealMethod();
            mockFileController.when(() -> MainClass.doInit(teamJson)).thenCallRealMethod();

            MainClass.doInit(teamJson);
        }

        // THEN
        assertEquals(teamJson, Files.readString(tempFile.toPath()));
    }

    @Test
    public void testIsTeamJsonValid_NullRef() {
        // FOR
        String teamJson = null;

        // WHEN
        boolean result = MainClass.isTeamJsonValid(teamJson);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testIsTeamJsonValid_EmptyArray() {
        // FOR
        String teamJson = "[]";

        // WHEN
        boolean result = MainClass.isTeamJsonValid(teamJson);

        assertTrue(result);
    }

    @Test
    public void testIsTeamJsonValid_SingleEntry() {
        // FOR
        String teamJson = """
 [{"name": "Jim", "regular_order": {"name": "Coffee 16oz", "drink_options": null, "price": 2.0}, "is_active": true}]""";

        // WHEN
        boolean result = MainClass.isTeamJsonValid(teamJson);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testIsTeamJsonValid_MultipleEntries() {
        // FOR
        String teamJson = """
 [{"name": "Jim", "regular_order": {"name": "Coffee 16oz", "drink_options": null, "price": 2.0}, "is_active": true},
 {"name": "Bob", "regular_order": {"name": "Cappuccino", "drink_options": "2% milk", "price": 6.0}, "is_active": false}]""";

        // WHEN
        boolean result = MainClass.isTeamJsonValid(teamJson);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testIsTeamJsonValid_InvalidJson() {
        // FOR
        String teamJson = """
 A modest proposal by Jonathan Swift...""";

        // WHEN
        boolean result = MainClass.isTeamJsonValid(teamJson);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testIsTeamJsonValid_ValidNonTeamJson() {
        // FOR
        String teamJson = """
{"launch_site": "Cheyenne", "missile_class": "Minuteman-III", "target": "REDACTED"}""";

        // WHEN
        boolean result = MainClass.isTeamJsonValid(teamJson);

        assertFalse(result);
    }

    @Test
    public void testParseTeammate_ValidTeammate() {
        // FOR
        String teammateJson = """
{"name": "Jim", "regular_order": {"name": "Coffee 16oz", "drink_options": null, "price": 2.0}, "is_active": true}""";

        // WHEN
        Teammate result = MainClass.parseTeammate(teammateJson);

        // THEN
        assertEquals("Jim", result.getName());
        assertEquals("Coffee 16oz", result.getRegularOrder().getName());
        assertNull(result.getRegularOrder().getDrinkOptions());
        assertEquals(2.0, result.getRegularOrder().getPrice());
        assertTrue(result.isActive());
    }

    @Test
    public void testParseTeammate_InvalidJson() {
        // FOR
        String teammateJson = "A modest proposal by Jonathan Swift...";

        // WHEN
        assertThrows(IllegalArgumentException.class, () -> MainClass.parseTeammate(teammateJson));
    }

    @Test
    public void testParseTeammate_ValidNonTeammate() {
        // FOR
        String teammateJson = """
{"name": "Coffee 16oz", "options": null, "price": 2.0}""";

        // WHEN
        assertThrows(IllegalArgumentException.class, () -> MainClass.parseTeammate(teammateJson));
    }

    @Test
    public void testParseOrderList_ValidJson() {
        // FOR
        String orderJson = """
{"Jim": null, "Bob": {"name": "Coffee 20oz", "drink_options": "half-and-half", "price": 2.5} }""";

        // WHEN
        Map<String, DrinkOrder> result = MainClass.parseOrderList(orderJson);

        // THEN
        assertEquals(2, result.size());
        assertNull(result.get("Jim"));
        assertEquals("Coffee 20oz", result.get("Bob").getName());
        assertEquals("half-and-half", result.get("Bob").getDrinkOptions());
        assertEquals(2.5, result.get("Bob").getPrice());
    }

    @Test
    public void testParseOrderList_EmptyOrder() {
        // FOR
        String orderJson = "{}";

        // WHEN
        Map<String, DrinkOrder> result = MainClass.parseOrderList(orderJson);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDoPut() throws DuplicateKeyException {
        // FOR
        String teammateJson = "{}"; // not used for parsing
        ITeamStorageContext mockStorageContext = mock(ITeamStorageContext.class);
        ITeamController mockTeamController = mock(ITeamController.class);
        Teammate mockTeammate = mock(Teammate.class);

        // WHEN
        try (MockedStatic<MainClass> staticController = mockStatic(MainClass.class)) {
            staticController.when(MainClass::getStorageContext).thenReturn(mockStorageContext);
            staticController.when(() -> MainClass.getTeamController(mockStorageContext)).thenReturn(mockTeamController);
            staticController.when(() -> MainClass.doPut("{}")).thenCallRealMethod();
            staticController.when(() -> MainClass.parseTeammate("{}")).thenReturn(mockTeammate);

            MainClass.doPut(teammateJson);
        }

        // THEN
        verify(mockTeamController).load();
        verify(mockTeamController).addOrUpdateTeammate(mockTeammate);
        verify(mockTeamController).save();
    }
}
