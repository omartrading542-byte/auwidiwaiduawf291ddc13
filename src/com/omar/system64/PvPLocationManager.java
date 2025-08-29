package com.omar.system64;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PvPLocationManager {
    private final JavaPlugin plugin;
    private final List<PvPArea> pvpAreas;

    public PvPLocationManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pvpAreas = new ArrayList<>();
        loadPvPAreas();
    }

    private void loadPvPAreas() {
        File file = new File(plugin.getDataFolder(), "pvp.yml");

        if (!file.exists()) {
            plugin.saveResource("pvp.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<String> areas = config.getStringList("pvp_areas");

        for (String areaString : areas) {
            String name = config.getString("pvp_areas.name");
            String world = config.getString("pvp_areas.world");
            List<Location> points = new ArrayList<>();

            // قراءة النقاط
            for (String point : config.getStringList("pvp_areas.points")) {
                String[] parts = point.split(",");
                if (parts.length == 3) {
                    double x = Double.parseDouble(parts[0].split(":")[1].trim());
                    double y = Double.parseDouble(parts[1].split(":")[1].trim());
                    double z = Double.parseDouble(parts[2].split(":")[1].trim());
                    points.add(new Location(Bukkit.getWorld(world), x, y, z));
                }
            }
            pvpAreas.add(new PvPArea(name, world, points));
        }
    }

    public boolean isInPvPZone(Location loc) {
        for (PvPArea area : pvpAreas) {
            if (area.getWorld().equals(loc.getWorld())) {
                for (Location point : area.getPoints()) {
                    if (point.distance(loc) <= 5) { // المسافة بين النقاط
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // إنشاء كلاس PvPArea لتخزين المعلومات
    public static class PvPArea {
        private final String name;
        private final String world;
        private final List<Location> points;

        public PvPArea(String name, String world, List<Location> points) {
            this.name = name;
            this.world = world;
            this.points = points;
        }

        public String getName() {
            return name;
        }

        public String getWorld() {
            return world;
        }

        public List<Location> getPoints() {
            return points;
        }
    }
}
