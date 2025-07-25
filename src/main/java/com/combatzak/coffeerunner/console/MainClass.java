package com.combatzak.coffeerunner.console;

import com.combatzak.coffeerunner.control.ITeamController;
import com.combatzak.coffeerunner.control.ITeamStorageContext;
import com.combatzak.coffeerunner.control.RollingAverageTeamController;
import com.combatzak.coffeerunner.model.DrinkOrder;
import com.combatzak.coffeerunner.model.Teammate;
import com.combatzak.coffeerunner.util.DuplicateKeyException;
import com.combatzak.coffeerunner.util.MissingKeyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainClass {
    public enum Verb {
        HELP,
        INIT,
        PUT,
        ORDER
    }

    private static final String border = "**************************************************\n";

    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String helpText = """
USAGE: java -jar coffeerunner.jar VERB OPTS

VERBS:
INIT: Initializes or resets the team.json file.
  OPTS: Json array of Teammates
PUT: Adds a new teammate to the team.json file (or fully updates an existing one)
  OPTS: Single json object representing a teammate
ORDER: Produces an order and updates the team history to reflect it.
  OPTS: None OR json array containing names of attendees and optionally overrides for their drink orders
""";

    protected static ITeamStorageContext getStorageContext() {
        return new JsonTeamStorageContext(getTeamFile().getPath());
    }

    protected static ITeamController getTeamController(ITeamStorageContext storageContext) {
        return new RollingAverageTeamController(storageContext);
    }

    /**
     * Console entry point
     * Usage: java -jar coffeerunner.jar VERB OPTS
     *
     * @param args raw arguments from command line
     */
    public static void main(String[] args) throws MissingKeyException, DuplicateKeyException {
        Verb commandVerb;

        if (args.length == 0) {
            commandVerb = Verb.HELP;
        } else {
            try {
                commandVerb = Verb.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                commandVerb = Verb.HELP;
            }
        }

        String opts = null;
        if (args.length > 1) {
            opts = String.join(" ", Arrays.stream(args).skip(1).toList());
        }

        switch (commandVerb) {
            case HELP:
                System.out.println(helpText);
                return;

            case INIT:
                doInit(opts);
                return;

            case PUT:
                doPut(opts);
                return;

            case ORDER:
                doOrder(opts);
        }
    }

    /**
     * Initializes or resets team.json
     *
     * @param json A JSON array containing the  baseline set of teammates
     */
    protected static void doInit(String json) {
        if (json == null) {
            //for simplicity, we swap null with an empty array
            json = "[]";
        }

        if (!isTeamJsonValid(json)) {
            throw new IllegalArgumentException(String.format("Invalid team JSON: '%1s'", json));
        }

        File teamFile = getTeamFile();
        try (FileWriter writer = new FileWriter(teamFile)) {
            writer.write(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds or updates a teammate in the team file
     *
     * @param json A JSON object representing the teammate
     */
    protected static void doPut(String json) throws DuplicateKeyException {
        Teammate putTeammate = parseTeammate(json);

        ITeamStorageContext storageContext = getStorageContext();
        ITeamController teamController =getTeamController(storageContext);

        teamController.load();
        teamController.addOrUpdateTeammate(putTeammate);
        teamController.save();
    }

    /**
     * Performs decision logic for a coffee order and prints the order to console
     *
     * @param json A JSON object representing orders (can be null)
     */
    protected static void doOrder(String json) throws MissingKeyException, DuplicateKeyException {
        Map<String, DrinkOrder> orderList = parseOrderList(json);

        ITeamStorageContext storageContext = getStorageContext();
        ITeamController teamController = getTeamController(storageContext);

        teamController.load();
        Teammate buyer = teamController.processOrder(orderList);
        teamController.save();

        String orderText = getOrderText(orderList, buyer);
        printOrder(orderText);
    }

    /**
     * Uses Json parser to determine if the JSON argument can be parsed to an array of Teammates
     * @param json JSON argument
     * @return True if the argument will work for init, false otherwise
     */
    protected static boolean isTeamJsonValid(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(json, new TypeReference<List<Teammate>>() {});
        }
        catch (IllegalArgumentException | JsonProcessingException e) {
             return false;
        }

        return true;
    }

    /**
     * Uses Json parser to deserialize teammate json object
     *
     * @param json JSON object representing a teammate
     * @return Deserialized teammate object
     */
    protected static Teammate parseTeammate(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, Teammate.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Failed to parse input JSON to teammate, '%1s'", json), e);
        }
    }

    /**
     * Uses json parser to deserialize order list object
     *
     * @param json JSON object representing a map of teammate names to drink orders
     * @return Deserialized map object
     */
    protected static Map<String, DrinkOrder> parseOrderList(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<Map<String, DrinkOrder>>() { });
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(String.format("Failed to parse input JSON to order list, '%1s'", json), e);
        }
    }

    protected static File getTeamFile() {
        //some hackiness to get the absolute jar path
        String jarPath;
        try {
            jarPath = MainClass.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        File jarFile = new File(jarPath);
        File teamFile = new File(jarFile.getParentFile().getAbsolutePath(), "team.json");

        return teamFile.getAbsoluteFile();
    }

    /**
     * Gets a human-readable list of drink orders
     *
     * @param orderList list of drinks for each teammate on the order
     * @param buyer the teammate buying the order
     * @return human-readable receipt-like order list
     */
    protected static String getOrderText(Map<String, DrinkOrder> orderList, Teammate buyer) {
        StringBuilder builder = new StringBuilder();

        double totalPrice = 0.0;
        for (Map.Entry<String, DrinkOrder> entry : orderList.entrySet()) {
            builder.append(border);
            builder.append(String.format("DRINK ORDER: %1s\n", entry.getValue().getName()));
            if (entry.getValue().getDrinkOptions() != null && entry.getValue().getDrinkOptions().isBlank()) {
                builder.append(String.format("OPTIONS: %1s\n", entry.getValue().getDrinkOptions()));
            }
            builder.append(String.format("\nTEAMMATE NAME: %1s\n", entry.getKey()));
            builder.append(String.format("PRICE: $%1.2f\n", entry.getValue().getPrice()));
            totalPrice += entry.getValue().getPrice();
        }

        builder.append(border);
        builder.append(String.format("ORDER DATE: %1s\n", buyer.getLastPurchase().format(dateFormat)));
        builder.append(String.format("TODAY'S BUYER: %1s\n", buyer.getName()));
        builder.append(String.format("TOTAL COST: $%1.2f\n", totalPrice));
        builder.append(border);

        return builder.toString();
    }

    protected static void printOrder(String orderText) {
        System.out.println(orderText);
    }
}
