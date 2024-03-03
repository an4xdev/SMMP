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
    public void extendTypesOfService(JSONObject arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'extendTypesOfService'");
    }

    @Override
    public boolean isServiceWithServiceID(Integer arg0) {
        return services.values().stream().anyMatch(s -> s.stream().anyMatch(service -> service.getServiceID() == arg0));
    }

    @Override
    public boolean isServiceWithPlug(Integer arg0) {
        return services.values().stream().anyMatch(s -> s.stream().anyMatch(service -> service.isPlugInService(arg0)));
    }

    @Override
    public void updateLastUsedPlug(Integer arg0) {
        services.values()
                .forEach(h -> h
                        .stream()
                        .filter(f -> f.isPlugInService(arg0))
                        .findFirst()
                        .ifPresent(s -> s.updateLastUsedPlug(arg0)));
    }

    @Override
    public ArrayList<Plug> getAllPlugs() {
        // TODO: change in API to List

        return (ArrayList<Plug>) services.values().stream()
                .flatMap(List::stream)
                .flatMap(s -> s.getAllPlugs().stream())
                .collect(Collectors.toList());

    }

}
