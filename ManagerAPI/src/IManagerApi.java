import org.json.JSONObject;

public interface IManagerApi {
    public void start();

    public void sendData(JSONObject message);

    public JSONObject receiveDataToProcess();

    public String UUIDGenerator();
}
