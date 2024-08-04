package com.bgsoftware.common.reflection;

import com.bgsoftware.common.annotations.Nullable;
import org.bukkit.Bukkit;

public class ClassInfo {

    private final String className;
    private final PackageType packageType;
    private final boolean printErrorOnFailure;
    private Class<?> cachedClass = null;

    public static Class<?>[] findClasses(ClassInfo[] classInfos) {
        Class<?>[] classes = new Class[classInfos.length];
        for (int i = 0; i < classes.length; ++i)
            classes[i] = classInfos[i].findClass();
        return classes;
    }

    public ClassInfo(Class<?> clazz) {
        this("", PackageType.UNKNOWN, false);
        this.cachedClass = clazz;
    }

    public ClassInfo(String className, PackageType packageType) {
        this(className, packageType, false);
    }

    public ClassInfo(String className, PackageType packageType, boolean printErrorOnFailure) {
        this.className = className;
        this.packageType = packageType;
        this.printErrorOnFailure = printErrorOnFailure;
    }

    @Nullable
    public Class<?> findClass() {
        if (this.cachedClass == null)
            this.cachedClass = findClassFromPackageType();

        return this.cachedClass;
    }

    protected Class<?> findClassFromPackageType() {
        try {
            return this.packageType.findClass(this.className);
        } catch (ClassNotFoundException error) {
            if (this.printErrorOnFailure)
                error.printStackTrace();
            return null;
        }
    }

    public enum PackageType {

        NMS {
            private final boolean isLegacyNMSPackageFormat = isLegacyNMSPackageFormat();

            @Override
            Class<?> findClass(String className) throws ClassNotFoundException {
                StringBuilder fullNMSPackage = new StringBuilder("net.minecraft");
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

            @Override
            Class<?> findClass(String className) throws ClassNotFoundException {
                return Class.forName(this.bukkitPackage + "." + className);
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
