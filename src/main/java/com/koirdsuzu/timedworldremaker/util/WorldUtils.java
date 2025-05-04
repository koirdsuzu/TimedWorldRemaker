package com.koirdsuzu.timedworldremaker.util;

import com.koirdsuzu.timedworldremaker.TimedWorldRemaker;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

public class WorldUtils {

    public static void remakeWorld(String name, String dim, ConfigurationSection method, Plugin plugin) {
        String dimSuffix = switch (dim) {
            case "the_nether" -> "_nether";
            case "the_end" -> "_the_end";
            default -> "";
        };
        String worldName = name + dimSuffix;
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);

        if (plugin.getConfig().getBoolean("backup.enabled", true)) {
            backupWorld(worldFolder, plugin);
        }

        if (Bukkit.getWorld(worldName) != null) {
            Bukkit.unloadWorld(worldName, false);
        }

        if (worldFolder.exists()) {
            deleteDirectory(worldFolder.toPath());
        }

        if ("copy".equals(method.getString("type"))) {
            File template = new File(plugin.getDataFolder(), "templates/" + method.getString("template"));
            copyDirectory(template.toPath(), worldFolder.toPath());
        }

        WorldCreator wc = new WorldCreator(worldName);
        if ("random".equals(method.getString("type"))) {
            if (method.contains("seed") && !method.isString("seed")) {
                wc.seed(method.getLong("seed"));
            }
        }
        if ("the_nether".equals(dim)) wc.environment(World.Environment.NETHER);
        if ("the_end".equals(dim)) wc.environment(World.Environment.THE_END);

        World created = wc.createWorld();

        String startMsg = plugin.getConfig().getString("messages.remake_start", "")
                .replace("{world}", name).replace("{dimension}", dim);
        String doneMsg = plugin.getConfig().getString("messages.remake_done", "")
                .replace("{world}", name).replace("{dimension}", dim);

        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', startMsg)));
        Bukkit.getLogger().info("[TimedWorldRemaker] " + startMsg);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', doneMsg)));
            Bukkit.getLogger().info("[TimedWorldRemaker] " + doneMsg);
        }, 40L);
    }

    private static void backupWorld(File folder, Plugin plugin) {
        try {
            Path backupDir = new File(plugin.getDataFolder(), plugin.getConfig().getString("backup.folder", "backups")).toPath();
            Files.createDirectories(backupDir);

            String fileName = folder.getName() + "_" + System.currentTimeMillis() + ".zip";
            Path zipFile = backupDir.resolve(fileName);

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
                Files.walk(folder.toPath()).filter(p -> !Files.isDirectory(p)).forEach(p -> {
                    try {
                        ZipEntry entry = new ZipEntry(folder.toPath().relativize(p).toString());
                        zos.putNextEntry(entry);
                        Files.copy(p, zos);
                        zos.closeEntry();
                    } catch (IOException ignored) {}
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Backup failed: " + e.getMessage());
        }
    }

    private static void copyDirectory(Path source, Path target) {
        try {
            Files.walk(source).forEach(path -> {
                try {
                    Path dest = target.resolve(source.relativize(path));
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }

    private static void deleteDirectory(Path path) {
        try {
            if (!Files.exists(path)) return;
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
}
