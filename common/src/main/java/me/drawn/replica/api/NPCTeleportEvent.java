package me.drawn.replica.api;

import me.drawn.replica.npc.NPC;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCTeleportEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {return HANDLERS;}

    @Override
    public @NotNull HandlerList getHandlers() {return HANDLERS;}

    private final NPC npc;

    public NPCTeleportEvent(final NPC npc) {
        this.npc = npc;
    }

    public NPC getNPC() {
        return this.npc;
    }

}
