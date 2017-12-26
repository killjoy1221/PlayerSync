package playersync.client;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Maps;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.nbt.NBTTagCompound;
import playersync.client.api.ChannelHandler;
import playersync.client.api.PacketSerializer;
import playersync.client.api.SettingsHandler;
import playersync.client.data.CRegisterData;
import playersync.client.data.SChannelData;
import playersync.client.data.SHelloData;
import playersync.client.data.SSettingsData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;

public class ClientDataHandler implements IClientDataHandler {

    private static final int VERSION = 4;

    private final ClientSyncManager manager;
    private final NetHandlerPlayClient client;

    ClientDataHandler(ClientSyncManager manager, NetHandlerPlayClient client) {
        this.manager = manager;
        this.client = client;
    }

    @Override
    public void handleSettingsData(SSettingsData data) {
        String mod = data.getMod();
        NBTTagCompound tag = data.getView();
        SettingsHandler handler = manager.getSettings().get(mod);
        if (handler != null) {
            handler.settingsReceived(mod, tag);
        } else {
            ClientSyncManager.logger.warn("Received mod config for unregistered handler ({}): {}", mod, tag);
        }
    }

    @Override
    public void handleChannelData(SChannelData channelData) {

        String chan = channelData.getChannel();
        checkArgument(manager.getChannels().contains(chan), "Got packet for unknown channel %s. Ignoring", chan);

        PacketSerializer<Object> serializer = manager.getChannelSerializer(chan);
        ChannelHandler<Object> handler = manager.getChannelHandler(chan);

        Map<UUID, Object> messages = Maps.newHashMap();

        for (SChannelData.PlayerData playerData : channelData.getData()) {
            UUID uuid = playerData.getId();
            try {
                messages.put(uuid, serializer.deserialize(playerData.getData()));
            } catch (IOException e) {
                ClientSyncManager.logger.warn("Exception while deserializing data from player {} and channel {}", chan, uuid, e);
            }
        }

        for (Map.Entry<UUID, Object> e : messages.entrySet()) {
            handler.handle(chan, e.getKey(), e.getValue());
        }

    }

    @Override
    public void handleHelloData(SHelloData helloData) {
        if (helloData.getVersion() != VERSION) {
            ClientSyncManager.logger.warn("Server has incompatible plugin version. server: {} client: {} ", helloData.getVersion(), VERSION);
            return;
        }
        sendHelloPacket(client);
        ClientSyncManager.logger.info("Servpai noticed me!");
        for (Map.Entry<String, ?> a : manager.getChannelDefaults().entrySet()) {
            manager.sendPacket(a.getKey(), a.getValue());
        }
    }

    private void sendHelloPacket(@Nonnull NetHandlerPlayClient client) {

        Set<String> channels = manager.getChannels();
        Set<String> settings = manager.getSettings().keySet();
        manager.sendData(new CRegisterData(4, channels, settings));
    }


}
