package me.jellysquid.mods.sodium.mixin.features.world_ticking;

import me.jellysquid.mods.sodium.client.util.rand.XoRoShiRoRandom;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(value = WorldClient.class, priority = 900)
public abstract class MixinClientWorld extends World {
    private static final XoRoShiRoRandom optimizedRandom = new XoRoShiRoRandom();

    protected MixinClientWorld(ISaveHandler saveHandler, WorldInfo worldInfo, WorldProvider worldProvider, Profiler profiler, boolean client) {
        super(saveHandler, worldInfo, worldProvider, profiler, client);
    }

    @Inject(method = "doVoidFogParticles", at = @At("HEAD"))
    private void onDoVoidFogParticles(CallbackInfo ci) {
        // We'll use the optimized random instance here
        optimizedRandom.nextInt(16);
    }
}
