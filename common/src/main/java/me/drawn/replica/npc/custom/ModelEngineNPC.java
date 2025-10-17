package me.drawn.replica.npc.custom;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import me.drawn.replica.nms.NMSHandler;
import me.drawn.replica.npc.NPC;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.NPCHandler;
import me.drawn.replica.utils.DataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;

import java.util.*;

public class ModelEngineNPC extends NPC {

    private final Dummy<?> dummy;
    private ModeledEntity modeledEntity;
    private ActiveModel activeModel;

    private boolean renderCanceled;

    @Override
    public double getEyeHeight() {
        return (activeModel == null) ? 0 : activeModel.getHitboxScale().y();
    }

    public ModelEngineNPC(int id, NPCData npcData) {
        super(id, npcData);

        this.renderCanceled = true;

        // Spawn Dummy
        this.dummy = new Dummy<>();

        this.dummy.syncLocation(this.location);

        // We will handle model loading and unloading.
        this.dummy.setDetectingPlayers(false);

        // Creating the modeled entity and the initial model
        this.modeledEntity = ModelEngineAPI.createModeledEntity(dummy);

        this.setEntityId(dummy.getEntityId());

        applyModel(npcData.modelEngine());

        initializeHologram();
    }

    public void applyModel(final String model) {
        ActiveModel active = null;

        try {
            active = ModelEngineAPI.createActiveModel(model);
        } catch (RuntimeException ignored) {}

        if(active == null)
            return;

        if(this.activeModel != null)
            this.activeModel.destroy();

        Set<String> models = new HashSet<>(this.modeledEntity.getModels().keySet());
        models.forEach(m1 -> {
            this.modeledEntity.removeModel(m1);
        });

        npcData.withModelEngine(model);

        this.activeModel = active;

        this.modeledEntity.addModel(this.activeModel, true);

        this.activeModel.getBones().values().forEach(bone -> {
            bone.getBoneBehavior(BoneBehaviorTypes.NAMETAG).ifPresent(tag -> {
                tag.setString(this.name);
                tag.setComponent(Component.text(this.name)
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD));
                tag.setVisible(true);
                tag.setBillboard(Display.Billboard.CENTER);
                //tag.setScale(this.hologram.textScale3f);
            });
        });

        this.activeModel.getBones().values().forEach(bone -> {
            bone.getBoneBehavior(BoneBehaviorTypes.HEAD).ifPresent(head -> {
                head.setLocal(true);
            });
        });

        if(this.hologram != null)
            this.hologram.tick();
    }

    @Override
    public void spawn(Player player) {
        if(renderCanceled) {
            ModelEngineAPI.setRenderCanceled(dummy.getEntityId(), false);
            renderCanceled = false;
        }

        dummy.setForceHidden(player, false);
        dummy.setForceViewing(player, true);

        if(this.hologram != null)
            this.hologram.spawn(player);
    }

    @Override
    public void delete() {
        NPCHandler.delete(this);

        shutdown();

        final String path = "npcs."+id;

        DataManager.npcData.set(path, null);

        NPCHandler.npcs.remove(this);
        NPCHandler.activeNpcs.remove(this);

        List<Player> cached = new ArrayList<>(getAudiencePlayers());
        cached.forEach(this::remove);
    }

    @Override
    public void shutdown() {
        if(activeModel != null)
            activeModel.destroy();
        if(modeledEntity != null)
            modeledEntity.destroy();
        dummy.setRemoved(true);
    }

    @Override
    public void remove(Player player) {
        if(audience.isEmpty() && !renderCanceled) {
            ModelEngineAPI.setRenderCanceled(dummy.getEntityId(), true);
            renderCanceled = true;
        }

        dummy.setForceHidden(player, true);
        dummy.setForceViewing(player, false);

        if(this.hologram != null)
            this.hologram.remove(player);
    }

    @Override
    public void lookClose() {}

    @Override
    public void teleport(Location newLocation) {
        dummy.syncLocation(newLocation);

        if(this.hologram != null) {
            this.hologram.tick();

            for (Player player : getAudiencePlayers()) {
                this.hologram.remove(player);
                this.hologram.spawn(player);
            }
        }
    }

}
