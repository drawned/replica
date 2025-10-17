package me.drawn.replica.npc;

import me.drawn.replica.npc.enums.InteractionType;

public class NPCInteraction {
    private final String command;
    private final InteractionType interactionType;

    public NPCInteraction(final String command, final InteractionType interactionType) {
        this.command = command;
        this.interactionType = interactionType;
    }

    public InteractionType getInteractionType() {
        return this.interactionType;
    }

    public String getCommand() {
        return this.command;
    }
}
