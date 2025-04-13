package io.neox.neonium;

import me.jellysquid.mods.sodium.common.config.EarlyModDetection;
import net.minecraftforge.fml.common.Loader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles LittleTiles compatibility both at startup and runtime
 */
public class LittleTilesCompat {
    private static final Logger LOGGER = LogManager.getLogger("LittleTilesCompat");
    private static boolean initialized = false;
    private static boolean littleTilesDetected = false;
    
    /**
     * Safe method to check for LittleTiles, can be called at any time
     * If called before Forge is ready, will return early detection results
     */
    public static boolean isLittleTilesLoaded() {
        // If we've already checked once successfully, return cached result
        if (initialized) {
            return littleTilesDetected;
        }
        
        // First check if we already have early detection results
        if (EarlyModDetection.isLittleTilesPresent()) {
            initialized = true;
            littleTilesDetected = true;
            LOGGER.info("Using early detection result for LittleTiles: FOUND");
            return true;
        }
        
        try {
            // Check if Forge Loader is ready to be queried
            if (Loader.instance() != null) {
                littleTilesDetected = Loader.isModLoaded("littletiles");
                initialized = true;
                
                if (littleTilesDetected) {
                    LOGGER.info("LittleTiles detected via Forge Loader!");
                }
                
                return littleTilesDetected;
            } else {
                // As a last resort, try direct class checking
                try {
                    Class<?> littleTilesClass = Class.forName("com.creativemd.littletiles.LittleTiles", 
                            false, LittleTilesCompat.class.getClassLoader());
                    
                    if (littleTilesClass != null) {
                        LOGGER.info("LittleTiles detected via direct class check!");
                        littleTilesDetected = true;
                        initialized = true;
                        return true;
                    }
                } catch (ClassNotFoundException e) {
                    // Class not found, LittleTiles is not present
                }
                
                // If Forge is not ready, fall back to early detection
                littleTilesDetected = EarlyModDetection.isLittleTilesPresent();
                initialized = true;
                return littleTilesDetected;
            }
        } catch (Throwable t) {
            // Failsafe - if any error occurs, check early detection as a fallback
            LOGGER.debug("Error checking for LittleTiles mod via Forge, using early detection result", t);
            littleTilesDetected = EarlyModDetection.isLittleTilesPresent();
            initialized = true;
            return littleTilesDetected;
        }
    }
    
    /**
     * Call to initialize during FMLPreInitializationEvent when we know Forge is ready
     */
    public static void initialize() {
        isLittleTilesLoaded();
    }
} 