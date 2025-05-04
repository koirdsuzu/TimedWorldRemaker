package com.koirdsuzu.timedworldremaker;

import com.koirdsuzu.timedworldremaker.tasks.RemakeTask;
import org.bukkit.plugin.java.JavaPlugin;

public class TimedWorldRemaker extends JavaPlugin {

    private static TimedWorldRemaker instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        new RemakeTask(this).runTaskTimer(this, 20L, 1200L); // 1分ごと
        getLogger().info("TimedWorldRemaker enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TimedWorldRemaker disabled.");
    }

    public static TimedWorldRemaker getInstance() {
        return instance;
    }
}