package playersync.client;

import playersync.client.data.CClientData;
import playersync.client.data.CRegisterData;

public interface IServerDataHandler {

    void handleRegisterData(CRegisterData sRegisterData);

    void handleClientData(CClientData sClientData);

}
