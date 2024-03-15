import java.util.List;

import org.json.JSONObject;

/**
 * Class that respresents connection of Manager to Agent. Class contains {@code List<JSONObject>} of requests that needs be send to Agent or Service.
 * @param S - type of Agent identifier.
 */
public abstract class AbstractManagerToAgentConnection<S> implements Runnable {
    protected S agentID;

    protected List<JSONObject> responses;

    /**
     * Contructor that provides list of responses from Agent and Services to Manager.
     * @param responses - list of responses.
     */
    public AbstractManagerToAgentConnection(List<JSONObject> responses) {
        this.responses = responses;
    }

    /**
     * Add message that will be send to specific Agent or Service that is connected to this Agent.
     * @param message in {@code JSONObject} type.
     */
    public abstract void sendMessage(JSONObject message);

    public synchronized List<JSONObject> getResponses() {
        return responses;
    }

}
