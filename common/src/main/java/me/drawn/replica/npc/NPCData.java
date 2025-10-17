package me.drawn.replica.npc;

import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.utils.Utils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class NPCData {

    public static record GlobalOptions(boolean silent, boolean lookCloseEnabled, double lookCloseRange, boolean noGravity) {}
    public static record SkinTexture(String signature, String textureRaw) {}

    private final Location location;
    private final String name;

    private NPCType npcType;
    private UUID uuid;

    private List<String> hologramLines;
    private boolean hologramEnabled;

    private SkinTexture skinTexture = new SkinTexture("", "");
    private String modelEngine = "";

    public static final double DEFAULT_SCALE = 1.0;

    public SkinTexture skinTexture() {return skinTexture;}
    public String modelEngine() {return modelEngine;}

    private boolean hologramAutoScale;
    private boolean silent;
    private boolean lookClose;
    private double lookCloseRange;
    private boolean noGravity;
    private double scale;

    private final List<NPCInteraction> interactions;

    public UUID uuid() {return uuid;}
    public String name() {return name;}
    public NPCType npcType() {return npcType;}
    public Location location() {return location;}
    public @Nullable List<String> hologramLines() {return hologramLines;}

    public NPCData(final Location location, String name) {
        this.location = location;
        this.name = Utils.sanitize(name);

        this.interactions = new ArrayList<>();
        this.hologramLines = List.of(name);
        this.npcType = NPCType.PLAYER;
        this.uuid = UUID.randomUUID();

        this.silent = false;
        this.lookClose = true;
        this.lookCloseRange = 5;
        this.noGravity = false;
        this.hologramEnabled = true;
        this.scale = DEFAULT_SCALE;
        this.hologramAutoScale = true;
    }

    public List<NPCInteraction> getInteractions() {
        return this.interactions;
    }

    public void addInteraction(final NPCInteraction npcInteraction) {
        this.interactions.add(npcInteraction);
    }
    public void clearInteractions() {
        this.interactions.clear();
    }

    public boolean hasInteractions() {
        return !interactions.isEmpty();
    }

    public boolean hasScale() {
        return scale != DEFAULT_SCALE;
    }

    public double scale() {return scale;}
    public boolean hologramEnabled() {return hologramEnabled;}
    public double lookCloseRange() {return lookCloseRange;}
    public boolean lookClose() {return lookClose;}
    public boolean noGravity() {return noGravity;}
    public boolean silent() {return silent;}

    public boolean hologramAutoScale() {
        return hologramAutoScale;
    }

    public void setHologramAutoScale(boolean b) {
        this.hologramAutoScale = b;
    }

    public void setLookCloseEnabled(boolean option) {
        this.lookClose = option;
    }

    public void setScale(final double scale) {this.scale = scale;}

    public void setLookCloseRange(double range) {
        this.lookCloseRange = range;
    }

    public NPCData withHologramEnabled(final boolean enabled) {
        this.hologramEnabled = enabled;
        return this;
    }

    public NPCData withModelEngine(final String model) {
        this.modelEngine = model;
        return this;
    }

    public NPCData withHologramLines(@Nullable List<String> components) {
        this.hologramLines = components;
        return this;
    }

    public NPCData withUUID(final UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public NPCData withType(final NPCType npcType) {
        this.npcType = npcType;
        return this;
    }

    public NPCData withSkinTexture(final SkinTexture skinTexture) {
        if(npcType != NPCType.PLAYER) return this;

        this.skinTexture = skinTexture;
        return this;
    }

}
