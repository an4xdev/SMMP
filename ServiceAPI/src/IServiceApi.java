import org.json.JSONObject;

public interface IServiceApi {

    public void start(String params);

    public JSONObject sendMessage(JSONObject message, boolean receive);

}
