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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extendTypesOfService'");
    }

}
