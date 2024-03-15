import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

import com.fasterxml.uuid.Generators;

public class ManagerApi implements IManagerApi<Integer>, Runnable {

    private LinkedList<JSONObject> responseList;

    private int managerPort;

    private final ExecutorService executorService;

    private ArrayList<AbstractManagerToAgentConnection<Integer>> connections;

    public ManagerApi() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        responseList = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        connections = new ArrayList<>();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(managerPort)) {
            while (true) {
                Socket socket = serverSocket.accept();
                connections.add(new ManagerToAgentConnection(responseList, socket));
            }
        } catch (IOException e) {
            System.out.println(
                    "ManagerApi in run method was error with ServerSocket or with getting Streams from new Socket");
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Override
    public void start(JSONObject args) {
        managerPort = args.getInt("port");
        run();
    }

    @Override
    public Future<JSONObject> receiveDataToProcess() {
        return executorService.submit(new Callable<JSONObject>() {
            @Override
            public JSONObject call() {
                JSONObject temp;
                while (true) {
                    if (responseList.size() < 1) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            System.out.println(
                                    "ManagerApi in receiveDataToProcess() Thread.sleep() was interrupted");
                            e.printStackTrace();
                        }
                    } else {
                        temp = responseList.poll();
                        break;
                    }
                }
                return temp;
            }
        });
    }

    @Override
    public String UUIDGenerator() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }

    @Override
    public void sendData(JSONObject message, Integer agentID) {
        connections.stream().filter(c -> c.agentID == agentID).findFirst()
                .ifPresentOrElse(a -> a.sendMessage(message), () -> {
                    System.out.println(String.format("Cannot find Agent with %d identifier", agentID));
                });
    }

}
