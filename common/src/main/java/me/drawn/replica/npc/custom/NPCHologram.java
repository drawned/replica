package me.drawn.replica.npc.custom;

import me.drawn.replica.nms.NMSHandler;
import me.drawn.replica.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class NPCHologram {

    private final NPC npc;
    private final Object display;

    protected float textScale;
    protected Vector3f textScale3f;

    public NPC getNPC() {
        return npc;
    }

    public float getTextScale() {
        return textScale;
    }

    public Vector3f getTextScale3f() {
        return textScale3f;
    }

    public void recalculate() {
        this.textScale = (npc.getNPCData().hologramAutoScale()) ?
                ((1f / npc.getNPCData().hologramLines().iterator().next().length()) + 0.75f) * (float)npc.getNPCData().scale()
                :
                1f;
        this.textScale3f = new Vector3f(textScale, textScale, textScale);
    }

    public Object getDisplay() {
        return display;
    }

    public NPCHologram(NPC npc) {
        this.npc = npc;

        recalculate();

        this.display = NMSHandler.get().createHologram(this, npc.getLocation());
    }

    public void spawn(Player player) {
        if(!npc.getNPCData().hologramEnabled()) return;

        NMSHandler.get().spawnHologram(this, player);
    }

    public void remove(Player player) {
        NMSHandler.get().removeHologram(this, player);
    }

    public void tick() {
        NMSHandler.get().tickHologram(this);
    }

    public void removeAll() {
        npc.getAudiencePlayers().forEach(this::remove);
    }

    public Vector calculatePosition() {
        final double offset = this.npc.getEyeHeight()/3;
        return new Vector(npc.getLocation().getX(),
                (this.npc.getLocation().getY()+this.npc.getEyeHeight()+offset),
                this.npc.getLocation().getZ());
    }

}
