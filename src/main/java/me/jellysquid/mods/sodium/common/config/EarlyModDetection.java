package me.jellysquid.mods.sodium.common.config;

/**
 * Stores results from early mod detection so they can be used throughout the mod loading process.
 * This class is initialized very early in the mod loading process, before mixins are applied.
 */
public class EarlyModDetection {
    private static boolean littleTilesPresent = false;
    private static boolean initialized = false;
    
    /**
     * Sets whether LittleTiles is present.
     * This should only be called early in the loading process.
     */
    public static void setLittleTilesPresent(boolean present) {
        littleTilesPresent = present;
        initialized = true;
        System.out.println("[Neonium] EarlyModDetection: LittleTiles present = " + present);
    }
    
    /**
     * Checks if LittleTiles is present based on early detection.
     * For use by mixins and other early-loading code.
     */
    public static boolean isLittleTilesPresent() {
        if (!initialized) {
            System.out.println("[Neonium] Warning: EarlyModDetection queried before initialization!");
        }
        return littleTilesPresent;
    }
} 