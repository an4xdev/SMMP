import org.json.JSONObject;

public interface IAgentApi {
    public void start(JSONObject args);

    public void sendMessage(JSONObject message);

    public JSONObject receiveMessage();

    public String UUIDGenereator();
}
