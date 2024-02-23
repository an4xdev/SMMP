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

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.uuid.Generators;

public class ServiceApi extends Thread implements IServiceApi {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> messagesList;

    private HashMap<String, LinkedBlockingQueue<ServiceToServiceOut>> connections;
    private HashMap<String, Integer> plugsOut;

    private Socket socketToAgent;
    private BufferedReader readerFromAgent;
    private PrintWriter writerToAgent;

    private int serviceID;

    public ServiceApi() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        messagesList = (LinkedList<JSONObject>) Collections
                .synchronizedList(new LinkedList<JSONObject>());
        connections = new HashMap<>();
        plugsOut = new HashMap<>();
    }

    @Override
    public void start(JSONObject params) {
        // TODO: add catching exceptions and maybe shutdown application?, maybe in params add emergency data to Manager so Service can directly report to Manager?
        // connect to ServerSocket in Agent via connectToAgent

        serviceID = params.getInt("serviceID");
        try {
            connectToAgent(params);
        } catch (IOException e) {
            System.out.println(
                    String.format(
                            "ServiceApi with ID: %d, in start() failed to connect to Agent. Sending data to Manager",
                            serviceID));
            JSONObject dataToConnectToManager = params.getJSONObject("emergencyData");
            try (Socket socketToManager = new Socket(dataToConnectToManager.getString("managerNetwork"),
                    dataToConnectToManager.getInt("managerPort"))) {
                PrintWriter writerToManager = new PrintWriter(socketToManager.getOutputStream());
                JSONObject errorMessage = new JSONObject();
                errorMessage.put("type", "startServiceResponse");
                errorMessage.put("internalMessage", true);
                errorMessage.put("messageID", params.getString("messageID"));
                errorMessage.put("serviceID", serviceID);
                errorMessage.put("success", false);
                writerToManager.println(errorMessage.toString());
                writerToManager.close();
            } catch (JSONException e1) {
                System.out.println(
                        String.format(
                                "ServiceApi with ID: %d, in start() was exception with JSON",
                                serviceID));
                e1.printStackTrace();
            } catch (UnknownHostException e1) {
                System.out.println(
                        String.format(
                                "ServiceApi with ID: %d, in start() Socket to Manager throw unknown host exception",
                                serviceID));
                e1.printStackTrace();
            } catch (IOException e1) {
                System.out.println(
                        String.format(
                                "ServiceApi with ID: %d, in start() Socket to Manager throw IO exception",
                                serviceID));
                e1.printStackTrace();
            }
            System.out.println(
                    String.format(
                            "ServiceApi with ID: %d, in start() connection to Agent failed",
                            serviceID));
            e.printStackTrace();
            System.exit(-1);
        }

        // initialize map of Out plugs
        JSONObject plugsOutJSON = params.getJSONObject("plugs").getJSONObject("out");

        for (String serviceType : plugsOutJSON.keySet()) {
            plugsOut.put(serviceType, plugsOutJSON.getInt(serviceType));
            connections.put(serviceType, new LinkedBlockingQueue<>());
        }

        // if needed initialize In plugs 
        if (params.getJSONObject("plugs").has("in")) {
            JSONObject plugsInJSON = params.getJSONObject("plugs").getJSONObject("in");

            for (String serviceType : plugsInJSON.keySet()) {
                executorService.submit(
                        new ServiceToServiceIn(plugsInJSON.getInt(serviceType), messagesList));
            }

        }

        // start run method
        this.start();

        // send message about that service was started via sendMessage
        JSONObject startServiceResponse = new JSONObject();
        startServiceResponse.put("type", "startServiceResponse");
        startServiceResponse.put("internalMessage", true);
        startServiceResponse.put("messageID", params.getString("messageID"));
        startServiceResponse.put("serviceID", serviceID);
        startServiceResponse.put("success", true);
        sendMessage(startServiceResponse);
    }

    /**
     * Method that connects to Agent.
     * 
     * @param params
     * @throws UnknownHostException
     * @throws IOException
     * 
     */
    private void connectToAgent(JSONObject params) throws UnknownHostException, IOException {
        // connect to ServerSocket in Agent
        socketToAgent = new Socket(params.getString("agentNetwork"), params.getInt("agentPort"));
        readerFromAgent = new BufferedReader(new InputStreamReader(socketToAgent.getInputStream()));
        writerToAgent = new PrintWriter(socketToAgent.getOutputStream());
    }

    @Override
    public void sendMessage(JSONObject message) {
        if (message.getBoolean("internalMessage")) {
            // add to sendList
            messagesList.add(message);
        } else {
            // check if is free runnable object that is connected to specific Service
            if (connections.get(message.getString("typeOfService")).size() < 1 || !connections.get(
                    message.getString("typeOfService")).stream().anyMatch(s -> !s.getIsBusy())) {
                connect(message.getString("typeOfService"));
            }
            messagesList.add(message);
        }
    }

    @Override
    public Future<JSONObject> receivceResponse(String messageID) {
        // retrieve from responsesList data where messageID is equal and return 
        return executorService.submit(new Callable<JSONObject>() {
            @Override
            public JSONObject call() {
                JSONObject temp;
                while (true) {
                    temp = messagesList.poll();
                    if (!temp.getString("messageID").equals(messageID)) {
                        messagesList.add(temp);
                    } else {
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println(String.format(
                                "ServiceApi with ID: %d, in receiveResponse() Thread.sleep() was interrupted",
                                serviceID));
                        e.printStackTrace();
                    }
                }

                return temp;
            }
        });

    }

    @Override
    public Future<JSONObject> receiveDataToProcess() {
        // retrieve from responsesList data where message isn't internal and type contains request and return 
        return executorService.submit(new Callable<JSONObject>() {
            @Override
            public JSONObject call() {
                JSONObject temp;
                while (true) {
                    temp = messagesList.poll();
                    if (!temp.getBoolean("internalMessage")
                            && temp.getString("type").contains("Request")) {
                        break;
                    } else {
                        messagesList.add(temp);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println(String.format(
                                "ServiceApi with ID: %d, in receiveDataToProcess() Thread.sleep() was interrupted",
                                serviceID));
                        e.printStackTrace();
                    }
                }

                return temp;
            }
        });
    }

    /**
     * Connecting to specific type of Service.
     * 
     * @param typeOfService
     */
    private void connect(String typeOfService) {
        // TODO: Service has one out plug per Service type, in Manager and Service implement adding more Out plugs?
        // send request
        JSONObject connectionRequest = new JSONObject();
        connectionRequest.put("type", "connectionRequest");
        connectionRequest.put("internalMessage", true);
        String messageID = UUIDGenerator();
        connectionRequest.put("messageID", messageID);
        connectionRequest.put("serviceID", serviceID);
        connectionRequest.put("sourcePlug", plugsOut.get(typeOfService));
        connectionRequest.put("destServiceName", typeOfService);
        sendMessage(connectionRequest);
        // receive message
        JSONObject response = null;
        try {
            response = receivceResponse(messageID).get();
        } catch (InterruptedException e) {
            System.out.println(String.format(
                    "ServiceApi with ID: %d, in connect() current thread was interrupted while waiting.", serviceID));
            e.printStackTrace();
        } catch (ExecutionException e) {
            System.out.println(String.format(
                    "ServiceApi with ID: %d, in connect() computation threw an exception.", serviceID));
            e.printStackTrace();
        }
        if (response != null) {
            // connect
            LinkedBlockingQueue<ServiceToServiceOut> connectionsQueue = connections.get(typeOfService);
            // send confirmation
            JSONObject connectionConfirmation = new JSONObject();
            connectionConfirmation.put("type", "connectionConfirmation");
            connectionConfirmation.put("internalMessage", true);
            connectionConfirmation.put("messageID", UUIDGenerator());
            // create ServiceToService
            boolean success = true;
            ServiceToServiceOut temp;
            try {
                temp = new ServiceToServiceOut(plugsOut.get(typeOfService), typeOfService,
                        messagesList, response, this);
                connectionsQueue.offer(temp);
            } catch (IOException e) {
                System.out.println(String.format(
                        "ServiceApi with ID: %d, in connect() initialization of connetion to another Service failed. Source Service plug: %d, dest Service name: %s",
                        serviceID, plugsOut.get(typeOfService), typeOfService));
                e.printStackTrace();
                success = false;
            }
            connectionConfirmation.put("success", success);
            connectionConfirmation.put("sourcePlug", plugsOut.get(typeOfService));
            connectionConfirmation.put("destPlug", response.getInt("destPlug"));
            sendMessage(connectionConfirmation);
        }

    }

    /**
     * Disconnecting plug with specific number.
     * 
     * @param messageID - to send response
     * @param plug - plug number
     */
    private void disconnect(String messageID, int plug) {
        connections.values().stream()
                .flatMap(LinkedBlockingQueue<ServiceToServiceOut>::stream)
                .filter(r -> r.getSourceServicePlug() == plug).findFirst()
                .ifPresent(r -> r.disconnect(messageID));

        connections.values().forEach(l -> l.removeIf(r -> r.getSourceServicePlug() == plug));
    }

    /**
     * Closing application when got specific request.
     * <p>
     * Closing connections, runnable when data are received from another Services.
     * 
     * @param messageID - to send response
     */
    private void close(String messageID) {

        // close all runnable connections and send messages
        connections.values().forEach(l -> l.forEach(r -> r.disconnect(messageID)));

        // send response
        JSONObject softShutdownResponse = new JSONObject();
        softShutdownResponse.put("type", "softShutdownResponse");
        softShutdownResponse.put("internalMessage", true);
        softShutdownResponse.put("messageID", messageID);
        softShutdownResponse.put("serviceID", serviceID);
        softShutdownResponse.put("success", true);
        sendMessage(softShutdownResponse);

        // wait 5 seconds
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println(String.format(
                    "ServiceApi with ID: %d, in close() Thread.sleep() was interrupted.",
                    serviceID));
            e.printStackTrace();
        }

        executorService.shutdown();

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
                    if (responseObject.getBoolean("internalMessage")
                            && responseObject.getString("type").contains("Request")) {
                        // process
                        switch (responseObject.getString("type")) {
                            case "connectionCloseRequest":
                                disconnect(responseObject.getString("messageID"), responseObject.getInt("sourcePlug"));
                                break;
                            case "softShutdownRequest":
                                close(responseObject.getString("messageID"));
                                break;
                            default:
                                System.out.println("Unknown internalMessage: " + responseObject.toString());
                                break;
                        }
                    } else {
                        messagesList.add(responseObject);
                    }
                    Thread.sleep(10);
                } catch (IOException e) {
                    System.out.println(String.format(
                            "ServiceApi with ID: %d, in run() BufferedReader from Agent throw IO exception.",
                            serviceID));
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.out.println(String.format(
                            "ServiceApi with ID: %d, in run() Thread.sleep() was interrupted.",
                            serviceID));
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (messagesList.size() != 0) {
                    JSONObject requestObject = messagesList.poll();
                    if (requestObject.getBoolean("internalMessage")
                            && requestObject.getString("type").contains("Response")) {
                        writerToAgent.println(requestObject.toString());
                        writerToAgent.flush();
                    } else {
                        messagesList.add(requestObject);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(String.format(
                            "ServiceApi with ID: %d, in run() Thread.sleep() was interrupted.",
                            serviceID));
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Get serviceID which is a unique identifier assigned by the Manager
     * 
     * @return serviceID as {@code int}
     */
    public int getServiceID() {
        return serviceID;
    }

    @Override
    public String UUIDGenerator() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }

}