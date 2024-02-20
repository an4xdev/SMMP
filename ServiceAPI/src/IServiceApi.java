import java.util.concurrent.Future;

import org.json.JSONObject;

public interface IServiceApi {

    /**
     * Staring protocole implementation:
     * <ol>
     * <li>Connecting to Agent, if fails send data to Manager and close application.
     * <li>Initialize out plug.
     * <li>Initialize in plugs if needed.
     * <li>Send response to Agent.
     * </ol>
     *
     * @param params {@code JSONObject} with data retreived from args.
     */
    public void start(JSONObject params);

    /**
     * Add message to send.
     *
     * @param message {@code JSONObject} with data
     */
    public void sendMessage(JSONObject message);

    /**
     * Receive message with specific messageID as String.
     *
     * @param messageID messageID as String
     * @return {@code Future<JSONObject>} as message
     */
    public Future<JSONObject> receivceResponse(String messageID);

    /**
     * Receive message with data to process.
     *
     * @return {@code Future<JSONObject>} as message
     */
    public Future<JSONObject> receiveDataToProcess();

    /**
     * UUID Generator thats generate unique indentifier based on date time.
     * <p>
     * see: <a href="https://github.com/cowtowncoder/java-uuid-generator">Java UUID generator</a> 
     *
     * @return indentifier as {@code String}
     */
    public String UUIDGenerator();
}
