import java.util.List;

import org.json.JSONObject;

/**
 * Class thats will be containing informations about all Services that were or are running.
 * 
 * @param T - type of Service identifier.
 * @param K - type of Plug identifier.
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
     * @param plugIdentifier in {@code K} type.
     * @return {@code true} if plug is, otherwise {@code false}.
     */
    public abstract boolean isServiceWithPlug(K plugIdentifier);

    /**
     * Check if Service with identifier is in all Services
     * 
     * @param serviceID in {@code T} type.
     * @return {@code true} if Service is, otherwise {@code false}.
     */
    public abstract boolean isServiceWithServiceID(T serviceID);

    /**
     * Update last used time of activity of plug and also of Service
     * 
     * @param plugIdentifier in {@code K} type. 
     */
    public abstract void updateLastUsedPlug(K plugIdentifier);

    /**
     * Return all plugs. Needed for checking plugs activity.
     * 
     * @return list of all plugs in {@code List<L>} type.
     */
    public abstract List<L> getAllPlugs();

}
