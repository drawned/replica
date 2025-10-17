package me.drawn.replica.nms;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FakeConnection_1_20_R4 extends Connection {

    public FakeConnection_1_20_R4(PacketFlow packetflow) {
        super(packetflow);
    }

    @Override
    public void setListener(@NotNull PacketListener packetlistener) {}

    @Override
    public void send(@NotNull Packet<?> packet, @Nullable PacketSendListener callbacks, boolean flush) {}

    @Override
    public boolean isConnected() {return true;}
}
