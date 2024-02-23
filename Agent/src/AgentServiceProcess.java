import org.json.JSONObject;

public class AgentServiceProcess extends Thread implements IAgentServiceProcess {

    @Override
    public void startService(JSONObject arg0) {
        // TODO: sets parameters

        // run thread
        this.start();
    }

    @Override
    public void run() {

    }

}
