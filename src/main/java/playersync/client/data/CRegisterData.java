package playersync.client.data;

import com.google.common.collect.Sets;
import net.minecraft.network.PacketBuffer;
import playersync.client.Data;
import playersync.client.IServerDataHandler;

import java.util.Set;
import javax.annotation.Nonnull;

public class CRegisterData implements Data<IServerDataHandler> {

    private int version;
    private Set<String> channels;
    private Set<String> settings;

    public CRegisterData() {
    }

    public CRegisterData(int version, Set<String> channels, Set<String> settings) {
        this.version = version;
        this.channels = channels;
        this.settings = settings;
    }

    @Override
    public void read(@Nonnull PacketBuffer buf) {
        this.version = buf.readVarInt();
        this.channels = readStrings(buf);
        this.settings = readStrings(buf);
    }

    @Override
    public void write(@Nonnull PacketBuffer buf) {
        buf.writeVarInt(this.version);
        writeStrings(buf, this.channels);
//        writeStrings(buf, this.settings);
    }

    @Override
    public void process(@Nonnull IServerDataHandler handler) {
        handler.handleRegisterData(this);
    }

    private static Set<String> readStrings(PacketBuffer input) {
        Set<String> strings = Sets.newHashSet();
        int size = input.readVarInt();
        for (int i = 0; i < size; i++) {
            strings.add(input.readString(50));
        }
        return strings;
    }

    private static void writeStrings(PacketBuffer buf, Set<String> strings) {
        buf.writeVarInt(strings.size());
        for (String s : strings) {
            buf.writeString(s);
        }
    }

    public int getVersion() {
        return version;
    }

    public Set<String> getChannels() {
        return channels;
    }

    public Set<String> getSettings() {
        return settings;
    }
}
