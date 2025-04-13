package me.jellysquid.mods.sodium.mixin.features.chunk_rendering;

import io.neox.neonium.LittleTilesCompat;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(RenderGlobal.class)
public abstract class MixinWorldRenderer {

    @Shadow
    @Final
    private Map<Integer, DestroyBlockProgress> damagedBlocks;

    @Shadow
    private void renderBlockLayer(BlockRenderLayer blockLayerIn) {
    }

    @Shadow @Final private Minecraft mc;
    private SodiumWorldRenderer renderer;

    @Redirect(method = "loadRenderers", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;renderDistanceChunks:I", ordinal = 1))
    private int nullifyBuiltChunkStorage(GameSettings settings) {
        // Skip custom chunk rendering if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return settings.renderDistanceChunks; // Use vanilla's rendering
        }
        // Do not allow any resources to be allocated
        return 0;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft minecraft, CallbackInfo ci) {
        this.renderer = SodiumWorldRenderer.create();
    }

    @Inject(method = "setWorldAndLoadRenderers", at = @At("RETURN"))
    private void onWorldChanged(WorldClient world, CallbackInfo ci) {
        // Skip if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return;
        }
        
        RenderDevice.enterManagedCode();

        try {
            this.renderer.setWorld(world);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    /**
     * @reason Redirect to our renderer
     * @author JellySquid
     */
    @Overwrite
    public int getRenderedChunks() {
        // Use vanilla behavior if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return 0; // This is a reasonable default, as vanilla code will be used
        }
        return this.renderer.getVisibleChunkCount();
    }

    /**
     * @reason Redirect the check to our renderer
     * @author JellySquid
     */
    @Overwrite
    public boolean hasNoChunkUpdates() {
        // Use vanilla behavior if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return true; // This ensures vanilla renderers function properly
        }
        return this.renderer.isTerrainRenderComplete();
    }

    @Inject(method = "setDisplayListEntitiesDirty", at = @At("RETURN"))
    private void onTerrainUpdateScheduled(CallbackInfo ci) {
        // Skip if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return;
        }
        this.renderer.scheduleTerrainUpdate();
    }

    /**
     * @reason Redirect the chunk layer render passes to our renderer
     * @author JellySquid
     */
    @Overwrite
    public int renderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        // Use vanilla rendering if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            this.renderBlockLayer(blockLayerIn);
            return 0;
        }
        
        RenderDevice.enterManagedCode();

        RenderHelper.disableStandardItemLighting();

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.bindTexture(this.mc.getTextureMapBlocks().getGlTextureId());
        GlStateManager.enableTexture2D();

        this.mc.entityRenderer.enableLightmap();

        double d3 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double d4 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
        double d5 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;

        try {
            this.renderer.drawChunkLayer(blockLayerIn, d3, d4, d5);
        } finally {
            RenderDevice.exitManagedCode();
        }

        this.mc.entityRenderer.disableLightmap();

        return 1;
    }

    /**
     * @reason Redirect the terrain setup phase to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void setupTerrain(Entity entity, double tick, ICamera camera, int frame, boolean spectator) {
        // Use vanilla rendering if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            // Let vanilla handle it by skipping our custom implementation
            return;
        }
        
        RenderDevice.enterManagedCode();

        boolean hasForcedFrustum = false;
        try {
            this.renderer.updateChunks((Frustum) camera, (float)tick, hasForcedFrustum, frame, spectator);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    private void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        // Skip if LittleTiles is loaded, let vanilla handle it
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return;
        }
        this.renderer.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, important);
    }

    // The following two redirects force light updates to trigger chunk updates and not check vanilla's chunk renderer
    // flags
    @Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;hasNoFreeRenderBuilders()Z"))
    private boolean alwaysHaveBuilders(ChunkRenderDispatcher instance) {
        // Skip if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return instance.hasNoFreeRenderBuilders();
        }
        return false;
    }

    @Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z", ordinal = 1))
    private boolean alwaysHaveNoTasks(Set instance) {
        // Skip if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return instance.isEmpty();
        }
        return true;
    }

    @Inject(method = "loadRenderers", at = @At("RETURN"))
    private void onReload(CallbackInfo ci) {
        // Skip if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return;
        }
        
        RenderDevice.enterManagedCode();

        try {
            this.renderer.reload();
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;enableStandardItemLighting()V", shift = At.Shift.AFTER, ordinal = 1), cancellable = true)
    public void sodium$renderTileEntities(Entity entity, ICamera camera, float partialTicks, CallbackInfo ci) {
        // Skip if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return;
        }
        
        this.renderer.renderTileEntities(partialTicks, damagedBlocks);

        this.mc.entityRenderer.disableLightmap();
        this.mc.profiler.endSection();
        ci.cancel();
    }

    /**
     * @reason Replace the debug string
     * @author JellySquid
     */
    @Overwrite
    public String getDebugInfoRenders() {
        // Use vanilla behavior if LittleTiles is loaded
        if (LittleTilesCompat.isLittleTilesLoaded()) {
            return ""; // Default to empty string, vanilla will handle debug info
        }
        return this.renderer.getChunksDebugString();
    }
}
