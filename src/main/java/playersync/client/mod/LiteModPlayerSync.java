package playersync.client.mod;

import com.google.common.collect.ImmutableList;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mumfrey.liteloader.JoinGameListener;
import com.mumfrey.liteloader.PluginChannelListener;
import com.mumfrey.liteloader.Priority;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketJoinGame;
import playersync.client.ClientSyncManager;
import playersync.client.api.PlayerSync;

import java.io.File;
import java.util.List;

@Priority(500)
public class LiteModPlayerSync implements JoinGameListener, PluginChannelListener{

    private ClientSyncManager client;

    @Override
    public String getName() {
        return "PlayerSync";
    }

    @Override
    public String getVersion() {
        return "@VERSION@";
    }

    @Override
    public void init(File configPath) {
        this.client = new ClientSyncManager();
        PlayerSync.setManager(this.client);
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    @Override
    public void onJoinGame(INetHandler netHandler, SPacketJoinGame joinGamePacket, ServerData serverData, RealmsServer realmsServer) {
        this.client.setClient((NetHandlerPlayClient) netHandler);
    }

    @Override
    public List<String> getChannels() {
        return ImmutableList.of(ClientSyncManager.CHANNEL);
    }

    @Override
    public void onCustomPayload(String channel, PacketBuffer data) {
        if (ClientSyncManager.CHANNEL.equals(channel))
            this.client.receiveData(new PacketBuffer(data.copy()));
    }

}
