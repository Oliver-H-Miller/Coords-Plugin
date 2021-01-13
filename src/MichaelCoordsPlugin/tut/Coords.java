package MichaelCoordsPlugin.tut;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;


public class Coords extends JavaPlugin implements TabCompleter, Listener {

    private static final HashMap<String, MinecraftLocation> locations = new HashMap<>();
    private static final ArrayList<String> locationNames = new ArrayList<>();
    private static final HashMap<String, HashSet<MinecraftLocation>> personalLocations = new HashMap<>();

    @Override
    public void onEnable() {
        JsonObject locationData = new JsonParser().parse(SheetCommunication.getContents()).getAsJsonObject();
        JsonArray locationArray = locationData.getAsJsonArray("locations");
        for (int i = 0; i < locationArray.size(); i++) {
            MinecraftLocation location = jsonToMinecraftLocation(locationArray.get(i).getAsJsonObject());
            locations.put(location.getName().toLowerCase(), location);
            locationNames.add(location.getName());
        }
        for (Player player: Bukkit.getServer().getOnlinePlayers()) {
            getLocationsFromPlayer(player);
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer();
        getLocationsFromPlayer(player);
    }

    private void getLocationsFromPlayer(Player player) {
        getLogger().info(SheetCommunication.getContents(player.getUniqueId().toString()));
        JsonObject personalLocationData = new JsonParser().parse(SheetCommunication.getContents(player.getUniqueId().toString())).getAsJsonObject();
        JsonArray personalLocationArray = personalLocationData.getAsJsonArray("locations");
        HashSet<MinecraftLocation> minecraftLocations = new HashSet<>();
        for(int i=0; i<personalLocationArray.size(); i++) {
            minecraftLocations.add(jsonToMinecraftLocation(personalLocationArray.get(i).getAsJsonObject()));
        }
        personalLocations.put(player.getUniqueId().toString(), minecraftLocations);
    }

    @Override
    public void onDisable() {
        getLogger().info("Coords Plugin has been disabled");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("get")) {
            return locationNames;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "get":
                return PluginCommands.getCommand(sender, command, label, args, locations);
            case "submit":
                getLogger().info(Arrays.toString(args));
                return PluginCommands.submitCommand(sender, command, label, args, locations, locationNames);
            case "find":
                return PluginCommands.findCommand(sender, command, label, args, locations);
            //noinspection SpellCheckingInspection
            case "mysubmit":
                return PluginCommands.mySubmitCommand(sender, command, label, args);
            default:
                return false;
        }
    }

    private MinecraftLocation jsonToMinecraftLocation(JsonObject location) {
        String name = location.get("name").toString();
        int xCoord = location.get("x-coord").getAsInt();
        int yCoord = location.get("y-coord").getAsInt();
        int zCoord = location.get("z-coord").getAsInt();
        return new MinecraftLocation(name, xCoord, yCoord, zCoord);
    }
}