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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

public class ServiceApi extends Thread implements IServiceApi {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> sendList;
    private LinkedList<JSONObject> receiveList;

    private HashMap<String, LinkedBlockingQueue<Runnable>> connections;
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
        }

        // start run method
        this.start();

        // send message about that service was started via sendMessage
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

    // TODO:: change to Future<JSONObject> and process exceptions in application?
    public JSONObject receivceResponse(JSONObject identifier) {
        JSONObject response = null;
        try {
            response = executorService.submit(new Callable<JSONObject>() {
                @Override
                public JSONObject call() {
                    JSONObject foo = new JSONObject();
                    foo.put("foo", 5);
                    return foo;
                }
            }).get();

        } catch (InterruptedException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }

        return response;
    }

    private void connect(String typeOfService) {
        // send request to mamanger
        sendMessage(null);
        // receive message
        JSONObject response = receivceResponse(null);
        // connect
        LinkedBlockingQueue<Runnable> connectionsQueue = connections.getOrDefault(typeOfService,
                new LinkedBlockingQueue<Runnable>());

        // TODO:: add class thats implements Runnable where is has plug number, once are initialized sockets, have method that send and recevice data, and have busy flag
        connectionsQueue.offer(null);
        // send confirmation
        sendMessage(null);
    }

    private void disconnect(int plug) {

        // TODO:: change to get plug and call close method them remove?

        // go by all runnable and check where plug is the same as parameter, interrupt and send message
        connections.values().removeIf(b -> b.size() == 1);
    }

    private void close() {

        // TODO:: call close method

        // close all runnable connections and send messages
        connections.values().forEach(f -> System.out.println(f.size()));

        // set connections to NULL?

        // send message about closing

        sendMessage(null);

        // close application

        System.exit(0);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String response = readerFromAgent.readLine();
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
                    // check if this message is to Manager if isn't add to queue

                    // check if connected to specific service(get queue via name), if is poll from queue, process and send request

                    // if poll is empty or all threads are busy connect to Service?

                    // TODO:: add checking if message is to Manager
                    // if is to Manager send to Agent
                    writerToAgent.println(temp.toString());
                    writerToAgent.flush();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
