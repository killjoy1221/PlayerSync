package playersync.client;

import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public interface Data<Handler> {

    void read(PacketBuffer buffer) throws IOException;

    void write(PacketBuffer buffer) throws IOException;

    void process(Handler handler);
}
