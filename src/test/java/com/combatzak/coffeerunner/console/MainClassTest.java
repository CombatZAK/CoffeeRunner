package com.combatzak.coffeerunner.console;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
}
