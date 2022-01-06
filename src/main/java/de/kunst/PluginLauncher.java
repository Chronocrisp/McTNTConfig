package de.kunst;

import de.kunst.commands.CommandHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public final class PluginLauncher extends JavaPlugin {
    private final File blockFile = new File(getDataFolder().getAbsolutePath(), "destroyedBlocks.txt");
    private final Logger logger = getLogger();
    private final CommandHandler commandHandler = new CommandHandler(new File(getDataFolder().getAbsolutePath(), "config.yml"), blockFile, logger);

    @Override
    public void onEnable() {
        final String[] commands = {"setDisableTNT", "setTNTFuseTicks", "setTNTExplosionRadius", "restoreTNTDamage", "clearBlockFile", "recordTNTDamage", "restoreDefaultTNTConfig", "tntConfigs"};

        try {
            if(!((blockFile.getParentFile().mkdir() || blockFile.getParentFile().exists()) && (blockFile.createNewFile() || blockFile.exists()))){
                throw new IOException("File does not exist");
            }
        } catch (IOException e) {
            logger.severe("Couldn't create blockFile: " + e.toString());
            getPluginLoader().disablePlugin(this);
        }

        for (String s : commands) {
            final PluginCommand command = getCommand(s);
            if(command == null){
                logger.warning("Unimplemented command: Could not find command " + s);
                continue;
            }
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        }

        getServer().getPluginManager().registerEvents(new Listeners(), this);
    }

    private class Listeners implements Listener {
        @EventHandler
        public void onEvent(EntityExplodeEvent event) {
            if(commandHandler.getIsTNTDisabled() || event.blockList().isEmpty() || !commandHandler.getIsTNTDamageBeingRecorded()) return;
            try (final FileWriter writer = new FileWriter(blockFile, true)) {
                for (Block b : event.blockList()) {
                    if (b.getType() != Material.TNT) {
                        writer.append(b.getBlockData().getAsString());
                        writer.append("\n");
                        writer.append(String.format("%d %d %d", (int) b.getLocation().getX(), (int) b.getLocation().getY(), (int) b.getLocation().getZ()));
                        writer.append("\n");
                    }
                }
            } catch (IOException e){
                logger.warning("DTNT: Failed to parse block data into blockfile: " + e.toString());
            }
        }

        @EventHandler
        public void onEvent(EntitySpawnEvent event) {
            if (event.getEntity() instanceof TNTPrimed) {
                final TNTPrimed tnt = ((TNTPrimed) event.getEntity());
                tnt.setFuseTicks(commandHandler.getTntFuseTicks());
                tnt.setYield(commandHandler.getTntExplosionRadius());
            }
        }

        @EventHandler
        public void onEvent(ExplosionPrimeEvent event) {
            event.setCancelled(commandHandler.getIsTNTDisabled() && event.getEntity() instanceof TNTPrimed);
        }

    }
}
