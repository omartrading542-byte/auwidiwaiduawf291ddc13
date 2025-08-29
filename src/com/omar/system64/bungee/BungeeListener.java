package com.omar.system64.bungee;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeListener implements PluginMessageListener {

    // عدد اللاعبين في الشبكة كلها (ALL) — حتى يشتغل {BUNGEECOUNT} في أي مكان
    public static volatile int bungeeCount = 0;

    // نخزن أعداد كل سيرفر للاستخدام العام إذا احتجته بأي مكان
    public static final Map<String, Integer> GLOBAL_COUNTS = new ConcurrentHashMap<>();

    private final Map<String, Integer> serverCountsRef;

    // نسمح بتمرير مرجع لخريطة خارجية (مثلاً من AnimatedHead)، ولو ما مرّرت، نستخدم GLOBAL_COUNTS
    public BungeeListener(Map<String, Integer> externalMap) {
        this.serverCountsRef = (externalMap != null) ? externalMap : GLOBAL_COUNTS;
    }
    public BungeeListener() {
        this(null);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        if (message == null || message.length == 0) return; // تجاهل الرسائل الفارغة

        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in.readUTF();
            if (subChannel.equals("PlayerCount")) {
                try {
                    String server = in.readUTF();
                    int count = in.readInt();
                    GLOBAL_COUNTS.put(server, count);

                    // ✅ إذا كان السيرفر "ALL" يعني هذا العدد الكلي
                    if (server.equals("ALL")) {
                        bungeeCount = count;
                    }
                } catch (EOFException ignored) {
                    // تجاهل EOFException
                }
            }
        } catch (IOException e) {
            // تجاهل أو اطبع للتصحيح
            e.printStackTrace();
        }
    }

}
