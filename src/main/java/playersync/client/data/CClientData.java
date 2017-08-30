package playersync.client.data;

import net.minecraft.network.PacketBuffer;
import playersync.client.Data;
import playersync.client.IServerDataHandler;

import java.io.IOException;
import javax.annotation.Nonnull;

public class CClientData implements Data<IServerDataHandler> {

    private String channel;
    private PacketBuffer buffer;

    public CClientData(String channel, PacketBuffer buffer) {
        this.channel = channel;
        this.buffer = buffer;
    }

    @Override
    public void read(@Nonnull PacketBuffer buf) throws IOException {
        this.channel = buf.readString(50);
        this.buffer = new PacketBuffer(buf.readBytes(buf.readableBytes()));
    }

    @Override
    public void write(@Nonnull PacketBuffer buf) {
        buf.writeString(channel);
        buf.writeByteArray(buffer.array());
    }

    @Override
    public void process(IServerDataHandler handler) {
        handler.handleClientData(this);
    }

    public PacketBuffer getBuffer() {
        return buffer;
    }

    public String getChannel() {
        return channel;
    }
}
