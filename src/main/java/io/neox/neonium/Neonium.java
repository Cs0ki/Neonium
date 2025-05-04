package io.neox.neonium;

import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.common.config.EarlyModDetection;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = Neonium.MODID, name = Neonium.MODNAME, version = Neonium.VERSION, clientSideOnly = true)
public class Neonium {
    public static final String MODID = "neonium";
    public static final String MODNAME = "Neonium";
    public static final String VERSION = "1.1.3";
    public static final String MOD_VERSION = VERSION;

    private static SodiumGameOptions CONFIG;
    public static Logger LOGGER = LogManager.getLogger(MODNAME);

    public static Neonium INSTANCE;

    static {
        // At class load time, run very early detection
        boolean isLittleTilesPresent = VeryEarlyModDetector.isLittleTilesPresent();
        System.out.println("[Neonium] Static initializer: LittleTiles detection = " + isLittleTilesPresent);
        
        try {
            EarlyModDetection.setLittleTilesPresent(isLittleTilesPresent);
        } catch (Throwable t) {
            System.err.println("[Neonium] Error updating early detection: " + t.getMessage());
        }
    }

    public Neonium() {
        INSTANCE = this;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing Neonium " + VERSION);
        
        boolean isLittleTiles = LittleTilesCompat.isLittleTilesLoaded();
        EarlyModDetection.setLittleTilesPresent(isLittleTiles);
        
        if (isLittleTiles) {
            LOGGER.info("LittleTiles detected in preInit - compatibility mode will be used");
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Intentionally empty
    }

    @Mod.EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        boolean isLittleTiles = VeryEarlyModDetector.isLittleTilesPresent();
        EarlyModDetection.setLittleTilesPresent(isLittleTiles);
        
        if (isLittleTiles) {
            LOGGER.info("LittleTiles detected in construction - compatibility mode will be used");
        }
    }

    public static SodiumGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    public static Logger logger() {
        if (LOGGER == null) {
            LOGGER = LogManager.getLogger(MODNAME);
        }

        return LOGGER;
    }

    private static SodiumGameOptions loadConfig() {
        return SodiumGameOptions.load(Minecraft.getMinecraft().gameDir.toPath().resolve("config").resolve(MODID + "-options.json"));
    }

    public static String getVersion() {
        return MOD_VERSION;
    }

    public static boolean isDirectMemoryAccessEnabled() {
        return options().advanced.allowDirectMemoryAccess;
    }
} 