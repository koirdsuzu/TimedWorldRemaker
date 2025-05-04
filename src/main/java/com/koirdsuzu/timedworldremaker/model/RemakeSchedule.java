package com.koirdsuzu.timedworldremaker.model;

import org.bukkit.configuration.ConfigurationSection;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RemakeSchedule {

    private final String type;
    private final String datetime;
    private final String time;
    private final DayOfWeek dayOfWeek;

    public RemakeSchedule(ConfigurationSection section) {
        this.type = section.getString("type", "once");
        this.datetime = section.getString("datetime", "");
        this.time = section.getString("time", "");
        this.dayOfWeek = section.contains("day_of_week") ?
                DayOfWeek.valueOf(section.getString("day_of_week").toUpperCase()) : null;
    }

    public boolean shouldRun(LocalDateTime now) {
        switch (type) {
            case "once" -> {
                if (datetime.isEmpty()) return false;
                LocalDateTime target = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                return now.getYear() == target.getYear()
                        && now.getMonth() == target.getMonth()
                        && now.getDayOfMonth() == target.getDayOfMonth()
                        && now.getHour() == target.getHour()
                        && now.getMinute() == target.getMinute();
            }
            case "daily" -> {
                if (time.isEmpty()) return false;
                String[] parts = time.split(":");
                return now.getHour() == Integer.parseInt(parts[0])
                        && now.getMinute() == Integer.parseInt(parts[1]);
            }
            case "weekly" -> {
                if (time.isEmpty() || dayOfWeek == null) return false;
                String[] parts = time.split(":");
                return now.getDayOfWeek() == dayOfWeek
                        && now.getHour() == Integer.parseInt(parts[0])
                        && now.getMinute() == Integer.parseInt(parts[1]);
            }
        }
        return false;
    }
}
