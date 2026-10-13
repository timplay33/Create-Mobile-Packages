package de.theidler.create_mobile_packages.compat.sable;

import de.theidler.create_mobile_packages.CreateMobilePackages;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SableCompat {
    private static final String SABLE_MOD_ID = "sable";
    private static final String SABLE_CLASS = "dev.ryanhcode.sable.Sable";
    private static final String PROJECT_METHOD = "projectOutOfSubLevel";

    private static volatile boolean initializationAttempted = false;
    private static volatile boolean initializationFailed = false;
    private static volatile boolean failureLogged = false;
    private static Object helperInstance;
    private static Method projectOutOfSubLevelMethod;

    private SableCompat() {
    }

    public static Vec3 projectOutOfSubLevel(Level level, Vec3 pos) {
        if (level == null || pos == null || !ModList.get().isLoaded(SABLE_MOD_ID)) {
            return pos;
        }

        if (!ensureInitialized()) {
            return pos;
        }

        try {
            Object projected = projectOutOfSubLevelMethod.invoke(helperInstance, level, pos);
            if (projected instanceof Vec3 vec3) {
                return vec3;
            }
        } catch (ReflectiveOperationException e) {
            initializationFailed = true;
            helperInstance = null;
            projectOutOfSubLevelMethod = null;
            logFailure("Failed to project a position out of a Sable sub-level.", e);
        }

        return pos;
    }

    private static boolean ensureInitialized() {
        if (projectOutOfSubLevelMethod != null && helperInstance != null) {
            return true;
        }

        if (initializationAttempted) {
            return !initializationFailed;
        }

        synchronized (SableCompat.class) {
            if (projectOutOfSubLevelMethod != null && helperInstance != null) {
                return true;
            }
            if (initializationAttempted) {
                return !initializationFailed;
            }

            initializationAttempted = true;
            try {
                Class<?> sableClass = Class.forName(SABLE_CLASS);
                Field helperField = sableClass.getField("HELPER");
                helperInstance = helperField.get(null);
                projectOutOfSubLevelMethod = helperInstance.getClass().getMethod(PROJECT_METHOD, Level.class, net.minecraft.core.Position.class);
                return true;
            } catch (ReflectiveOperationException e) {
                initializationFailed = true;
                logFailure("Failed to initialize Sable compatibility hooks.", e);
                return false;
            }
        }
    }

    private static void logFailure(String message, ReflectiveOperationException e) {
        if (failureLogged) {
            CreateMobilePackages.LOGGER.debug(message, e);
        } else {
            failureLogged = true;
            CreateMobilePackages.LOGGER.warn(message, e);
        }
    }
}
