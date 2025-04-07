package me.jellysquid.mods.sodium.client;

import com.google.common.collect.ImmutableList;
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

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
