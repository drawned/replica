package me.drawn.replica.api;

import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.enums.InteractionType;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NPCInteractEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {return HANDLERS;}

    @Override
    public @NotNull HandlerList getHandlers() {return HANDLERS;}

    private final InteractionType interactionType;
    private final NPC npc;

    public NPCInteractEvent(final NPC npc, final InteractionType interactionType) {
        this.npc = npc;
        this.interactionType = interactionType;
    }

    public InteractionType getInteractionType() {
        return this.interactionType;
    }

    public NPC getNPC() {
        return this.npc;
    }
}