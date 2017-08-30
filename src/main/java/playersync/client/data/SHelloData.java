package playersync.client.data;

import net.minecraft.network.PacketBuffer;
import playersync.client.Data;
import playersync.client.IClientDataHandler;

import javax.annotation.Nonnull;

public class SHelloData implements Data<IClientDataHandler> {

    private int version;

    public SHelloData() {}

    public SHelloData(int version) {
        this.version = version;
    }

    @Override
    public void read(@Nonnull PacketBuffer buf) {
        version = buf.readVarInt();
    }

    @Override
    public void write(@Nonnull PacketBuffer buf) {
        buf.writeVarInt(version);
    }

    @Override
    public void process(@Nonnull IClientDataHandler handler) {
        handler.handleHelloData(this);
    }

    public int getVersion() {
        return version;
    }
}
