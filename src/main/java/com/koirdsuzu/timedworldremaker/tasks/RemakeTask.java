package com.koirdsuzu.timedworldremaker.tasks;

import com.koirdsuzu.timedworldremaker.TimedWorldRemaker;
import com.koirdsuzu.timedworldremaker.util.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.ConfigurationSection;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RemakeTask extends BukkitRunnable {

    private final TimedWorldRemaker plugin;

    public RemakeTask(TimedWorldRemaker plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        List<?> list = plugin.getConfig().getList("remake");
        if (list == null) return;

        for (Object item : list) {
            if (!(item instanceof ConfigurationSection section)) continue;

            String worldName = section.getString("world");
            String dimension = section.getString("dimension", "overworld");
            ConfigurationSection schedule = section.getConfigurationSection("schedule");
            ConfigurationSection method = section.getConfigurationSection("method");

            if (!shouldRun(now, schedule)) continue;

            Bukkit.getScheduler().runTask(plugin, () -> {
                WorldUtils.remakeWorld(worldName, dimension, method, plugin);
            });
        }
    }

    private boolean shouldRun(LocalDateTime now, ConfigurationSection schedule) {
        String type = schedule.getString("type");
        switch (type) {
            case "once" -> {
                String datetime = schedule.getString("datetime");
                if (datetime == null) return false;
                LocalDateTime target = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return now.getYear() == target.getYear()
                        && now.getMonth() == target.getMonth()
                        && now.getDayOfMonth() == target.getDayOfMonth()
                        && now.getHour() == target.getHour()
                        && now.getMinute() == target.getMinute();
            }
            case "daily" -> {
                String time = schedule.getString("time");
                String[] parts = time.split(":");
                return now.getHour() == Integer.parseInt(parts[0])
                        && now.getMinute() == Integer.parseInt(parts[1]);
            }
            case "weekly" -> {
                String day = schedule.getString("day_of_week").toUpperCase();
                String time = schedule.getString("time");
                String[] parts = time.split(":");
                return now.getDayOfWeek() == DayOfWeek.valueOf(day)
                        && now.getHour() == Integer.parseInt(parts[0])
                        && now.getMinute() == Integer.parseInt(parts[1]);
            }
        }
        return false;
    }
}
