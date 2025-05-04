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
    
    // List of mixins to disable for LittleTiles and SGCraft compatibility
    private static final String[] PROBLEM_MIXINS = {
        "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinRenderGlobal",
        "me.jellysquid.mods.sodium.mixin.features.particle.cull.MixinParticleManager",
        "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinWorldRenderer",
        "me.jellysquid.mods.sodium.mixin.features.chunk_rendering.MixinChunkBuilder"
    };

    private final Logger logger = LogManager.getLogger(Neonium.MODNAME);
    private SodiumConfig config;
    private boolean littleTilesLogged = false;
    private boolean littleTilesDetected = false;
    private boolean sgcraftLogged = false;
    private boolean sgcraftDetected = false;

    public SodiumMixinPlugin() {
        // Detect LittleTiles and SGCraft as early as possible
        tryDetectIncompatibleMods();
    }
    
    private void tryDetectIncompatibleMods() {
        try {
            // Detect LittleTiles
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
            
            // Detect SGCraft
            sgcraftDetected = VeryEarlyModDetector.isSGCraftPresent();
            
            if (!sgcraftDetected) {
                try {
                    Class.forName("gcewing.sg.SGCraft", false, getClass().getClassLoader());
                    sgcraftDetected = true;
                    System.out.println("[Neonium] SGCraft detected via direct class check in MixinPlugin!");
                } catch (ClassNotFoundException e) {
                    try {
                        Class.forName("gcewing.sg.SGCraftClient", false, getClass().getClassLoader());
                        sgcraftDetected = true;
                        System.out.println("[Neonium] SGCraft detected via SGCraftClient class check!");
                    } catch (ClassNotFoundException ex) {
                        sgcraftDetected = false;
                    }
                }
            }
            
            if (littleTilesDetected) {
                System.out.println("[Neonium] LittleTiles detected at MixinPlugin constructor time!");
            }
            
            if (sgcraftDetected) {
                System.out.println("[Neonium] SGCraft detected at MixinPlugin constructor time!");
            }
        } catch (Throwable t) {
            System.err.println("[Neonium] Error during early mod detection: " + t.getMessage());
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        tryDetectIncompatibleMods();
        
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
        
        if (sgcraftDetected) {
            this.logger.info("[SGCraftCompat] SGCraft detected on mixin load, incompatible mixins will be disabled");
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
        
        // Check if this mixin is one of the problematic ones for LittleTiles or SGCraft
        if (littleTilesDetected || sgcraftDetected) {
            for (String problemMixin : PROBLEM_MIXINS) {
                if (mixinClassName.equals(problemMixin)) {
                    if (littleTilesDetected && !littleTilesLogged) {
                        this.logger.info("[LittleTilesCompat] LittleTiles detected, disabling incompatible mixins for compatibility");
                        littleTilesLogged = true;
                    }
                    
                    if (sgcraftDetected && !sgcraftLogged) {
                        this.logger.info("[SGCraftCompat] SGCraft detected, disabling incompatible mixins for compatibility");
                        sgcraftLogged = true;
                    }
                    
                    if (littleTilesDetected) {
                        this.logger.info("Disabling mixin '{}' for LittleTiles compatibility", mixinClassName);
                    }
                    
                    if (sgcraftDetected && !littleTilesDetected) {
                        this.logger.info("Disabling mixin '{}' for SGCraft compatibility", mixinClassName);
                    }
                    
                    return false;
                }
            }
        }
        
        // Fall back to the regular MixinConfig if neither mod was detected via our early methods
        if (!littleTilesDetected && !sgcraftDetected && MixinConfig.shouldDisableMixin(mixinClassName)) {
            this.logger.info("Disabling mixin '{}' for compatibility via MixinConfig", mixinClassName);
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
