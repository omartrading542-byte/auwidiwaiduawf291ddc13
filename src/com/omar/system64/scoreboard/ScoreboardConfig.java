package com.omar.system64.scoreboard;

import com.omar.system64.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ScoreboardConfig {

    private static FileConfiguration config;

    // تحميل الإعدادات من scoreboard.yml
    public static void load(Main plugin) {
        File file = new File(plugin.getDataFolder(), "scoreboard.yml");

        if (!file.exists()) {
            try {
                // إذا لم يكن الملف موجودًا، سنقوم بإنشاءه من الموارد الافتراضية
                Main.getInstance().saveResource("scoreboard.yml", false);
                System.out.println("[INFO] scoreboard.yml has been created from resources.");
            } catch (Exception e) {
                System.out.println("[ERROR] Failed to create scoreboard.yml: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // تحميل الإعدادات من الملف
        config = YamlConfiguration.loadConfiguration(file);

        if (config == null) {
            System.out.println("[ERROR] Failed to load scoreboard.yml.");
        }
    }

    // الحصول على عنوان الترويسة من الإعدادات
    public static String getTitle() {
        return config.getString("settings.title", "Default Title");
    }

    // الحصول على أسطر البيانات
    public static List<String> getLines() {
        return config.getStringList("settings.lines");
    }

    // الحصول على لون التاريخ
    public static String getDateColor() {
        return config.getString("settings.date-color", "&7");
    }

    // الحصول على لون الوقت
    public static String getTimeColor() {
        return config.getString("settings.time-color", "&8");
    }

    // الحصول على إطارات الترويسة المتحركة
    public static List<String> getTitleFrames() {
        return config.getStringList("settings.title-frames");
    }

    // الحصول على فترة التحديث بين الإطارات المتحركة
    public static int getTitleAnimationInterval() {
        return config.getInt("settings.title-interval", 20);
    }
}
