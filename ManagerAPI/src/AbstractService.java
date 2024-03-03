import java.util.ArrayList;

/**
 * Class that contains informations about Service and it's plugs.
 * 
 * @param T - type of plug identifier.
 * @param K - type of Service identifier.
 * @param L - type of Plug implementation.
 */

public abstract class AbstractService<T, K, L> {

    private K serviceID;

    /**
     * Contructor that require Service identifier in {@code K} type.
     * 
     * @param serviceID
     */
    public AbstractService(K serviceID) {
        this.serviceID = serviceID;
    }

    /**
     * Update last used time of activity of plug.
     * 
     * @param plugIdentifier
     */
    public abstract void updateLastUsedPlug(T plugIdentifier);

    /**
     * Check if Service contains plug with specific identifier.
     * 
     * @param plugIdentifier
     * @return - {@code true} if contains, otherwise {@code false}.
     */
    public abstract boolean isPlugInService(T plugIdentifier);

    /**
     * Getter of Service identifier.
     * 
     * @return identifier in {@code K} type.
     */
    public K getServiceID() {
        return serviceID;
    }

    /**
     * Change status of plug ex. connected/not connected, running/not running.
     * 
     * @param plugIdentifier
     */
    public abstract void changeStateOfPlug(T plugIdentifier);

    /**
     * Return all plugs from that Service.
     * 
     * @return list in {@code ArrayList<L>} type.
     */
    public abstract ArrayList<L> getAllPlugs();

}
