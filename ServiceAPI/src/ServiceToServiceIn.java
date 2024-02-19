import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceToServiceIn implements Runnable {

    private final ExecutorService executorService;

    private LinkedList<JSONObject> messagesList;
    private ServiceApi serviceApi;
    private int plugIn;
    private int sourceServicePlugOut;

    private ServerSocket serverSocket;
    private BufferedReader readerFromService;
    private PrintWriter writerToService;

    public ServiceToServiceIn(int plugIn, LinkedList<JSONObject> messagesList) {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.plugIn = plugIn;
        this.messagesList = messagesList;
        run();
    }

    public void disconnected() {
        JSONObject disconnectJSON = new JSONObject();
        disconnectJSON.put("type", "destConnectionClose");
        disconnectJSON.put("internalMessage", true);
        disconnectJSON.put("messageID", serviceApi.UUIDGenerator());
        disconnectJSON.put("serviceID", serviceApi.getServiceID());
        disconnectJSON.put("sourcePlug", sourceServicePlugOut);
        disconnectJSON.put("destPlug", plugIn);
        serviceApi.sendMessage(disconnectJSON);
        executorService.shutdown();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(plugIn);
        } catch (IOException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }

        while (!Thread.currentThread().isInterrupted()) {
            try (Socket socket = serverSocket.accept()) {
                readerFromService = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writerToService = new PrintWriter(socket.getOutputStream());
                sourceServicePlugOut = socket.getPort();
                executorService.submit(() -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            String request = readerFromService.readLine();
                            if (request != null) {
                                JSONObject requestJSON = new JSONObject(request);
                                messagesList.add(requestJSON);
                                Future<JSONObject> response = serviceApi
                                        .receivceResponse(requestJSON.getString("messageID"));
                                try {
                                    JSONObject responseJSON = response.get();
                                    writerToService.println(responseJSON.toString());
                                    writerToService.flush();
                                } catch (InterruptedException e) {
                                    // TODO: Auto-generated catch block
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    // TODO: Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else {
                                disconnected();
                            }
                        } catch (JSONException e) {
                            // TODO: Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO: Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                // TODO: Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
