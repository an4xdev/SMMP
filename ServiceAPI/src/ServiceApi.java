import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public class ServiceApi extends Thread implements IServiceApi {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> sendList;
    private LinkedList<JSONObject> receiveList;

    private HashMap<String, Queue<Runnable>> connections;
    private HashMap<String, Integer> plugs;

    public ServiceApi() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        sendList = (LinkedList<JSONObject>) Collections
                .synchronizedList(new LinkedList<JSONObject>());
        receiveList = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        connections = new HashMap<>();
        plugs = new HashMap<>();
    }

    @Override
    public void start(String params) {
        // initialize socket and initialize map of plugs
        // connect to ServerSocket in Agent via connectToAgent
        // start run method
        // send message about that service was started via sendMessage
    }

    private void connectToAgent() {
        // connect to ServerSocket in Agent
    }

    @Override
    public JSONObject sendMessage(JSONObject message, boolean receive) {
        // check if connected to specific service(get queue via name)
        // if is poll from queue, process and send response and maybe get response (flag?)
        sendList.add(message);

        // if flag is set create new callable thats check receiveList if ID of message is the same, poll and return

        try {
            JSONObject foo = executorService.submit(new Callable<JSONObject>() {
                @Override
                public JSONObject call() {
                    JSONObject foo = new JSONObject();
                    foo.put("foo", 5);
                    return foo;
                }
            }).get();

            return foo;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        // if flag isn't set return null

        return null;
    }

    private void connect(String typeOfService) {
        // send request to mamanger
        // receive message
        // connect
        // send confirmation
    }

    private void disconnect(int plug) {
        // go by all runnable and check where plug is the same as parameter, interrupt and send message
    }

    private void close() {
        // close all runnable connections and send messages, send message about closing and close application
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (sendList.size() != 0) {
                    // send
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // receive to receiveList
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
