package me.drawn.replica.npc;

import me.drawn.replica.nms.NMSHandler;
import me.drawn.replica.npc.custom.ModelEngineNPC;
import me.drawn.replica.npc.custom.NPCHologram;
import me.drawn.replica.npc.custom.PlayerNPC;
import me.drawn.replica.npc.enums.InteractionType;
import me.drawn.replica.npc.enums.NPCType;
import me.drawn.replica.utils.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class NPC {

    protected final int id;
    protected final String name;
    protected final NPCData npcData;
    @Nullable protected NPCHologram hologram;

    protected Object entity;
    protected int entityId = 0;

    public Object getEntity() {return this.entity;}
    public void setEntity(Object newEntity) {
        this.entity = newEntity;
    }

    protected Location location;
    private boolean spawnedForAnyone = false;

    protected Set<UUID> audience = new HashSet<>();

    public Set<UUID> getAudience() {
        return audience;
    }

    public void setEntityId(int newId) {
        this.entityId = newId;
    }
    public int getEntityId() {
        return entityId;
    }

    public List<Player> getAudiencePlayers() {
        return audience.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public double getEyeHeight() {
        return NMSHandler.get().getEyeHeight(this.entity);
    }

    public String getName() {return this.name;}
    public Location getLocation() {return this.location;}
    public int getId() {return this.id;}
    public boolean isActive() {return this.spawnedForAnyone;}
    public NPCData getNPCData() {return this.npcData;}

    public NPC(int id, NPCData npcData) {
        this.npcData = npcData;
        this.name = npcData.name();
        this.id = id;
        this.location = npcData.location();

        recalculateNumericValues();
        NPCHandler.npcs.add(this);
    }

    @Nullable
    public NPCHologram getHologram() {
        return hologram;
    }

    public void initializeHologram() {
        if(npcData.hologramEnabled()) {
            this.hologram = new NPCHologram(this);
        }
    }

    /*
    The main method to spawn the NPC.
    This is called each time a player first-time enters the render range of a NPC.
     */
    public abstract void spawn(Player player);

    /*
    The main method to remove the NPC.
    This is called each time a player goes further away from a NPC and needs to unload it.
     */
    public abstract void remove(Player player);

    public void changeNPCType(NPCType newType) {
        this.removeForAll();

        npcData.withType(newType);

        NPC newNpc;
        if (newType == NPCType.PLAYER) {
            newNpc = new PlayerNPC(this.id, npcData);
        } else if (newType == NPCType.MODEL_ENGINE) {
            newNpc = new ModelEngineNPC(this.id, npcData);
        } else {
            throw new IllegalArgumentException("Invalid Type: " + newType);
        }

        // Replaces run-time
        NPCHandler.replaceNPC(this, newNpc);
    }

    public void onInteraction(Player player, InteractionType interactionType) {
        npcData.getInteractions().stream()
                .filter(e -> e.getInteractionType() == interactionType)
                .forEach(interaction -> {
                    player.chat("/"+interaction.getCommand());
                });
    }

    public void setToFile() {
        final String path = "npcs."+id;
        final NPCType npcType = npcData.npcType();

        DataManager.npcData.set(path+".type", npcType.toString());
        DataManager.npcData.set(path+".uuid", npcData.uuid().toString());
        DataManager.npcData.set(path+".name", name);

        // Location save
        DataManager.npcData.set(path+".location.xyz", location.getWorld().getName()+" "+location.getX()+" "+location.getY()+" "+location.getZ());
        DataManager.npcData.set(path+".location.yaw-pitch", location.getYaw()+" "+location.getPitch());

        // Global options
        DataManager.npcData.set(path+".global-options.silent", npcData.silent());
        DataManager.npcData.set(path+".global-options.look-close.enabled", npcData.lookClose());
        DataManager.npcData.set(path+".global-options.look-close.range", npcData.lookCloseRange());
        DataManager.npcData.set(path+".global-options.no-gravity", npcData.noGravity());

        // Interactions save
        if(npcData.hasInteractions()) {
            for(int index = 0; index < npcData.getInteractions().size(); index++) {
                final NPCInteraction interaction = npcData.getInteractions().get(index);
                DataManager.npcData.set(path+".actions."+index+".type", interaction.getInteractionType().toString());
                DataManager.npcData.set(path+".actions."+index+".command", interaction.getCommand());
            }
        }

        // only setting if the value is not the default one
        if(npcData.hasScale())
            DataManager.npcData.set(path+".global-options.scale", npcData.scale());

        // Per-type options
        if(npcType == NPCType.PLAYER) {
            DataManager.npcData.set(path+".options.PLAYER.signature", npcData.skinTexture().signature());
            DataManager.npcData.set(path+".options.PLAYER.textureRaw", npcData.skinTexture().textureRaw());
        }
        if(npcType == NPCType.MODEL_ENGINE) {
            DataManager.npcData.set(path+".options.MODEL_ENGINE.model", (npcData.modelEngine() == null ? ""
                    : npcData.modelEngine()));
        }

        // Holograms
        DataManager.npcData.set(path+".hologram.enabled", npcData.hologramEnabled());
        DataManager.npcData.set(path+".hologram.auto-scale", npcData.hologramAutoScale());
        DataManager.npcData.set(path+".hologram.lines", npcData.hologramLines());

        // only setting if the value is not the default one
        if(!npcData.hologramAutoScale())
            DataManager.npcData.set(path+".hologram.auto-scale", npcData.hologramAutoScale());
    }

    public void delete() {
        NPCHandler.delete(this);

        final String path = "npcs."+id;

        DataManager.npcData.set(path, null);

        NPCHandler.npcs.remove(this);
        NPCHandler.activeNpcs.remove(this);

        List<Player> cached = new ArrayList<>(getAudiencePlayers());
        cached.forEach(this::remove);
    }

    public abstract void shutdown();

    public static final double npcRenderRange = 32;
    public void tick(final Map<Player, Location> cachedGlobalAudience) {
        if(location == null) return;

        spawnedForAnyone = !audience.isEmpty();

        if(spawnedForAnyone)
            NPCHandler.activeNpcs.add(this);
        else
            NPCHandler.activeNpcs.remove(this);

        if(cachedGlobalAudience.isEmpty()) return;

        final World world = location.getWorld();
        if (world == null) return;

        final double x = location.getX();
        final double z = location.getZ();
        final double radiusSquared = npcRenderRange * npcRenderRange;

        for (Player player : cachedGlobalAudience.keySet()) {
            final Location playerLoc = cachedGlobalAudience.getOrDefault(player, player.getLocation());

            final UUID uuid = player.getUniqueId();
            final boolean isInAudience = audience.contains(uuid);

            if(!playerLoc.getWorld().equals(world)) {
                if(isInAudience) {
                    audience.remove(player.getUniqueId());
                    remove(player);
                }
                return;
            }

            double dx = playerLoc.getX() - x;
            double dz = playerLoc.getZ() - z;
            double distanceSquared = dx*dx + dz*dz;

            // Player outside range
            if (distanceSquared > radiusSquared) {
                if (isInAudience) {
                    audience.remove(player.getUniqueId());
                    remove(player);
                }

                // Player inside range
            } else if(!isInAudience) {
                spawn(player);
                audience.add(player.getUniqueId());
            }
        }

        if(this.hologram != null)
            this.hologram.tick();
    }

    public void activeTick() {
        if(audience.isEmpty()) return;

        // Look Close
        lookClose();
    }

    protected double lookCloseMinDistanceSquared = 25; // 5 ^2
    public void recalculateNumericValues() {
        lookCloseMinDistanceSquared = npcData.lookCloseRange() * npcData.lookCloseRange();
    }

    public abstract void lookClose();

    public abstract void teleport(final Location newLocation);

    public void removeForAll() {
        if(!spawnedForAnyone) return;

        Bukkit.getOnlinePlayers().forEach(this::remove);
    }

}
