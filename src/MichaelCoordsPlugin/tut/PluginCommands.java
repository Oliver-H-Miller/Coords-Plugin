package MichaelCoordsPlugin.tut;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PluginCommands extends JavaPlugin {

    public static boolean sheetCommand(CommandSender sender, Command command, String label, String[] args) {
        TextComponent message = new TextComponent("Click " + ChatColor.UNDERLINE + ChatColor.BOLD + ChatColor.YELLOW + "Here" + ChatColor.RESET + " to spreadsheet of coordinates");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://docs.google.com/spreadsheets/d/1NHLjmH-6B4OBlHohH8cbRWAbHeapjtxnS0HjE8t4j04/view"));
        sender.spigot().sendMessage(message);
        return true;
    }

    public static boolean findCommand(CommandSender sender, Command command, String label, String[] args, LocationStorage locationStorage) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                try {
                    double radius = Integer.parseInt(args[0]);
                    Player player = ((Player) sender);
                    Location location = player.getLocation();
                    AtomicBoolean locationFound = new AtomicBoolean(false);
                    locationStorage.getLocations().forEach((name, MineLocal) -> {
                        if (location.distance(new Location(location.getWorld(), MineLocal.getXcoord(), MineLocal.getYcoord(), MineLocal.getZcoord())) <= radius) {
                            player.sendMessage(MineLocal.getName());
                            locationFound.set(true);
                        }
                    });
                    if (!locationFound.get()) {
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

    public static boolean getCommand(CommandSender sender, Command command, String label, String[] args, LocationStorage locationStorage, Plugin plugin) {
        ArrayList<String> multiwordArgs = getArguments(args);
        int[] coords;
        if (multiwordArgs.size() > 0) {
            for (String arg : multiwordArgs) {
                MinecraftLocation location = locationStorage.getLocationFromName(arg.toLowerCase(), ((Player) sender).getUniqueId());
                if (location != null) {
                    coords = location.getCoords();
                } else {
                    sender.sendMessage(ChatColor.RED + arg + " was not found");
                    return true;
                }
                if(sender.isOp()) {
                    TextComponent message = new TextComponent("The coordinates of " + arg + " are " + ChatColor.DARK_GREEN + ChatColor.BOLD + ChatColor.UNDERLINE + coords[0] + ", " + coords[1] + ", " + coords[2]);
                    if (sender instanceof Player)
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + sender.getName() + " " + coords[0] + " " + coords[1] + " " + coords[2]));
                    sender.spigot().sendMessage(message);
                } else {
                    sender.sendMessage("The coordinates of " + arg + " are " + ChatColor.DARK_GREEN + ChatColor.BOLD + ChatColor.UNDERLINE + coords[0] + ", " + coords[1] + ", " + coords[2]);
                }
                spawnParticleTrail(sender, new Location(((Player) sender).getWorld(), coords[0], coords[1], coords[2]), plugin);
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + " Please enter a location name");
            return true;
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

    public static boolean submitCommand(CommandSender sender, Command command, String label, String[] args, LocationStorage locationStorage) {
        ArrayList<String> multiwordArgs = getArguments(args);
        Vector coords = getSubmitCoords(sender, multiwordArgs);
        sender.sendMessage(multiwordArgs.get(0));
        if (!locationStorage.checkNameExists(multiwordArgs.get(0))) {
            if (coords != null) {
                try {
                    addLocation(sender, multiwordArgs.get(0), coords, locationStorage);
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

    public static boolean mySubmitCommand(CommandSender sender, Command command, String label, String[] args, LocationStorage locationStorage) {
        ArrayList<String> multiwordArgs = getArguments(args);
        Vector coords = getSubmitCoords(sender, multiwordArgs);
        if(!locationStorage.checkNameExists(multiwordArgs.get(0))) {
            if (coords != null) {
                try {
                    SheetCommunication.sendPostRequest(locationToJsonString(multiwordArgs.get(0), coords.getBlockX(), coords.getBlockY(), coords.getBlockZ(), ((Player) sender).getUniqueId().toString()));
                    sender.sendMessage("Added " + multiwordArgs.get(0));
                    String uuid = ((Player) sender).getUniqueId().toString();
                    MinecraftLocation newLocation = new MinecraftLocation(multiwordArgs.get(0), coords.getBlockX(), coords.getBlockY(), coords.getBlockZ());
                    if (locationStorage.getPersonalLocations().containsKey(uuid)) {
                        locationStorage.getPersonalLocations().get(uuid).put(multiwordArgs.get(0).toLowerCase(), newLocation);
                    } else {
                        HashMap<String, MinecraftLocation> newPersonalLocations = new HashMap<>();
                        newPersonalLocations.put(multiwordArgs.get(0).toLowerCase(), newLocation);
                        locationStorage.getPersonalLocations().put(uuid, newPersonalLocations);
                    }
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

    private static ArrayList<String> getArguments(String[] args) {
        ArrayList<String> multiwordArgs = new ArrayList<>();
        if (args.length > 0) {
            boolean lookingForEnd = false;
            String stringSoFar = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].indexOf("\"") == 0) {
                    if (args[i].substring(1).indexOf("\"") == args[i].length() - 2) {
                        multiwordArgs.add(args[i]);
                        continue;
                    }
                    stringSoFar += args[i];
                    lookingForEnd = true;
                    continue;
                }
                if (lookingForEnd) {
                    if (args[i].indexOf("\"") == args[i].length() - 1) {
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

    private static void addLocation(CommandSender sender, String name, Vector coords, LocationStorage locationStorage) throws IOException {
        SheetCommunication.sendPostRequest(locationToJsonString(name, coords.getBlockX(), coords.getBlockY(), coords.getBlockZ()));
        locationStorage.getLocations().put(name.toLowerCase(), new MinecraftLocation(name, coords.getBlockX(), coords.getBlockY(), coords.getBlockZ()));
        locationStorage.getLocationNames().add(name);
    }

    private static String locationToJsonString(String name, int x, int y, int z) {
        name = name.replace("\"", "");
        return "{\"newLocation\":{\"name\":\"" + name + "\",\"xCoord\":" + x + ",\"yCoord\":" + y + ",\"zCoord\":" + z + "}}";
    }

    private static String locationToJsonString(String name, int x, int y, int z, String uuid) {
        name = name.replace("\"", "");
        return "{\"newLocation\":{\"name\":\"" + name + "\",\"xCoord\":" + x + ",\"yCoord\":" + y + ",\"zCoord\":" + z + "},\"uuid\":\"" + uuid + "\"}";
    }

    private static void spawnParticleTrail(CommandSender sender, Location destination, Plugin plugin) {
        final Player player = ((Player) sender);
        final Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(52, 235, 97), 1);
        final Vector direc = destination.clone().subtract(player.getLocation()).toVector().normalize();
        destination.add(0, 1, 0);
        new BukkitRunnable(){
            int t = 0;
            final Location playerLocation = player.getLocation().add(0, 1, 0);
            @Override
            public void run() {
                Location locationCopy = playerLocation.clone();
                for (int i=0; i<15; i++) {
                    if(locationCopy.distance(destination) > 2) {
                        locationCopy.add(direc);
                        player.spawnParticle(Particle.REDSTONE, locationCopy, 1, 0, 0, 0, dust);
                    } else {
                        player.spawnParticle(Particle.REDSTONE, destination, 5, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(235, 58, 52), 1));
                    }
                }
                if(t>25) {
                    this.cancel();
                }
                t++;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

}
