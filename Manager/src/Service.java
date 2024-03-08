import java.time.LocalDateTime;
import java.util.ArrayList;

public class Service extends AbstractService<Integer, Integer, Plug> {

    private ArrayList<Plug> plugs;
    private LocalDateTime lastUsedService;

    public Service(Integer serviceID) {
        super(serviceID);
        plugs = new ArrayList<>();
    }

    public LocalDateTime getLastUsedService() {
        return lastUsedService;
    }

    @Override
    public ArrayList<Plug> getAllPlugs() {
        return plugs;
    }

    private void updateServiceLastUsedTime() {
        lastUsedService = LocalDateTime.now();
    }

    @Override
    public void changeStateOfPlug(Integer plugIdentifier) {
        plugs.stream().filter(p -> p.getPlugPort() == plugIdentifier).findFirst().ifPresent(c -> c.changeStateOfPlug());
    }

    @Override
    public void updateLastUsedPlug(Integer plugIdentifier) {
        plugs.stream().filter(p -> p.getPlugPort() == plugIdentifier).findFirst().ifPresent(f -> {
            f.updateLastUsedPlug();
            updateServiceLastUsedTime();
        });
    }

    @Override
    public boolean isPlugInService(Integer plugIdentifier) {
        return plugs.stream().anyMatch(p -> p.getPlugPort() == plugIdentifier);
    }

}
