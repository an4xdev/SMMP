import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.uuid.Generators;

public class AgentApi implements IAgentApi, Runnable {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> receiveList;
    private LinkedList<JSONObject> dataToManager;

    private Socket socketToManager;
    private BufferedReader readerFromManager;
    private PrintWriter writerToManager;

    private int agentPort;
    private String agentNetwork;

    private ArrayList<ServiceToAgent> connectionsToAgent;

    public AgentApi() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        receiveList = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        dataToManager = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        connectionsToAgent = new ArrayList<>();
    }

    @Override
    public void start(JSONObject args) {
        agentPort = args.getInt("agentPort");
        // connect to Manager
        try {
            socketToManager = new Socket(args.getString("managerNetwork"), args.getInt("managerPort"));
        } catch (IOException e) {
            System.out.println("Agent in start() method cannot connect to Manager. Closing application");
            e.printStackTrace();
            executorService.shutdown();
            System.exit(-1);
        }
        try {
            readerFromManager = new BufferedReader(new InputStreamReader(socketToManager.getInputStream()));
        } catch (IOException e) {
            System.out.println("Agent in start() method exception with BufferedReader. Closing application");
            e.printStackTrace();
            executorService.shutdown();
            System.exit(-1);
        }
        try {
            writerToManager = new PrintWriter(socketToManager.getOutputStream());
        } catch (IOException e) {
            System.out.println("Agent in start() method exception with PrintWriter. Closing application");
            e.printStackTrace();
            executorService.shutdown();
            System.exit(-1);
        }
        // register to Manager
        JSONObject registration = new JSONObject();
        registration.put("type", "agentRegisterRequest");
        registration.put("messageID", UUIDGenereator());
        agentNetwork = socketToManager.getLocalAddress().getHostAddress();
        registration.put("agentNetwork", agentNetwork);
        registration.put("agentPort", agentPort);
        registration.put("repository", args.getJSONArray("repository"));
        // send message
        sendMessage(registration);
        // create ServerSocket for connections with Services
        executorService.submit(() -> {
            try (ServerSocket serverSocket = new ServerSocket(agentPort)) {
                while (!Thread.currentThread().isInterrupted()) {
                    Socket socket = serverSocket.accept();
                    connectionsToAgent.add(new ServiceToAgent(socket, receiveList));
                }
            } catch (IOException e) {
                System.out.println("Agent in start() method in runnable where is accepting connections with Services");
                e.printStackTrace();
                Thread.currentThread().interrupt();
                // executorService.shutdown();?
                // System.exit(-1);?
            }
        });
        this.run();
    }

    @Override
    public void sendMessage(JSONObject message) {
        try {
            // get from connections list object with specific serviceID and send data, or send to Manager
            connectionsToAgent.stream().filter(c -> c.getServiceID() == message.getInt("serviceID")).findFirst()
                    .ifPresentOrElse(t -> t.sendMessage(message), () -> {
                        System.out.println(
                                "Agent in sendMessage() got data to send to Service with serviceID that's isn't connected to it. Data: "
                                        + message.toString());
                    });

        } catch (JSONException e) {
            System.out.println("Agent in sendMessage() got data to Manager.");
            dataToManager.add(message);
        }
    }

    @Override
    public JSONObject receiveMessage() {
        // TODO: add more informations about who send data and who needs to receive this data
        return receiveList.poll();
    }

    @Override
    public String UUIDGenereator() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    receiveList.add(new JSONObject(readerFromManager.readLine()));
                } catch (IOException e) {
                    System.out.println(
                            "Agent in run() method in runnable that is reading data from Manager. Exception was throw.");
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(
                            "Agent in run() method in runnable that is reading data from Manager. Thread.sleep() throws exception.");
                    e.printStackTrace();
                }
            }
        });
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (dataToManager.size() > 0) {
                    JSONObject object = dataToManager.poll();

                    writerToManager.println(object.toString());
                    writerToManager.flush();

                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(
                            "Agent in run() method in runnable that is writing data to Manager. Thread.sleep() throws exception.");
                    e.printStackTrace();
                }
            }
        });
    }

}
