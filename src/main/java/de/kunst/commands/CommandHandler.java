package de.kunst.commands;

import org.bukkit.ChatColor;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {
    private final File configFile;
    private final File blockFile;
    private final YamlConfiguration config;
    private final Logger logger;

    private boolean isTNTDisabled;
    private boolean isTNTDamageBeingRecorded;
    private int tntFuseTicks;
    private float tntExplosionRadius;

    public CommandHandler(File configFileIn, File blockFileIn, Logger loggerIn){
        configFile = configFileIn;
        blockFile = blockFileIn;
        config = YamlConfiguration.loadConfiguration(configFile);
        logger = loggerIn;

        isTNTDisabled = config.getBoolean("tnt.isDisabled");
        isTNTDamageBeingRecorded = config.getBoolean("tnt.isTNTDamageBeingRecorded");
        tntFuseTicks = !config.isInt("tnt.fuseTicks") ? 80 : config.getInt("tnt.fuseTicks");
        tntExplosionRadius = !config.isDouble("tnt.yield") ? 4.0F : (float) config.getDouble("tnt.yield");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            switch (command.getName()) {
                case "setDisableTNT":{
                    final boolean isTNTDisabledIn = Boolean.parseBoolean(args[0]);
                    setConfig("tnt.isDisabled", isTNTDisabledIn);
                    isTNTDisabled = isTNTDisabledIn;
                    sender.sendMessage(ChatColor.GOLD + (isTNTDisabledIn ? "TNT is now disabled" : "TNT is now enabled"));
                    return true;
                }

                case "recordTNTDamage":{
                    final boolean recordTNTIn = Boolean.parseBoolean(args[0]);
                    setConfig("tnt.isTNTDamageBeingRecorded", recordTNTIn);
                    isTNTDamageBeingRecorded = recordTNTIn;
                    sender.sendMessage(ChatColor.GOLD + (recordTNTIn ? "TNT damage is now being recorded" : "TNT damage won't be recorded anymore"));
                    return true;
                }

                case "setTNTFuseTicks":{
                    final int fuseTicksIn = Integer.parseInt(args[0]);
                    setConfig("tnt.fuseTicks", fuseTicksIn);
                    tntFuseTicks = fuseTicksIn;
                    sender.sendMessage(ChatColor.GOLD + "TNT fuse ticks is now set to " + fuseTicksIn);
                    return true;
                }

                case "setTNTExplosionRadius":{
                    final float yieldIn = Float.parseFloat(args[0]);
                    tntExplosionRadius = yieldIn;
                    setConfig("tnt.yield", yieldIn);
                    sender.sendMessage(ChatColor.GOLD + "TNT radius is now set to " + yieldIn);
                    return true;
                }

                case "restoreTNTDamage": {
                    if (!isTNTDamageBeingRecorded) {
                        sender.sendMessage(ChatColor.RED + "TNT Damage isn't being recorded. Use /recordTNTDamage to restore destroyed blocks again.");
                        return true;
                    }

                    final String worldName = args.length == 0 ? "world" : args[0];
                    try(final Scanner scanner = new Scanner(blockFile)) {
                        while (scanner.hasNext()) {
                            final BlockData data = sender.getServer().createBlockData(scanner.nextLine());
                            final String[] xyz = scanner.nextLine().split("([ ])"); // x y z
                            //Replaces destroyed blocks
                            Objects.requireNonNull(sender.getServer().getWorld(worldName), worldName)
                                    .getBlockAt(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),Integer.parseInt(xyz[2])).setBlockData(data, true);
                        }
                    } catch (IOException e){
                        logger.warning("Failed to restore TNT damage: " + e.toString());
                        return false;
                    } catch (NullPointerException e){
                        logger.warning("Couldn't find world: " + e.toString());
                        sender.sendMessage(ChatColor.RED + "Couldn't find a world named " + e.toString());
                        return false;
                    }
                    sender.sendMessage("Successfully replaced blocks destroyed by TNT");
                    new FileWriter(blockFile, false).close();
                    return true;
                }

                case "clearBlockFile": {
                    new FileWriter(blockFile, false).close();
                    sender.sendMessage("Block File has successfully been wiped.");
                    return true;
                }

                case "restoreDefaultTNTConfig": {
                    setConfig(new String[]{"tnt.yield", "tnt.fuseTicks", "tnt.isDisabled"}, new Object[]{4.0F, 80, false});
                    tntExplosionRadius = 4.0F;
                    tntFuseTicks = 80;
                    isTNTDisabled = false;
                    sender.sendMessage(ChatColor.GOLD + "Configs have been reset!");
                    return true;
                }

                case "tntConfigs": {
                    sender.sendMessage(String.format("TNTDisabled?: %s Ticks: %d Radius: %f", isTNTDisabled, tntFuseTicks, tntExplosionRadius));
                    return true;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Please provide an argument!");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Could not save changes to Plugin yml!");
            logger.severe("Could save changes to config.yml: " + e.getMessage());
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Please provide a numeric argument!");
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (command.getName()) {
            case "restoreTNTDamage":
                return sender.getServer().getWorlds().stream().map(it -> it.getWorldFolder().getName()).collect(Collectors.toList());

            case "recordTNTDamage":
            case "setDisableTNT":
                return Arrays.asList("true", "false");

            case "setTNTFuseTicks":
                return Collections.singletonList("80");

            case "setTNTExplosionRadius":
                return Collections.singletonList("4.0");
        }
        return Collections.singletonList("");
    }

    public boolean getIsTNTDisabled(){
        return isTNTDisabled;
    }

    public boolean getIsTNTDamageBeingRecorded(){
        return isTNTDamageBeingRecorded;
    }

    public int getTntFuseTicks(){
        return tntFuseTicks;
    }

    public float getTntExplosionRadius() {
        return tntExplosionRadius;
    }

    private void setConfig(String path, Object value) throws IOException {
        config.set(path, value);
        config.save(configFile);
        logger.info(String.format("Successfully set value %s to %s", path, value));
    }

    private void setConfig(String[] path, Object[] value) throws IOException{
        for(int i = 0; i<path.length; i++) setConfig(path[i], value[i]);
    }
}
