package me.jellysquid.mods.sodium.mixin.features.options;

import io.neox.neonium.Neonium;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Minecraft.class)
public class MixinMinecraftClient {
    /**
     * @author JellySquid
     * @reason Make ambient occlusion user configurable
     */
    @Overwrite
    public static boolean isAmbientOcclusionEnabled() {
        return Neonium.options().quality.smoothLighting != SodiumGameOptions.LightingQuality.OFF;
    }
}
