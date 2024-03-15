import java.util.concurrent.Future;

import org.json.JSONObject;

/**
 * Interface to implement that cointains functions that are required, so Manager can work properly.
 * @param S - type of Agent identifier. 
 */
public interface IManagerApi<S> {
    /**
     * Start Manager with data needed for setup.
     * 
     * @param args - args in {@code JSONObject}.
     */
    public void start(JSONObject args);

    /**
     * Send message to another Service.
     * 
     * @param message - message in {@code JSONObject}.
     */
    public void sendData(JSONObject message, S agentID);

    /**
     * Receive data to process.
     * 
     * @return {@code Future<JSONObject>} with data.
     */
    public Future<JSONObject> receiveDataToProcess();

    /**
     * Generate time based UUID.
     * 
     * @return identifier in {@code String}.
     */
    public String UUIDGenerator();
}
