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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

import com.fasterxml.uuid.Generators;

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

        // send message about that service was started via sendMessage
        JSONObject startServiceResponse = new JSONObject();
        startServiceResponse.put("type", "startServiceResponse");
        startServiceResponse.put("internalMessage", true);
        startServiceResponse.put("messageID", params.getString("messageID"));
        // TODO: think about status 
        startServiceResponse.put("status", 200);
        sendMessage(startServiceResponse);
    }

    private void connectToAgent() throws UnknownHostException, IOException {
        // connect to ServerSocket in Agent
        socketToAgent = new Socket("address", 1234);
        readerFromAgent = new BufferedReader(new InputStreamReader(socketToAgent.getInputStream()));
        writerToAgent = new PrintWriter(socketToAgent.getOutputStream());
    }

    @Override
    public void sendMessage(JSONObject message) {
        if (message.getBoolean("internalMessage")) {
            // add to sendList
            sendList.add(message);
        } else {
            // check if is free runnable object that is connected to specific Service
            if (connections.get(message.getString("typeOfService")).size() < 1) {
                // TODO: change connect return type to boolean?
                connect(message.getString("typeOfService"));
            }
            sendList.add(message);
        }
    }

    public Future<JSONObject> receivceResponse(String messageID) {
        return executorService.submit(new Callable<JSONObject>() {
            @Override
            public JSONObject call() {
                JSONObject temp;
                while (true) {
                    temp = receiveList.poll();
                    if (!temp.getString("messageID").equals(messageID)) {
                        receiveList.add(temp);
                    } else {
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO: Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                return temp;
            }
        });

    }

    private void connect(String typeOfService) {
        // send request
        JSONObject connectionRequest = new JSONObject();
        connectionRequest.put("type", "connectionRequest");
        connectionRequest.put("internalMessage", true);
        String messageID = UUIDGenerator();
        connectionRequest.put("messageID", messageID);
        connectionRequest.put("sourcePlug", plugs.get(typeOfService));
        connectionRequest.put("destServiceName", typeOfService);
        sendMessage(connectionRequest);
        // receive message
        JSONObject response = null;
        try {
            response = receivceResponse(messageID).get();
        } catch (InterruptedException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }
        if (response != null) {
            // connect
            LinkedBlockingQueue<ServiceToService> connectionsQueue = connections.get(typeOfService);
            // send confirmation
            JSONObject connectionConfirmation = new JSONObject();
            connectionConfirmation.put("type", "connectionConfirmation");
            connectionConfirmation.put("internalMessage", true);
            connectionConfirmation.put("messageID", UUIDGenerator());
            // TODO: create ServiceToService and think about status
            connectionConfirmation.put("statusCode", connectionsQueue.offer(null) ? 200 : 300);
            connectionConfirmation.put("sourcePlug", plugs.get(typeOfService));
            connectionConfirmation.put("destPlug", response.getInt("destPlug"));
            sendMessage(connectionConfirmation);
        }

    }

    private void disconnect(String messageID, int plug) {
        connections.values().stream()
                .flatMap(LinkedBlockingQueue<ServiceToService>::stream)
                .filter(r -> r.getSourceServicePlug() == plug).findFirst().ifPresent(r -> r.disconnect(messageID));

        connections.values().forEach(l -> l.removeIf(r -> r.getSourceServicePlug() == plug));
    }

    private void close(String messageID) {

        // close all runnable connections and send messages
        connections.values().forEach(l -> l.forEach(r -> r.disconnect(messageID)));

        // send response
        JSONObject softShutdownResponse = new JSONObject();
        softShutdownResponse.put("type", "softShutdownResponse");
        softShutdownResponse.put("internalMessage", true);
        // TODO: change serviceID
        softShutdownResponse.put("serviceID", 10);
        softShutdownResponse.put("status", 200);
        sendMessage(softShutdownResponse);

        // wait 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }

        // close application
        System.exit(0);
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String response = readerFromAgent.readLine();
                    JSONObject responseObject = new JSONObject(response);
                    if (responseObject.getBoolean("internalMessage")) {
                        // process
                        switch (responseObject.getString("type")) {
                            case "connectionCloseRequest":
                                disconnect(responseObject.getString("messageID"), responseObject.getInt("sourcePlug"));
                                break;
                            case "softShutdownRequest":
                                close(responseObject.getString("messageID"));
                                break;
                            default:
                                break;
                        }
                    } else {
                        receiveList.add(responseObject);
                    }
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
                    JSONObject requestObject = sendList.poll();
                    if (requestObject.getBoolean("internalMessage")) {
                        writerToAgent.println(requestObject.toString());
                        writerToAgent.flush();
                    } else {
                        sendList.add(requestObject);
                    }
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

    @Override
    public String UUIDGenerator() {
        return Generators.timeBasedGenerator().generate().toString();
    }

}
