package com.combatzak.coffeerunner.console;

import com.combatzak.coffeerunner.control.ITeamStorageContext;
import com.combatzak.coffeerunner.model.Teammate;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * A very basic handler for tracking a team using a JSON file. NOT THREAD SAFE buyer beware
 */
public class JsonTeamStorageContext implements ITeamStorageContext {
    private final String filePath;

    /**
     * Creates a new JsonTeamStorageContext instance
     *
     * @param filePath Absolute path to json file
     */
    public JsonTeamStorageContext(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Absolute path to team JSON file
     */
    public String getFilePath() {
        return filePath;
    }

    @Override
    public Collection<Teammate> fetchTeamMembers() {
        ObjectMapper mapper = new ObjectMapper();
        File fileHandle = new File(this.filePath);

        try {
            return mapper.readValue(fileHandle, new TypeReference<List<Teammate>>() { });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveTeamMembers(Collection<Teammate> team) {
        ObjectMapper mapper = new ObjectMapper();
        File fileHandle = new File(this.filePath);

        try {
            mapper.writeValue(fileHandle, team);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
