package me.jellysquid.mods.sodium.client.gui.options.storage;

import io.neox.neonium.Neonium;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;

import java.io.IOException;

public class SodiumOptionsStorage implements OptionStorage<SodiumGameOptions> {
    private final SodiumGameOptions options;

    public SodiumOptionsStorage() {
        this.options = Neonium.options();
    }

    @Override
    public SodiumGameOptions getData() {
        return this.options;
    }

    @Override
    public void save() {
        try {
            this.options.writeChanges();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't save configuration changes", e);
        }

        Neonium.logger().info("Flushed changes to " + Neonium.MODNAME + " configuration");
    }
}
