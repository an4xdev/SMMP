import java.util.concurrent.Future;

import org.json.JSONObject;

public interface IServiceApi {

    /**
    * Staring protocole implementation:
    * 1. Connecting to Agent, if fails close application
    * 2. Initialize out plug
    * 3. Initialize in plugs if needed
    * 4. Send response to Agent
    * @param params {@code JSONObject} with data retreived from args
    */
    public void start(JSONObject params);

    /**
    * Add message to send
    * @param message {@code JSONObject} with data
    */
    public void sendMessage(JSONObject message);

    /**
    * Receive message with specific messageID as String
    * @param messageID messageID as String
    * @return {@code Future<JSONObject>} as message
    */
    public Future<JSONObject> receivceResponse(String messageID);

    /**
    * Receive message with data to process
    * @return {@code Future<JSONObject>} as message
    */
    public Future<JSONObject> receiveDataToProcess();

    /**
    * UUID Generator thats generate unique indentifier based on date time
    * @return indentifier as {@code String}
    */
    public String UUIDGenerator();
}
