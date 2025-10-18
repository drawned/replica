package me.drawn.replica.api;

import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.enums.InteractionType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCSpawnEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {return HANDLERS;}

    @Override
    public @NotNull HandlerList getHandlers() {return HANDLERS;}

    private final NPC npc;
    private final Player player;

    private boolean isCancelled;

    public NPCSpawnEvent(final NPC npc, final Player player) {
        this.npc = npc;
        this.player = player;
        this.isCancelled = false;
    }

    public Player getPlayer() {
        return this.player;
    }

    public NPC getNPC() {
        return this.npc;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}