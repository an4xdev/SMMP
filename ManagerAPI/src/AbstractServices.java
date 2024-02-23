import java.util.ArrayList;

import org.json.JSONObject;

public abstract class AbstractServices {
    public abstract void extendTypesOfService(JSONObject typesOfServices);

    public abstract <T> boolean isServiceWithPlug(T plug);

    public abstract <T> boolean isServiceWithServiceID(T serviceID);

    public abstract <T> void updateLastUsedPlug(T plug);

    public abstract <T> ArrayList<T> getAllPlugs();

}
