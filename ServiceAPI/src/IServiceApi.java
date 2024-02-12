import java.io.IOException;
import java.net.UnknownHostException;

import org.json.JSONObject;

public interface IServiceApi {

    public void start(JSONObject params) throws UnknownHostException, IOException;

    public void sendMessage(JSONObject message);

    public JSONObject receivceResponse(JSONObject identifier);

}
