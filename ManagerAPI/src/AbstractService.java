import java.util.List;

/**
 * Class that contains informations about Service and it's plugs.
 * 
 * @param S - type of Agent identifier.
 * @param T - type of Service identifier.
 * @param K - type of Plug identifier.
 * @param L - type of Plug implementation.
 */

public abstract class AbstractService<S, T, K, L> {

    private T serviceID;

    private S agentID;

    /**
     * Contructor that require Service identifier in {@code T} type.
     * 
     * @param serviceID in {@code T} type
     */
    public AbstractService(T serviceID, S agentID) {
        this.serviceID = serviceID;
        this.agentID = agentID;
    }

    /**
     * Update last used time of activity of plug.
     * 
     * @param plugIdentifier in {@code K} type.
     */
    public abstract void updateLastUsedPlug(K plugIdentifier);

    /**
     * Check if Service contains plug with specific identifier.
     * 
     * @param plugIdentifier in {@code K} type.
     * @return - {@code true} if contains, otherwise {@code false}.
     */
    public abstract boolean isPlugInService(K plugIdentifier);

    /**
     * Getter of Service identifier.
     * 
     * @return identifier in {@code T} type.
     */
    public T getServiceID() {
        return serviceID;
    }

    /**
     * Getter of Agent identifier.
     * @return identifier in {@code S} type.
     */
    public S getAgentID() {
        return agentID;
    }

    /**
     * Change status of plug ex. connected/not connected, running/not running.
     * 
     * @param plugIdentifier in {@code K} type.
     */
    public abstract void changeStateOfPlug(K plugIdentifier);

    /**
     * Return all plugs from that Service.
     * 
     * @return list in {@code List<L>} type.
     */
    public abstract List<L> getAllPlugs();

}
