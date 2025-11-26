package me.drawn.replica.nms;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.math.Transformation;
import me.drawn.replica.Replica;
import me.drawn.replica.npc.NPCData;
import me.drawn.replica.npc.custom.NPCHologram;
import me.drawn.replica.npc.custom.PlayerNPC;
import net.minecraft.ChatFormatting;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.drawn.replica.utils.Utils.emptyQuaternion;
import static me.drawn.replica.utils.Utils.emptyVector3f;

public class NMS_v1_21_R10 implements NMS {

    private Scoreboard scoreboard;
    private PlayerTeam npcTeam;

    @Override
    public void tickHologram(NPCHologram npcHologram) {
        final Display.TextDisplay display = (Display.TextDisplay) npcHologram.getDisplay();

        final Vector vector = npcHologram.calculatePosition();
        final Level level = ((CraftWorld)npcHologram.getNPC().getLocation().getWorld()).getHandle();

        display.setLevel(level);
        display.setPos(vector.getX(), vector.getY(), vector.getZ());

        final ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(display.getId(),
                display.getEntityData().getNonDefaultValues());
        npcHologram.getNPC().getAudiencePlayers().forEach(player -> {
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
            connection.send(packet);
        });
    }

    @Override
    public void initializeTeamManager() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();

        this.scoreboard = minecraftServer.getScoreboard();

        PlayerTeam playerTeam = scoreboard.getPlayerTeam("npc_hide");
        if (playerTeam == null) {
            playerTeam = scoreboard.addPlayerTeam("npc_hide");
        }

        playerTeam.setNameTagVisibility(Team.Visibility.NEVER);

        this.npcTeam = playerTeam;
    }

    @Override
    public void spawnPlayerNPC(PlayerNPC playerNPC, Player player) {
        final ServerPlayer serverPlayer = (ServerPlayer)playerNPC.getEntity();

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;

        connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket
                .Action.ADD_PLAYER, serverPlayer));

        final ClientboundAddEntityPacket addPlayerPacket = new ClientboundAddEntityPacket(
                serverPlayer.getId(),
                serverPlayer.getUUID(),
                serverPlayer.getX(),
                serverPlayer.getY(),
                serverPlayer.getZ(),
                serverPlayer.getXRot(), // Pitch
                serverPlayer.getYRot(), // Yaw
                serverPlayer.getType(),
                (byte)0, // entity data (geralmente 0 para players)
                serverPlayer.getDeltaMovement(), // motion/velocity
                serverPlayer.getYHeadRot() // yaw head rotation
        );

        connection.send(addPlayerPacket);

        if(playerNPC.getNPCData().hasScale()) {
            connection.send(new ClientboundUpdateAttributesPacket(serverPlayer.getId(),
                    serverPlayer.getAttributes().getSyncableAttributes()));
        }

        // Show Skin second layer
        final SynchedEntityData.DataItem<Float> dataItem = new SynchedEntityData.DataItem<>(new EntityDataAccessor<>(17, EntityDataSerializers.FLOAT), 127f);
        final ClientboundSetEntityDataPacket playerDataPacket = new ClientboundSetEntityDataPacket(serverPlayer.getId(), List.of(dataItem.value()));

        connection.send(playerDataPacket);

        hideNamesPacket((ServerPlayer) playerNPC.getEntity(), connection);
    }

    @Override
    public void removePlayerNPC(PlayerNPC playerNPC, Player player) {
        final ServerPlayer serverPlayer = (ServerPlayer)playerNPC.getEntity();

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(serverPlayer.getId()));
    }

    public void hideNamesPacket(final ServerPlayer npc, final ServerGamePacketListenerImpl connection) {
        ClientboundSetPlayerTeamPacket packetCreate = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(npcTeam, true);
        ClientboundSetPlayerTeamPacket packetAddPlayer = ClientboundSetPlayerTeamPacket.createPlayerPacket(npcTeam, npc.getName().getString(), ClientboundSetPlayerTeamPacket.Action.ADD);

        connection.send(packetCreate);
        connection.send(packetAddPlayer);
    }

    @Override
    public void teleportPlayerNPC(final PlayerNPC npc, Location newLocation) {
        ServerPlayer player = (ServerPlayer) npc.getEntity();

        player.setLevel(((CraftWorld)newLocation.getWorld()).getHandle());
        player.setPos(newLocation.getX(), newLocation.getY(), newLocation.getZ());

        player.setRot(newLocation.getYaw(), newLocation.getPitch());
        player.setYHeadRot(newLocation.getYaw());
    }

    @Override
    public void initialSkinLoad(PlayerNPC playerNPC, NPCData npcData) {
        final ServerPlayer player = (ServerPlayer) playerNPC.getEntity();

        // NPC Initial Skin Load
        if(!npcData.skinTexture().textureRaw().isEmpty()
                && !npcData.skinTexture().signature().isEmpty()) {
            final GameProfile oldProfile = player.gameProfile;

            Multimap<String, Property> multimap = ImmutableListMultimap.<String, Property>builder()
                    .put("textures",
                            new Property("textures", npcData.skinTexture().textureRaw(), npcData.skinTexture().signature()))
                    .build();

            player.gameProfile = new GameProfile(oldProfile.id(), oldProfile.name(), new PropertyMap(multimap));
        }
    }

    @Override
    public double getEyeHeight(Object livingEntity) {
        return ((LivingEntity)livingEntity).getEyeHeight();
    }

    @Override
    public void setScale(Object livingEntity, final double scale) {
        ((LivingEntity)livingEntity).getAttribute(Attributes.SCALE).setBaseValue(scale);
        ((LivingEntity)livingEntity).refreshDimensions();
    }

    @Override
    public Object createHologram(final NPCHologram npcHologram, final Location location) {
        Level level = ((CraftWorld)location.getWorld()).getHandle();

        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);

        final Vector vector = npcHologram.calculatePosition();
        display.setPos(vector.getX(), vector.getY(), vector.getZ());

        final NPCData npcData = npcHologram.getNPC().getNPCData();

        display.setText(npcData.hologramLines().stream().map(a -> Component.literal(a)
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)).iterator().next());

        display.setTransformation(
                new Transformation(emptyVector3f, emptyQuaternion,
                        npcHologram.getTextScale3f(), emptyQuaternion)
        );

        display.setFlags((byte) 0x01);

        display.setBillboardConstraints(Display.BillboardConstraints.CENTER);

        return display;
    }

    @Override
    public void spawnHologram(NPCHologram npcHologram, Player player) {
        final Display.TextDisplay display = (Display.TextDisplay)npcHologram.getDisplay();

        final Level level = ((CraftWorld)npcHologram.getNPC().getLocation().getWorld()).getHandle();
        final Vector pos = npcHologram.calculatePosition();
        display.setLevel(level);
        display.setPos(pos.getX(), pos.getY(), pos.getZ());

        display.setTransformation(
                new Transformation(emptyVector3f, emptyQuaternion,
                        npcHologram.getTextScale3f(), emptyQuaternion)
        );

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        //connection.send(display.getAddEntityPacket(new ServerEntity(level.getMinecraftWorld(), stand, 0, false, consumer -> {}, Set.of())));

        connection.send(new ClientboundAddEntityPacket(
                display.getId(),
                display.getUUID(),
                display.getX(),
                display.getY(),
                display.getZ(),
                display.getXRot(),
                display.getYRot(),
                display.getType(),
                (byte)0,
                display.getDeltaMovement(),
                display.getYHeadRot()
        ));
        connection.send(new ClientboundSetEntityDataPacket(display.getId(),
                display.getEntityData().getNonDefaultValues()));
    }

    @Override
    public void removeHologram(NPCHologram npcHologram, Player player) {
        final Display.TextDisplay display = (Display.TextDisplay)npcHologram.getDisplay();

        ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
        connection.send(new ClientboundRemoveEntitiesPacket(display.getId()));
    }

    @Override
    public void applySkin(PlayerNPC playerNPC, NPCData.SkinTexture skinTexture, UUID newUuid) {
        final String name = playerNPC.getName();

        // Formats NPC name to not be longer than 16 chars
        String profileName = name.length() > 16 ? name.substring(0, 16) : name;

        playerNPC.getNPCData().withSkinTexture(skinTexture);
        playerNPC.getNPCData().withUUID(newUuid);

        final List<Player> oldAudience = new ArrayList<>(playerNPC.getAudiencePlayers());
        playerNPC.getAudience().clear();

        for(Player player : oldAudience) {
            playerNPC.remove(player);
        }

        ServerPlayer npc = createPlayer(playerNPC, playerNPC.getLocation(), profileName);

        playerNPC.setEntity(npc);

        final GameProfile oldProfile = npc.gameProfile;

        Multimap<String, Property> multimap = ImmutableListMultimap.<String, Property>builder()
                .put("textures",
                        new Property("textures", skinTexture.textureRaw(), skinTexture.signature()))
                .build();
        npc.gameProfile = new GameProfile(oldProfile.id(), oldProfile.name(), new PropertyMap(multimap));

        Replica.getScheduler().runTaskLaterAsynchronously(() -> {
            for(Player player : oldAudience) {
                playerNPC.spawn(player);
            }
        }, 2);
    }

    @Override
    public void playerLookClose(PlayerNPC playerNPC, final Player closest,
                                final Map<Player, Double> distanceMap) {
        final ServerPlayer player = (ServerPlayer)playerNPC.getEntity();
        final double closestEyeY = closest.getLocation().getY() + closest.getEyeHeight();

        double dx = closest.getLocation().getX() - player.getX();
        double dy = (closestEyeY - player.getEyeY());
        double dz = closest.getLocation().getZ() - player.getZ();

        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90F);
        float pitch = (float) -(Math.toDegrees(Math.atan2(dy, distanceXZ)));

        playerNPC.getLocation().setYaw(yaw);
        playerNPC.getLocation().setPitch(pitch);

        player.setYRot(yaw);
        player.setXRot(pitch);

        player.setYHeadRot(yaw);
        player.yBodyRot = yaw;

        ClientboundRotateHeadPacket headPacket = new ClientboundRotateHeadPacket(player,
                (byte) ((yaw * 256F) / 360F));
        ClientboundMoveEntityPacket.Rot bodyPacket = new ClientboundMoveEntityPacket.Rot(
                player.getId(),
                (byte) ((yaw * 256F) / 360F),
                (byte) ((pitch * 256F) / 360F),
                false
        );

        for (Player b : distanceMap.keySet()) {
            ServerPlayer sp = ((CraftPlayer) b).getHandle();
            sp.connection.send(headPacket);
            sp.connection.send(bodyPacket);
        }
    }

    @Override
    public ServerPlayer createPlayer(PlayerNPC playerNPC, final Location location, final String name) {
        final NPCData data = playerNPC.getNPCData();

        final MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        final ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();

        // Formats NPC name to not be longer than 16 chars
        String profileName = name.length() > 16 ? name.substring(0, 16) : name;

        GameProfile gameProfile = new GameProfile(data.uuid(), profileName);

        ServerPlayer newNpc = new ServerPlayer(server, level, gameProfile, ClientInformation.createDefault());
        newNpc.setPos(location.getX(), location.getY(), location.getZ());

        final float yaw = location.getYaw();
        final float pitch = location.getPitch();
        newNpc.setYRot(yaw);          // rotação horizontal (direção que está olhando)
        newNpc.setXRot(pitch);        // rotação vertical (cima/baixo)
        newNpc.setYHeadRot(yaw);      // cabeça segue o yaw
        newNpc.setYBodyRot(yaw); // body yaw = yaw because why not

        newNpc.setSilent(data.silent());
        newNpc.setNoGravity(data.noGravity());

        if(data.hasScale())
            setScale(newNpc, data.scale());

        newNpc.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), newNpc,
                CommonListenerCookie.createInitial(gameProfile, false));

        scoreboard.addPlayerToTeam(newNpc.getScoreboardName(), npcTeam);
playerNPC.setEntityId(newNpc.getId());

        return newNpc;
    }


}