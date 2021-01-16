package MichaelCoordsPlugin.tut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class LocationStorage {
    private static HashMap<String, MinecraftLocation> locations;
    private static ArrayList<String> locationNames;
    private static HashMap<String, HashMap<String, MinecraftLocation>> personalLocations;

    public LocationStorage() {
        locations = new HashMap<>();
        locationNames = new ArrayList<>();
        personalLocations = new HashMap<>();
    }

    public HashMap<String, MinecraftLocation> getLocations() {
        return locations;
    }

    public ArrayList<String> getLocationNames() {
        return locationNames;
    }

    public HashMap<String, HashMap<String, MinecraftLocation>> getPersonalLocations() {
        return personalLocations;
    }

    public boolean checkNameExists(String name) {
        if(locations.containsKey(name))
            return true;
        for(HashMap<String, MinecraftLocation> playerLocations : personalLocations.values()) {
            if(playerLocations.containsKey(name))
                return true;
        }
        return false;
    }

    public MinecraftLocation getLocationFromName(String name) {
        if (locations.containsKey(name)) {
            return locations.get(name);
        }
        return null;
    }

    public MinecraftLocation getLocationFromName(String name, UUID uuid) {
        if (locations.containsKey(name)) {
            return locations.get(name);
        }

        if (personalLocations.containsKey(uuid.toString())) {
            if (personalLocations.get(uuid.toString()).containsKey(name)) {
                return personalLocations.get(uuid.toString()).get(name);
            }
        }
        return null;
    }
}
