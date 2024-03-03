import java.util.ArrayList;

/**
 * Interface for implemening checking activity of plugs.
 * 
 * @param T - type of Plug.
 */
public interface IManagerCheckPlugsActivity<T> {
    /**
     * Method to pass reference to list of plugs.
     * 
     * @param plugs
     */
    public void checkPlugsActivity(ArrayList<T> plugs);
}
