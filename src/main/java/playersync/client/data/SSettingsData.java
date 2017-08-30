package playersync.client.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import playersync.client.Data;
import playersync.client.IClientDataHandler;

import java.io.IOException;
import javax.annotation.Nonnull;

public class SSettingsData implements Data<IClientDataHandler> {

    private String mod;
    private NBTTagCompound view;

    public SSettingsData(String mod, NBTTagCompound data) {
        this.mod = mod;
        this.view = data;
    }

    @Override
    public void read(@Nonnull PacketBuffer buf) throws IOException {
        mod = buf.readString(32);
        view = buf.readCompoundTag();
    }

    @Override
    public void write(@Nonnull PacketBuffer buf) throws IOException {
        buf.writeString(mod);
        buf.writeCompoundTag(view);
    }

    @Override
    public void process(@Nonnull IClientDataHandler handler) {
        handler.handleSettingsData(this);
    }

    public String getMod() {
        return mod;
    }

    public NBTTagCompound getView() {
        return view;
    }
}
