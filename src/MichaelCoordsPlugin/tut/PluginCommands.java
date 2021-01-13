package MichaelCoordsPlugin.tut;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PluginCommands extends JavaPlugin {

    public static boolean findCommand(CommandSender sender, Command command, String label, String[] args, HashMap<String, MinecraftLocation> locations) {
        if(sender instanceof Player) {
            if(args.length == 1) {
                try {
                    double radius = Integer.parseInt(args[0]);
                    Player player = ((Player) sender);
                    Location location = player.getLocation();
                    AtomicBoolean locationFound = new AtomicBoolean(false);
                    locations.forEach((name, MineLocal) -> {
                        if (location.distance(new Location(location.getWorld(), MineLocal.getXcoord(), MineLocal.getYcoord(), MineLocal.getZcoord())) <= radius) {
                            player.sendMessage(MineLocal.getName());
                            locationFound.set(true);
                        }
                    });
                    if(!locationFound.get()) {
                        player.sendMessage(ChatColor.RED + "No landmark was found within " + (int) radius + " blocks of you");
                    }
                    return true;
                } catch (Exception e) {
                    sender.sendMessage(e.getMessage());
                    return false;
                }
            } else {
                sender.sendMessage("Wrong number of arguments");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "A player must send this message");
            return true;
        }
    }

    public static boolean getCommand(CommandSender sender, Command command, String label, String[] args, HashMap<String, MinecraftLocation> locations) {
        ArrayList<String> multiwordArgs = getArguments(args);
        if (multiwordArgs.size() > 0) {
            for (String arg : multiwordArgs) {
                if (locations.containsKey(arg.toLowerCase())) {
                    int[] coords = locations.get(arg.toLowerCase()).getCoords();
                    TextComponent message = new TextComponent("The coordinates of " + arg + " are " + ChatColor.DARK_GREEN + ChatColor.BOLD + ChatColor.UNDERLINE + coords[0] + ", " + coords[1] + ", " + coords[2]);
                    if (sender instanceof Player)
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + sender.getName() + " " + coords[0] + " " + coords[1] + " " + coords[2]));
                    sender.spigot().sendMessage(message);
                } else {
                    sender.sendMessage(ChatColor.RED + arg + " was not found");
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + " Please enter a location name");
        }
        return true;
    }

    private static Vector getSubmitCoords(CommandSender sender, ArrayList<String> multiwordArgs) {
        try {
            int xCoord;
            int yCoord;
            int zCoord;
            if (multiwordArgs.size() == 1 && sender instanceof Player) {
                Location playerLocation = ((Player) sender).getLocation();
                return playerLocation.toVector();
            } else if (multiwordArgs.size() == 4 && sender instanceof Player) {
                xCoord = Integer.parseInt(multiwordArgs.get(1));
                yCoord = Integer.parseInt(multiwordArgs.get(2));
                zCoord = Integer.parseInt(multiwordArgs.get(3));
                return new Vector(xCoord, yCoord, zCoord);
            } else {
                sender.sendMessage(ChatColor.RED + " Please enter a name followed by 3 integers (no decimals");
                return null;
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + " Please enter a name followed by 3 integers (no decimals");
            return null;
        }
    }

    public static boolean submitCommand(CommandSender sender, Command command, String label, String[] args, HashMap<String, MinecraftLocation> locations, List<String> locationNames) {
        ArrayList<String> multiwordArgs = getArguments(args);
        Vector coords = getSubmitCoords(sender, multiwordArgs);
        sender.sendMessage(multiwordArgs.get(0));
        if(!locations.containsKey(multiwordArgs.get(0))) {
            if (coords != null) {
                try {
                    addLocation(locations, locationNames, sender, multiwordArgs.get(0), coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
                    sender.sendMessage("Added " + multiwordArgs.get(0));
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + " Please enter a name followed by 3 integers (no decimals");
                return false;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Sorry, that name has already been taken");
            return true;
        }
    }

    public static boolean mySubmitCommand(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> multiwordArgs = getArguments(args);
        Vector coords = getSubmitCoords(sender, multiwordArgs);
        if(coords != null) {
            try {
                SheetCommunication.sendPostRequest(locationToJsonString(multiwordArgs.get(0), coords.getBlockX(), coords.getBlockY(), coords.getBlockZ(), ((Player) sender).getUniqueId().toString()));
            } catch (IOException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " Please enter a name followed by 3 integers (no decimals");
            return false;
        }
    }

    private static ArrayList<String> getArguments(String[] args) {
        ArrayList<String> multiwordArgs = new ArrayList<>();
        if (args.length > 0) {
            boolean lookingForEnd = false;
            String stringSoFar = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].indexOf("\"") == 0) {
                    if (args[i].substring(1).indexOf("\"") == args[i].length() - 2) {
//                        multiwordArgs.add(args[i].substring(1, args[i].length() - 1));
                        multiwordArgs.add(args[i]);
                        continue;
                    }
//                    stringSoFar += args[i].substring(1);
                    stringSoFar += args[i];
                    lookingForEnd = true;
                    continue;
                }
                if (lookingForEnd) {
                    if (args[i].indexOf("\"") == args[i].length() - 1) {
//                        stringSoFar += " " + args[i].substring(0, args[i].length() - 1);
                        stringSoFar += " " + args[i];
                        lookingForEnd = false;
                        multiwordArgs.add(stringSoFar);
                        stringSoFar = "";
                    } else {
                        stringSoFar += " " + args[i];
                    }
                } else {
                    multiwordArgs.add(args[i]);
                }
            }
        }
        return multiwordArgs;
    }

    private static void addLocation(HashMap<String, MinecraftLocation> locations, List<String> locationNames, CommandSender sender, String name, int xCoord, int yCoord, int zCoord) throws IOException {
        sender.sendMessage(locations.keySet().toString());
        SheetCommunication.sendPostRequest(locationToJsonString(name.replace("\"", ""), xCoord, yCoord, zCoord));
        locations.put(name.toLowerCase(), new MinecraftLocation(name, xCoord, yCoord, zCoord));
        locationNames.add(name);
//            locationNames.add("\"" + name + "\"");
    }

    private static String locationToJsonString(String name, int x, int y, int z) {
        return "{\"newLocation\":{\"name\":\"" + name + "\",\"xCoord\":" + x + ",\"yCoord\":" + y + ",\"zCoord\":" + z + "}}";
    }

    private static String locationToJsonString(String name, int x, int y, int z, String uuid) {
        return "{\"newLocation\":{\"name\":\"" + name + "\",\"xCoord\":" + x + ",\"yCoord\":" + y + ",\"zCoord\":" + z + "},\"uuid\":\""+ uuid + "\"}";
    }
}
