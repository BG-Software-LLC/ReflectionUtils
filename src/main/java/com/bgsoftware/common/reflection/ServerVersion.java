package com.bgsoftware.common.reflection;

import org.bukkit.Bukkit;

class ServerVersion {

    private static final String version = getNMSVersion();

    private static String getNMSVersion() {
        String[] bukkitPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        if (bukkitPackage.length <= 3)
            return null;
        return bukkitPackage[3];
    }

    public static String getVersion() {
        if (version == null)
            throw new RuntimeException("Cannot determine Bukkit version");
        return version;
    }

    private ServerVersion() {

    }

}
