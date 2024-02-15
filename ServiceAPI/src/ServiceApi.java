import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

public class ServiceApi extends Thread implements IServiceApi {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> sendList;
    private LinkedList<JSONObject> receiveList;

    private HashMap<String, LinkedBlockingQueue<ServiceToService>> connections;
    private HashMap<String, Integer> plugs;

    private Socket socketToAgent;
    private BufferedReader readerFromAgent;
    private PrintWriter writerToAgent;

    public ServiceApi() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        sendList = (LinkedList<JSONObject>) Collections
                .synchronizedList(new LinkedList<JSONObject>());
        receiveList = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        connections = new HashMap<>();
        plugs = new HashMap<>();
    }

    @Override
    public void start(JSONObject params) throws UnknownHostException, IOException {
        // connect to ServerSocket in Agent via connectToAgent
        connectToAgent();
        // initialize map of plugs

        JSONObject plugsJSON = params.getJSONObject("plugs");

        for (String serviceType : plugsJSON.keySet()) {
            plugs.put(serviceType, plugsJSON.getInt(serviceType));
            connections.put(serviceType, new LinkedBlockingQueue<>());
        }

        // start run method
        this.start();

        // TODO: send message about that service was started via sendMessage
        sendMessage(null);
    }

    private void connectToAgent() throws UnknownHostException, IOException {
        // connect to ServerSocket in Agent
        socketToAgent = new Socket("address", 1234);
        readerFromAgent = new BufferedReader(new InputStreamReader(socketToAgent.getInputStream()));
        writerToAgent = new PrintWriter(socketToAgent.getOutputStream());
    }

    @Override
    public void sendMessage(JSONObject message) {
        sendList.add(message);
    }

    public Future<JSONObject> receivceResponse(JSONObject identifier) {
        return executorService.submit(new Callable<JSONObject>() {
            @Override
            public JSONObject call() {
                JSONObject temp = receiveList.poll();
                // TODO: change to checking if identifier is the same
                // if not add to receiveList
                return temp;
            }
        });

    }

    private void connect(String typeOfService) {
        // TODO: send request to mamanger
        sendMessage(null);
        // receive message
        Future<JSONObject> response = receivceResponse(null);
        // connect
        LinkedBlockingQueue<ServiceToService> connectionsQueue = connections.get(typeOfService);

        connectionsQueue.offer(null);
        // TODO: send confirmation
        sendMessage(null);
    }

    private void disconnect(int plug) {
        connections.values().stream()
                .flatMap(LinkedBlockingQueue<ServiceToService>::stream)
                .filter(r -> r.getPlug() == plug).findFirst().ifPresent(r -> r.disconnect());

        connections.values().forEach(l -> l.removeIf(r -> r.getPlug() == plug));
    }

    private void close() throws InterruptedException {

        // close all runnable connections and send messages
        connections.values().forEach(l -> l.forEach(r -> r.disconnect()));

        // TODO: send message about closing
        sendMessage(null);

        // wait 5 seconds
        Thread.sleep(5000);

        // close application
        System.exit(0);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String response = readerFromAgent.readLine();
                    // TODO: check if request is from Manager and process
                    receiveList.add(new JSONObject(response));
                    Thread.sleep(10);
                } catch (IOException e) {
                    // TODO: Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO: Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (sendList.size() != 0) {
                    JSONObject temp = sendList.poll();
                    // TODO:: add checking if message is to Manager
                    writerToAgent.println(temp.toString());
                    writerToAgent.flush();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO: Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

}
