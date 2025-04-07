package me.jellysquid.mods.sodium.client.gui.options;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public enum OptionImpact {
    LOW(TextFormatting.GREEN, new TextComponentTranslation("neonium.option_impact.low").getFormattedText()),
    MEDIUM(TextFormatting.YELLOW, new TextComponentTranslation("neonium.option_impact.medium").getFormattedText()),
    HIGH(TextFormatting.GOLD, new TextComponentTranslation("neonium.option_impact.high").getFormattedText()),
    EXTREME(TextFormatting.RED, new TextComponentTranslation("neonium.option_impact.extreme").getFormattedText()),
    VARIES(TextFormatting.WHITE, new TextComponentTranslation("neonium.option_impact.varies").getFormattedText());

    private final TextFormatting color;
    private final String text;

    OptionImpact(TextFormatting color, String text) {
        this.color = color;
        this.text = text;
    }

    public TextFormatting getColor() {
        return this.color;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public String toString() {
        return this.color + this.text;
    }

    public String toDisplayString() {
        return this.toString();
    }
}
