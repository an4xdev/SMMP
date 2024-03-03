import java.time.LocalDateTime;
import java.util.ArrayList;

public class Service extends AbstractService<Integer, Integer, Plug> {

    private ArrayList<Plug> plugs;
    private LocalDateTime lastUsedService;

    public Service(Integer serviceID) {
        super(serviceID);
        plugs = new ArrayList<>();
    }

    @Override
    public void changeStateOfPlug(Integer arg0) {
        plugs.stream().filter(p -> p.getPlugPort() == arg0).findFirst().ifPresent(c -> c.changeStateOfPlug());
    }

    @Override
    public ArrayList<Plug> getAllPlugs() {
        return plugs;
    }

    @Override
    public boolean isPlugInService(Integer arg0) {
        return plugs.stream().anyMatch(p -> p.getPlugPort() == arg0);
    }

    @Override
    public void updateLastUsedPlug(Integer arg0) {
        plugs.stream().filter(p -> p.getPlugPort() == arg0).findFirst().ifPresent(f -> {
            f.updateLastUsedPlug();
            updateServiceLastUsedTime();
        });
    }

    private void updateServiceLastUsedTime() {
        lastUsedService = LocalDateTime.now();
    }

}
