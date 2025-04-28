package me.jellysquid.mods.sodium.common.config;

import io.neox.neonium.LittleTilesCompat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class to control whether specific mixins are disabled
 */
public class MixinConfig {
    // Store whether we've detected LittleTiles
    private static Boolean littleTilesDetected = null;
    private static final Logger LOGGER = LogManager.getLogger("LittleTilesCompat");
    
    /**
     * Checks if a specific mixin should be disabled
     * @param mixinClassName The full mixin class name
     * @return true if the mixin should be disabled
     */
    public static boolean shouldDisableMixin(String mixinClassName) {
        return shouldDisableForLittleTilesCompat(mixinClassName);
    }
    
    /**
     * Check if LittleTiles is present using our various detection methods
     */
    private static boolean isLittleTilesPresent() {
        // If we've already detected, return the cached result
        if (littleTilesDetected != null) {
            return littleTilesDetected;
        }
        
        // FIRST: Try using the very early detection result
        // This should have been set before any mixins are loaded
        boolean isPresent = EarlyModDetection.isLittleTilesPresent();
        
        if (isPresent) {
            LOGGER.info("LittleTiles detected via early detection!");
            littleTilesDetected = true;
            return true;
        }
        
        // SECOND: Try using runtime detection as a backup
        try {
            isPresent = LittleTilesCompat.isLittleTilesLoaded();
            
            if (isPresent) {
                LOGGER.info("LittleTiles detected via runtime detection!");
            }
            
            // Cache the result
            littleTilesDetected = isPresent;
            return isPresent;
        } catch (Throwable t) {
            // Failsafe - if any error occurs, check class presence directly as a last resort
            try {
                isPresent = Class.forName("com.creativemd.littletiles.LittleTiles", false, MixinConfig.class.getClassLoader()) != null;
                LOGGER.info("LittleTiles detected via direct class check!");
                littleTilesDetected = isPresent;
                return isPresent;
            } catch (ClassNotFoundException e) {
                LOGGER.debug("LittleTiles not found via direct class check");
                littleTilesDetected = false;
                return false;
            }
        }
    }
    
    /**
     * Checks if a mixin should be disabled for LittleTiles compatibility
     * @param mixinClassName The full mixin class name
     * @return true if the mixin should be disabled for LittleTiles compatibility
     */
    private static boolean shouldDisableForLittleTilesCompat(String mixinClassName) {
        // Only disable mixins if LittleTiles mod is present
        if (!isLittleTilesPresent()) {
            return false;
        }
        
        // List of mixins to disable for LittleTiles compatibility
        boolean shouldDisable = 
               mixinClassName.equals("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinRenderGlobal") ||
               mixinClassName.equals("me.jellysquid.mods.sodium.mixin.features.particle.cull.MixinParticleManager") ||
               mixinClassName.equals("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer") ||
               mixinClassName.equals("me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinChunkBuilder");
        
        if (shouldDisable) {
            LOGGER.info("Disabling mixin '" + mixinClassName + "' for LittleTiles compatibility");
        }
        
        return shouldDisable;
    }
} 