package me.jellysquid.mods.sodium.mixin;

import io.neox.neonium.Neonium;
import io.neox.neonium.VeryEarlyModDetector;
import me.jellysquid.mods.sodium.common.config.SodiumConfig;
import me.jellysquid.mods.sodium.common.config.MixinConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.util.List;
import java.util.Set;

public class SodiumMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.jellysquid.mods.sodium.mixin.";
    
    // List of mixins to disable for LittleTiles compatibility
    private static final String[] LITTLETILES_PROBLEM_MIXINS = {
        "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinRenderGlobal",
        "me.jellysquid.mods.sodium.mixin.features.particle.cull.MixinParticleManager",
        "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer",
        "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinChunkBuilder"
    };

    private final Logger logger = LogManager.getLogger(Neonium.MODNAME);
    private SodiumConfig config;
    private boolean littleTilesLogged = false;
    private boolean littleTilesDetected = false;

    public SodiumMixinPlugin() {
        // Detect LittleTiles as early as possible
        tryDetectLittleTiles();
    }
    
    private void tryDetectLittleTiles() {
        try {
            littleTilesDetected = VeryEarlyModDetector.isLittleTilesPresent();
            
            if (!littleTilesDetected) {
                try {
                    Class.forName("com.creativemd.littletiles.LittlePatchingLoader", false, getClass().getClassLoader());
                    littleTilesDetected = true;
                    System.out.println("[Neonium] LittleTiles detected via direct class check in MixinPlugin!");
                } catch (ClassNotFoundException e) {
                    try {
                        Class.forName("com.creativemd.littletiles.LittleTilesTransformer", false, getClass().getClassLoader());
                        littleTilesDetected = true;
                        System.out.println("[Neonium] LittleTiles detected via LittleTilesTransformer class check!");
                    } catch (ClassNotFoundException ex) {
                        littleTilesDetected = false;
                    }
                }
            }
            
            if (littleTilesDetected) {
                System.out.println("[Neonium] LittleTiles detected at MixinPlugin constructor time!");
            }
        } catch (Throwable t) {
            System.err.println("[Neonium] Error during early LittleTiles detection: " + t.getMessage());
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        tryDetectLittleTiles();
        
        try {
            this.config = SodiumConfig.load(new File(".").toPath().resolve("config").resolve(Neonium.MODID + "-mixins.properties").toFile());
        } catch (Exception e) {
            throw new RuntimeException("Could not load configuration file for " + Neonium.MODNAME, e);
        }

        this.logger.info("Loaded configuration file for " + Neonium.MODNAME + ": {} options available, {} override(s) found",
                this.config.getOptionCount(), this.config.getOptionOverrideCount());
                
        if (littleTilesDetected) {
            this.logger.info("[LittleTilesCompat] LittleTiles detected on mixin load, incompatible mixins will be disabled");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            this.logger.error("Expected mixin '{}' to start with package root '{}', treating as foreign and " +
                    "disabling!", mixinClassName, MIXIN_PACKAGE_ROOT);

            return false;
        }
        
        // Check if this mixin is one of the problematic ones for LittleTiles
        if (littleTilesDetected) {
            for (String problemMixin : LITTLETILES_PROBLEM_MIXINS) {
                if (mixinClassName.equals(problemMixin)) {
                    if (!littleTilesLogged) {
                        this.logger.info("[LittleTilesCompat] LittleTiles detected, disabling incompatible mixins for compatibility");
                        littleTilesLogged = true;
                    }
                    this.logger.info("Disabling mixin '{}' for LittleTiles compatibility", mixinClassName);
                    return false;
                }
            }
        }
        
        // Fall back to the regular MixinConfig if LittleTiles wasn't detected via our early methods
        if (!littleTilesDetected && MixinConfig.shouldDisableMixin(mixinClassName)) {
            this.logger.info("Disabling mixin '{}' for LittleTiles compatibility via MixinConfig", mixinClassName);
            return false;
        }
        
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());
        SodiumConfig.Option option = this.config.getEffectiveOptionForMixin(mixin);

        if (option == null) {
            this.logger.error("No rules matched mixin '{}', treating as foreign and disabling!", mixin);
            return false;
        }

        if (option.isUserDefined()) {
            String source = "user configuration";
            
            if (option.isEnabled()) {
                this.logger.warn("Force-enabling mixin '{}' as rule '{}' (added by {}) enables it", mixin,
                        option.getKey(), source);
            } else {
                this.logger.warn("Force-disabling mixin '{}' as rule '{}' (added by {}) disables it and children", mixin,
                        option.getKey(), source);
            }
        }

        return option.isEnabled();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
