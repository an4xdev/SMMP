import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class ManagerToAgentConnection extends AbstractManagerToAgentConnection<Integer> {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> requests;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private int agentID;

    public ManagerToAgentConnection(LinkedList<JSONObject> responses, Socket socket) throws IOException {
        super(responses);
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        requests = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        writer = new PrintWriter(this.socket.getOutputStream());
    }

    @Override
    public void run() {

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (requests.size() > 0) {
                        writer.println(requests.pollFirst().toString());
                        writer.flush();
                    }

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println(String.format(
                                "Agent with id: %d in run method in runnable that is sending requests Thread.sleep() throw InterruptedException",
                                agentID));
                    }
                }
            }

        });

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        JSONObject object = new JSONObject(reader.readLine());
                        responses.add(object);
                        Thread.sleep(10);
                    } catch (JSONException e) {
                        System.out.println(String.format(
                                "Agent with id: %d in run method in runnable that is getting responses was error with parsing JSON from String",
                                agentID));
                    } catch (IOException e) {
                        System.out.println(String.format(
                                "Agent with id: %d in run method in runnable that is getting responses was error with reading from input stream",
                                agentID));
                    } catch (InterruptedException e) {
                        System.out.println(String.format(
                                "Agent with id: %d in run method in runnable that is getting responses Thread.sleep() throw InterruptedException",
                                agentID));
                    }
                }

            }

        });
    }

    @Override
    public void sendMessage(JSONObject message) {
        requests.add(message);
    }

}
