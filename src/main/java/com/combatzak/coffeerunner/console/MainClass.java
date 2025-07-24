package com.combatzak.coffeerunner.console;

import java.io.Console;
import java.net.URISyntaxException;

public class MainClass {
    public static class CommandParser {
        public static enum Verb {
            HELP,
            INIT,
            PUT,
            ORDER
        }

        private final Verb verb;

        public CommandParser(String[] arguments) {
            if (arguments.length > 0) {
                verb = Verb.valueOf(arguments[0]);
            }
            else {
                verb = Verb.HELP;
            }
            System.out.printf("VERB DETECTED: %1s%n", verb);
        }

        public boolean isValid() {
            return false;
        }
    }

    public static class CommandHandler {

    }

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

    /**
     * Console entry point
     * Usage: java -jar coffeerunner.jar VERB OPTS
     *
     * @param args raw arguments from command line
     */
    public static void main(String[] args) {
        String jarDir;
        try {
            jarDir = MainClass.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        System.out.printf("Executable directory: %1s\n", jarDir);

        CommandParser parser = new CommandParser(args);

        if (!parser.isValid()) {
            System.out.println(helpText);
            return;
        }
    }
}
