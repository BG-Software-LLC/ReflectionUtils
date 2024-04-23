package com.bgsoftware.common.reflection;

import com.bgsoftware.common.annotations.Nullable;
import org.bukkit.Bukkit;

public class ClassInfo {

    private final String className;
    private final PackageType packageType;

    public static Class<?>[] findClasses(ClassInfo[] classInfos) {
        Class<?>[] classes = new Class[classInfos.length];
        for (int i = 0; i < classes.length; ++i)
            classes[i] = classInfos[i].findClass();
        return classes;
    }

    public ClassInfo(String className, PackageType packageType) {
        this.className = className;
        this.packageType = packageType;
    }

    @Nullable
    public Class<?> findClass() {
        try {
            return this.packageType.findClass(this.className);
        } catch (ClassNotFoundException error) {
            error.printStackTrace();
            return null;
        }
    }

    public enum PackageType {

        NMS {
            private final boolean isLegacyNMSPackageFormat = isLegacyNMSPackageFormat();

            @Override
            Class<?> findClass(String className) throws ClassNotFoundException {
                StringBuilder fullNMSPackage = new StringBuilder("net.minecraft.");
                if (this.isLegacyNMSPackageFormat) {
                    // Legacy format adds `server` and the version to the class name.
                    // Non legacy are required to provide the subpackage name as well.
                    fullNMSPackage.append(".server.").append(ServerVersion.getVersion());
                }
                fullNMSPackage.append(".").append(className);
                return Class.forName(fullNMSPackage.toString());
            }

            private boolean isLegacyNMSPackageFormat() {
                // Check for legacy format: net.minecraft.server.<VERSION>.<CLASS>
                try {
                    String version = ServerVersion.getVersion();
                    Class.forName("net.minecraft.server." + version + ".WorldServer");
                    return true;
                } catch (Exception error) {
                    return false;
                }
            }

        },

        CRAFTBUKKIT {
            private final String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
            private final boolean hasVersionInPackage = this.bukkitPackage.split("\\.").length > 3;

            @Override
            Class<?> findClass(String className) throws ClassNotFoundException {
                StringBuilder fullBukkitPackage = new StringBuilder(this.bukkitPackage);
                if (this.hasVersionInPackage) {
                    fullBukkitPackage.append(".").append(ServerVersion.getVersion());
                }
                fullBukkitPackage.append(".").append(className);
                return Class.forName(fullBukkitPackage.toString());
            }
        },

        UNKNOWN {
            @Override
            Class<?> findClass(String className) throws ClassNotFoundException {
                return Class.forName(className);
            }
        };

        abstract Class<?> findClass(String className) throws ClassNotFoundException;

    }

}