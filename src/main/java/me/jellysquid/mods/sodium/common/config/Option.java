package me.jellysquid.mods.sodium.common.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Option {
    private final String name;
    private final Set<String> definingMods = new HashSet<>();
    
    private boolean enabled;
    private boolean userDefined;
    private boolean modDefined;
    
    public Option(String name) {
        this.name = name;
        this.enabled = true;
        this.userDefined = false;
        this.modDefined = false;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isOverridden() {
        return this.userDefined || this.modDefined;
    }
    
    public boolean isUserDefined() {
        return this.userDefined;
    }
    
    public boolean isModDefined() {
        return this.modDefined;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Collection<String> getDefiningMods() {
        return Collections.unmodifiableSet(this.definingMods);
    }
}
