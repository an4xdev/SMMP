import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;

public class ServiceList extends AbstractServices<Integer, Integer, Plug> {

    private HashMap<String, ArrayList<Service>> services;

    public ServiceList() {
        services = new HashMap<>();
    }

    @Override
    public List<Plug> getAllPlugs() {
        return services.values().stream()
                .flatMap(List::stream)
                .flatMap(s -> s.getAllPlugs().stream())
                .collect(Collectors.toList());

    }

    @Override
    public void updateLastUsedPlug(Integer plugIdentifier) {
        services.values()
                .forEach(h -> h
                        .stream()
                        .filter(f -> f.isPlugInService(
                                plugIdentifier))
                        .findFirst()
                        .ifPresent(s -> s.updateLastUsedPlug(plugIdentifier)));
    }

    @Override
    public boolean isServiceWithPlug(Integer plugIdentifier) {
        return services.values().stream().anyMatch(s -> s.stream().anyMatch(service -> service.isPlugInService(
                plugIdentifier)));
    }

    @Override
    public boolean isServiceWithServiceID(Integer serviceID) {

        return services.values().stream()
                .anyMatch(s -> s.stream().anyMatch(service -> service.getServiceID() == serviceID));

    }

    @Override
    public void extendTypesOfService(JSONObject typesOfServices) {
        // TODO: do something with types of Plugs, some Services may have only out or only in, some  Services may have in and out

        for (String serviceType : typesOfServices.keySet()) {
            JSONObject service = typesOfServices.getJSONObject(serviceType);
            String name = service.getString("name");
            services.put(name, new ArrayList<Service>());
        }

    }

}
