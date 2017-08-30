package playersync.client;

import playersync.client.data.SChannelData;
import playersync.client.data.SHelloData;
import playersync.client.data.SSettingsData;

public interface IClientDataHandler {

    void handleSettingsData(SSettingsData settingsData);

    void handleChannelData(SChannelData channelData);

    void handleHelloData(SHelloData helloData);

}
