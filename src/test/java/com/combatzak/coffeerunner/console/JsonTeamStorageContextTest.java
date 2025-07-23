package com.combatzak.coffeerunner.console;

import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

public class JsonTeamStorageContextTest {

    public static List<Teammate> testCollection;

    @BeforeAll
    public static void setUp() {
        testCollection = new ArrayList<>();
        testCollection.add(new Teammate("Jim", new DrinkOrder("Coffee 16oz", null, 2.0), true, 2.0, LocalDate.parse("2025-07-20")));
        testCollection.add(new Teammate("Bob", new DrinkOrder("Cappuccino", "2% milk", 6.0), true, 0.0, LocalDate.parse("2025-07-21")));
    }

    @Test
    public void testFetchTeamMembers() throws IOException {
        // FOR
        ObjectMapper mapper = new ObjectMapper();
        File tempFile = File.createTempFile("coffeeTest", "json");
        mapper.writeValue(tempFile, testCollection);

        JsonTeamStorageContext testContext = new JsonTeamStorageContext(tempFile.getPath());

        // WHEN
        Collection<Teammate> result = testContext.fetchTeamMembers();
        List<Teammate> resultList = result.stream().toList();

        // THEN
        Assertions.assertEquals(2, result.size(), "Deserialized team expected to have exactly 2 elements");

        Assertions.assertEquals(testCollection.getFirst().getName(), resultList.getFirst().getName());
        Assertions.assertEquals(testCollection.getFirst().getRegularOrder().getName(), resultList.getFirst().getRegularOrder().getName());
        Assertions.assertEquals(testCollection.getFirst().getRegularOrder().getDrinkOptions(), resultList.getFirst().getRegularOrder().getDrinkOptions());
        Assertions.assertEquals(testCollection.getFirst().getRegularOrder().getPrice(), resultList.getFirst().getRegularOrder().getPrice());
        Assertions.assertEquals(testCollection.getFirst().isActive(), resultList.getFirst().isActive());
        Assertions.assertEquals(testCollection.getFirst().getWeight(), resultList.getFirst().getWeight());
        Assertions.assertEquals(testCollection.getFirst().getLastPurchase(), resultList.getFirst().getLastPurchase());

        Assertions.assertEquals(testCollection.getLast().getName(), resultList.getLast().getName());
        Assertions.assertEquals(testCollection.getLast().getRegularOrder().getName(), resultList.getLast().getRegularOrder().getName());
        Assertions.assertEquals(testCollection.getLast().getRegularOrder().getDrinkOptions(), resultList.getLast().getRegularOrder().getDrinkOptions());
        Assertions.assertEquals(testCollection.getLast().getRegularOrder().getPrice(), resultList.getLast().getRegularOrder().getPrice());
        Assertions.assertEquals(testCollection.getLast().isActive(), resultList.getLast().isActive());
        Assertions.assertEquals(testCollection.getLast().getWeight(), resultList.getLast().getWeight());
        Assertions.assertEquals(testCollection.getLast().getLastPurchase(), resultList.getLast().getLastPurchase());
    }

    @Test
    public void saveTeamMembers() throws IOException {
        // FOR
        File tempFile = File.createTempFile("coffeeTest", "json");

        JsonTeamStorageContext testContext = new JsonTeamStorageContext(tempFile.getPath());

        // WHEN
        testContext.saveTeamMembers(testCollection);

        // THEN
        String expected = """
[{"active":true,"name":"Jim","regular_order":{"name":"Coffee 16oz","drink_options":null,"price":2.0},"is_active":true,"weight":2.0,"last_purchase":"2025-07-20"},{"active":true,"name":"Bob","regular_order":{"name":"Cappuccino","drink_options":"2% milk","price":6.0},"is_active":true,"weight":0.0,"last_purchase":"2025-07-21"}]""";
        Assertions.assertEquals(expected, Files.readString(tempFile.toPath()));

    }
}
