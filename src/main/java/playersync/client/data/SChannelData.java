package playersync.client.data;

import net.minecraft.network.PacketBuffer;
import playersync.client.Data;
import playersync.client.IClientDataHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SChannelData implements Data<IClientDataHandler> {

    private String channel;
    private List<PlayerData> data;

    public SChannelData() {
    }

//    public SChannelData(String channel, UUID uuid, byte[] data) {
//        this.channel = channel;
//        this.data.add(new PlayerData(uuid, data));
//    }
//
//    public SChannelData(String channel, Map<UUID, byte[]> data) {
//        this.channel = channel;
//        for (Map.Entry<UUID, byte[]> e : data.entrySet()) {
//            this.data.add(new PlayerData(e.getKey(), e.getValue()));
//        }
//    }

    @Override
    public void write(@Nonnull PacketBuffer buf) {
        buf.writeString(channel);
        buf.writeVarInt(data.size());
        for (PlayerData player : data) {
            buf.writeUniqueId(player.id);
            buf.writeVarInt(player.data.readableBytes());
            buf.writeBytes(player.data);
        }
    }

    @Override
    public void read(@Nonnull PacketBuffer buf) throws IOException {
        this.channel = buf.readString(50);
        int size = buf.readVarInt();
        data = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            UUID uuid = buf.readUniqueId();
            int length = buf.readVarInt();
            PacketBuffer buffer = new PacketBuffer(buf.readBytes(length));

            data.add(new PlayerData(uuid, buffer));
        }
    }

    @Override
    public void process(@Nonnull IClientDataHandler handler) {
        handler.handleChannelData(this);
    }

    public String getChannel() {
        return channel;
    }

    public List<PlayerData> getData() {
        return data;
    }

    public static class PlayerData {

        private final UUID id;
        private final PacketBuffer data;

        private PlayerData(UUID id, PacketBuffer data) {
            this.id = id;
            this.data = data;
        }

        public UUID getId() {
            return id;
        }

        public PacketBuffer getData() {
            return data;
        }
    }
}
