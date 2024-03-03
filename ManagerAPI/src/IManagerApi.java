import org.json.JSONObject;

public interface IManagerApi {
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
    public void sendData(JSONObject message);

    /**
     * Receive data to process.
     * 
     * @return {@code JSONObject} with data.
     */
    public JSONObject receiveDataToProcess();

    /**
     * Generate time based UUID.
     * 
     * @return identifier in {@code String}.
     */
    public String UUIDGenerator();
}
