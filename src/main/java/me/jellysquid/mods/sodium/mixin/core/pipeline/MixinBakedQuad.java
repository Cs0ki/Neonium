package me.jellysquid.mods.sodium.mixin.core.pipeline;

import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFlags;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescription;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BakedQuad.class)
public class MixinBakedQuad implements ModelQuadView {

    @Shadow
    @Final
    protected TextureAtlasSprite sprite;

    @Shadow
    @Final
    protected int tintIndex;

    @Shadow public int[] getVertexData() {
        throw new AssertionError();
    }

    @Shadow @Final protected VertexFormat format;
    protected int cachedFlags;

    private VertexFormatDescription formatDescription;

    @Inject(method = "<init>([IILnet/minecraft/util/EnumFacing;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;ZLnet/minecraft/client/renderer/vertex/VertexFormat;)V", at = @At("RETURN"))
    private void init(int[] vertexData, int colorIndex, EnumFacing face, TextureAtlasSprite sprite, boolean shade, VertexFormat format, CallbackInfo ci) {
        this.formatDescription = VertexFormatDescription.get(format);
        try {
            if(!UnpackedBakedQuad.class.isAssignableFrom(this.getClass())) {
                this.cachedFlags = ModelQuadFlags.getQuadFlags((BakedQuad) (Object) this);
            }
        } catch (Exception e) {
            // Safely handle any errors during flag calculation
            this.cachedFlags = 0;
        }
    }

    private int vertexOffset(int idx) {
        return idx * this.format.getIntegerSize();
    }

    @Override
    public float getX(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int positionIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.POSITION) : -1;
            if (positionIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + positionIndex;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return Float.intBitsToFloat(vertexData[offset]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public float getY(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int positionIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.POSITION) : -1;
            if (positionIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + positionIndex + 1;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return Float.intBitsToFloat(vertexData[offset]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public float getZ(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int positionIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.POSITION) : -1;
            if (positionIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + positionIndex + 2;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return Float.intBitsToFloat(vertexData[offset]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getColor(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int colorIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.COLOR) : -1;
            if (colorIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + colorIndex;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return vertexData[offset];
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public TextureAtlasSprite rubidium$getSprite() {
        return this.sprite;
    }

    @Override
    public float getTexU(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int textureIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.TEXTURE) : -1;
            if (textureIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + textureIndex;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return Float.intBitsToFloat(vertexData[offset]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public float getTexV(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int textureIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.TEXTURE) : -1;
            if (textureIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + textureIndex + 1;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return Float.intBitsToFloat(vertexData[offset]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getFlags() {
        return this.cachedFlags;
    }

    @Override
    public int getNormal(int idx) {
        try {
            int[] vertexData = this.getVertexData();
            if (vertexData == null || vertexData.length == 0) {
                return 0;
            }
            
            int normalIndex = this.formatDescription != null ? this.formatDescription.getIndex(VertexFormatDescription.Element.NORMAL) : -1;
            if (normalIndex == -1) {
                return 0;
            }
            
            int offset = vertexOffset(idx) + normalIndex;
            if (offset < 0 || offset >= vertexData.length) {
                return 0;
            }
            
            return vertexData[offset];
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getColorIndex() {
        return this.tintIndex;
    }
}
