package me.jellysquid.mods.sodium.client;

import com.google.common.collect.ImmutableList;
import io.neox.neonium.VeryEarlyModDetector;
import me.jellysquid.mods.sodium.common.config.EarlyModDetection;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("neonium")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class SodiumMixinTweaker implements IFMLLoadingPlugin, IEarlyMixinLoader {
    private static final List<String> MIXINS = ImmutableList.of(
            "mixins.neonium.json"
    );
    
    static {
        // Run early detection as soon as this class is loaded
        try {
            boolean isLittleTilesPresent = VeryEarlyModDetector.isLittleTilesPresent();
            System.out.println("[Neonium] Early LittleTiles detection result: " + isLittleTilesPresent);
            
            // Initialize our early detection system with the result
            EarlyModDetection.setLittleTilesPresent(isLittleTilesPresent);
        } catch (Throwable t) {
            System.err.println("[Neonium] Error during early mod detection: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public List<String> getMixinConfigs() {
        return MIXINS;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // Run detection again to be sure
        try {
            boolean isLittleTilesPresent = VeryEarlyModDetector.isLittleTilesPresent();
            EarlyModDetection.setLittleTilesPresent(isLittleTilesPresent);
        } catch (Throwable t) {
            System.err.println("[Neonium] Error during mod detection in injectData: " + t.getMessage());
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
