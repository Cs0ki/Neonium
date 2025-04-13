package io.neox.neonium;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Detects mods at the earliest possible stage, 
 * even before normal mod loading begins.
 * This allows us to detect mods like LittleTiles
 * that need special compatibility handling very early.
 */
public class VeryEarlyModDetector {
    private static final String[] LITTLETILES_CLASS_MARKERS = {
            "com.creativemd.littletiles.LittleTiles",
            "com.creativemd.littletiles.LittleTilesTransformer",
            "com.creativemd.littletiles.LittlePatchingLoader"
    };
    
    private static Boolean cachedLittleTilesResult = null;
    
    static {
        // Initialize class markers early
        LITTLETILES_CLASS_MARKERS[0] = "com.creativemd.littletiles.LittleTiles";
        LITTLETILES_CLASS_MARKERS[1] = "com.creativemd.littletiles.LittleTilesTransformer";
        LITTLETILES_CLASS_MARKERS[2] = "com.creativemd.littletiles.LittlePatchingLoader";
    }
    
    /**
     * Checks if LittleTiles is present.
     * This method is intended to be called very early in the loading process.
     * Results are cached after the first check.
     * 
     * @return true if LittleTiles is detected
     */
    public static boolean isLittleTilesPresent() {
        // If we already detected, return the cached result
        if (cachedLittleTilesResult != null) {
            return cachedLittleTilesResult;
        }
        
        try {
            boolean result = scanModsForLittleTiles();
            cachedLittleTilesResult = result;
            return result;
        } catch (Throwable t) {
            System.err.println("[Neonium] Error detecting LittleTiles: " + t.getMessage());
            return false;
        }
    }
    
    /**
     * Scans the mods directory for LittleTiles
     * 
     * @return true if LittleTiles is found
     */
    private static boolean scanModsForLittleTiles() {
        try {
            // Look for mods directory in current directory
            File modsDir = new File("mods");
            if (!modsDir.exists() || !modsDir.isDirectory()) {
                // Try minecraft directory
                modsDir = new File(".", "mods");
                if (!modsDir.exists() || !modsDir.isDirectory()) {
                    // Try parent directory
                    modsDir = new File("..", "mods");
                    if (!modsDir.exists() || !modsDir.isDirectory()) {
                        return false;
                    }
                }
            }
            
            File[] files = modsDir.listFiles();
            
            if (files == null) {
                return false;
            }
            
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    boolean found = scanJarForLittleTiles(file);
                    if (found) {
                        System.out.println("[io.neox.neonium.VeryEarlyModDetector:scanModsForLittleTiles:90]: [Neonium] LittleTiles detected in JAR file: " + file.getName());
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Throwable t) {
            System.err.println("[Neonium] Error scanning mods directory for LittleTiles: " + t.getMessage());
            return false;
        }
    }
    
    /**
     * Scans a JAR file for signs of LittleTiles
     * Using a non-blocking approach that only checks key markers
     * 
     * @param file the JAR file to scan
     * @return true if the JAR file appears to contain LittleTiles
     */
    private static boolean scanJarForLittleTiles(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            // First, try to check mcmod.info
            JarEntry mcmodInfo = jarFile.getJarEntry("mcmod.info");
            if (mcmodInfo != null) {
                try (InputStream is = jarFile.getInputStream(mcmodInfo)) {
                    String mcmodContent = readStream(is);
                    if (mcmodContent.contains("\"modid\": \"littletiles\"") || 
                            mcmodContent.contains("\"modid\":\"littletiles\"")) {
                        return true;
                    }
                }
            }
            
            // Next, look for class files
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    if (entryName.endsWith(".class")) {
                        String className = entryName.replace('/', '.').replace(".class", "");
                        for (String marker : LITTLETILES_CLASS_MARKERS) {
                            if (className.equals(marker)) {
                                return true;
                            }
                        }
                    }
                }
            }
            
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Reads all bytes from an input stream into a string
     * Java 8 compatible alternative to readAllBytes()
     */
    private static String readStream(InputStream input) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }
} 