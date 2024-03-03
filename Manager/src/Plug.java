import java.time.LocalDateTime;

public class Plug {

    private boolean isConnected;
    private boolean isOut;
    private int plugPort;

    private LocalDateTime lastUsedPlug;

    public Plug(boolean isConnected, boolean isOut, int plugPort) {
        this.isConnected = isConnected;
        this.isOut = isOut;
        this.plugPort = plugPort;
        this.lastUsedPlug = LocalDateTime.now();
    }

    public void changeStateOfPlug() {
        isConnected = !isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isOut() {
        return isOut;
    }

    public int getPlugPort() {
        return plugPort;
    }

    public LocalDateTime getLastUsedPlug() {
        return lastUsedPlug;
    }

    public void updateLastUsedPlug() {
        lastUsedPlug = LocalDateTime.now();
    }

}
