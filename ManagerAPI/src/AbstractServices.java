import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Class thats will be containing informations about all Services that were or
 * are running.
 * 
 * @param T - type of plug identifier.
 * @param K - type of service identifier.
 * @param L - type of Plug implementation.
 */
public abstract class AbstractServices<T, K, L> {

    /**
     * When new Agent connected it provides repository of Services that could run.
     * 
     * @param typesOfServices - object that contains Services in {@code JSONObject}.
     */
    public abstract void extendTypesOfService(JSONObject typesOfServices);

    /**
     * Check if Service with specific plug is in all Services
     * 
     * @param plugIdentifier
     * @return {@code true} if plug is, otherwise {@code false}.
     */
    public abstract boolean isServiceWithPlug(T plugIdentifier);

    /**
     * Check if Service with identifier is in all Services
     * 
     * @param serviceID
     * @return {@code true} if Service is, otherwise {@code false}.
     */
    public abstract boolean isServiceWithServiceID(K serviceID);

    /**
     * Update last used time of activity of plug and also of Service
     * 
     * @param plugIdentifier
     */
    public abstract void updateLastUsedPlug(T plugIdentifier);

    /**
     * Return all plugs. Needed for checking plugs activity.
     * 
     * @return list of all plugs in {@code ArrayList<L>} type.
     */
    public abstract ArrayList<L> getAllPlugs();

}
