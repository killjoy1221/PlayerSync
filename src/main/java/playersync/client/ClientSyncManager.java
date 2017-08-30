package playersync.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import playersync.client.api.ChannelHandler;
import playersync.client.api.PacketSerializer;
import playersync.client.api.SettingsHandler;
import playersync.client.api.SyncManager;
import playersync.client.data.CClientData;
import playersync.client.data.CRegisterData;
import playersync.client.data.SChannelData;
import playersync.client.data.SHelloData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public class ClientSyncManager implements SyncManager {

    public static final String CHANNEL = "pSync";
    public static Logger logger = LogManager.getLogger("PlayerSync");

    @Nullable
    private NetHandlerPlayClient client;
    @Nullable
    private ClientDataHandler dataHandler;

    private Set<String> channels = Sets.newHashSet();
    private Map<String, PacketSerializer<?>> channelSerializers = Maps.newHashMap();
    private Map<String, ChannelHandler<?>> channelHandlers = Maps.newHashMap();
    private Map<String, Object> channelDefaults = Maps.newHashMap();
    private Map<String, SettingsHandler> settings = Maps.newHashMap();

    private BiMap<Integer, Class<? extends Data<?>>> dataPackets = HashBiMap.create();

    public ClientSyncManager() {
        dataPackets.put(0, SHelloData.class);
        dataPackets.put(1, CRegisterData.class);
        dataPackets.put(2, SChannelData.class);
        dataPackets.put(3, CClientData.class);
    }

    public void sendData(Data<? extends IServerDataHandler> data) {

        if (client != null) {
            try {
                int index = dataPackets.inverse().getOrDefault(data.getClass(), -1);
                checkArgument(index >= 0, "%s is not a registered data packet.", data.getClass());

                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeByte(index);
                data.write(buffer);

                this.client.sendPacket(new CPacketCustomPayload(CHANNEL, buffer));
            } catch (Exception e) {
                logger.warn("Unable to send packet.", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void receiveData(PacketBuffer buffer) {
        try {
            if (this.dataHandler != null) {
                int index = buffer.readByte();
                Class<? extends Data<?>> dataClass = dataPackets.get(index);


                checkNotNull(dataClass, "Unknown data packet ID: %s", index);

                Data<IClientDataHandler> data = (Data<IClientDataHandler>) dataClass.newInstance();

                data.read(buffer);

                if (buffer.isReadable()) {
                    throw new IOException(dataClass + " did not read the entire packet.");
                }

                data.process(this.dataHandler);
            }
        } catch (Exception e) {
            logger.warn("Unable to process packet.", e);
        }

    }

    @Override
    public <T> void sendPacket(String chan, T packet) {
        if (this.client != null && this.client.getNetworkManager().isChannelOpen()) {

            checkArgument(this.channels.contains(chan), "There is no channel registered for {}", chan);
            if (!this.channels.contains(chan))
                throw new IllegalArgumentException();

            PacketSerializer<T> serializer = (PacketSerializer<T>) this.channelSerializers.get(chan);
            try {
                PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                // this will be sent to clients exactly
                serializer.serialize(packet, buffer);

                sendData(new CClientData(chan, buffer));
            } catch (IOException e) {
                logger.warn(e);
            }
        }
    }

    @Override
    public <T> void register(String channel, PacketSerializer<T> serializer, ChannelHandler<T> handler, T def) {
        checkNotNull(channel, "channel cannot be null.");
        checkNotNull(serializer, "serializer cannot be null.");
        checkNotNull(handler, "handler cannot be null.");
        checkNotNull(def, "default cannot be null.");

        this.channels.add(channel);
        this.channelDefaults.put(channel, def);
        this.channelHandlers.put(channel, handler);
        this.channelSerializers.put(channel, serializer);
    }

    @Override
    public void registerConfig(String id, SettingsHandler handler) {
        checkNotNull(id, "config id cannot be null");
        checkNotNull(handler, "handler cannot be null");

        this.settings.put(id, handler);
    }

    public void setClient(@Nonnull NetHandlerPlayClient client) {
        this.client = client;
        this.dataHandler = new ClientDataHandler(this, client);
    }

    public Set<String> getChannels() {
        return channels;
    }

    public Map<String, SettingsHandler> getSettings() {
        return settings;
    }

    public Map<String, Object> getChannelDefaults() {
        return channelDefaults;
    }

    public <T> PacketSerializer<T> getChannelSerializer(String chan) {
        return (PacketSerializer<T>) this.channelSerializers.get(chan);
    }

    public <T> ChannelHandler<T> getChannelHandler(String chan) {
        return (ChannelHandler<T>) this.channelHandlers.get(chan);
    }
}
