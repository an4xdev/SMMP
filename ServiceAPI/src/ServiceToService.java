import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONObject;

public class ServiceToService implements Runnable {

    private int sourceServicePlug;
    private int destServicePlug;
    private String typeOfService;

    private LinkedList<JSONObject> sendList;
    private LinkedList<JSONObject> receiveList;

    private final ExecutorService executorService;

    private Socket socketToService;
    private PrintWriter writerToService;
    private BufferedReader readerFromService;

    private AtomicBoolean isBusy;

    private ServiceApi serviceApi;

    public ServiceToService(int sourceServicePlug, String typeOfService, LinkedList<JSONObject> sendList,
            LinkedList<JSONObject> receiveList, JSONObject dataToConnect, ServiceApi serviceApi) throws IOException {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.sourceServicePlug = sourceServicePlug;
        this.typeOfService = typeOfService;
        this.sendList = sendList;
        this.receiveList = receiveList;
        this.serviceApi = serviceApi;
        initializeSocket(dataToConnect);
        this.run();
    }

    private void initializeSocket(JSONObject dataToConnect) throws IOException {
        socketToService = new Socket(
                InetAddress.getByAddress(
                        dataToConnect.getString("destServiceNetwork").getBytes()),
                dataToConnect.getInt("destServicePlug"),
                InetAddress.getLocalHost(),
                sourceServicePlug);
        writerToService = new PrintWriter(socketToService.getOutputStream());
        readerFromService = new BufferedReader(new InputStreamReader(socketToService.getInputStream()));
        destServicePlug = dataToConnect.getInt("destServicePlug");
    }

    public int getSourceServicePlug() {
        return sourceServicePlug;
    }

    public boolean getIsBusy() {
        return isBusy.get();
    }

    public void disconnect(String messageID) {
        // try closing connection
        boolean success = true;
        writerToService.close();
        try {
            readerFromService.close();
            socketToService.close();
        } catch (IOException e) {
            // TODO: Auto-generated catch block
            e.printStackTrace();
            success = false;
        }

        // send closing message to Manager
        JSONObject sourceConnectionClose = new JSONObject();
        sourceConnectionClose.put("type", "sourceConnectionClose");
        sourceConnectionClose.put("internalMessage", true);
        sourceConnectionClose.put("messageID", messageID);
        sourceConnectionClose.put("serviceID", serviceApi.getServiceID());
        sourceConnectionClose.put("sourceServicePlug", sourceServicePlug);
        sourceConnectionClose.put("destServicePlug", destServicePlug);
        sourceConnectionClose.put("success", success);

        serviceApi.sendMessage(sourceConnectionClose);

        executorService.shutdown();
    }

    @Override
    public void run() {
        isBusy.set(false);
        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    JSONObject sendData = sendList.poll();
                    // if message isn't internal and dest Service is equal typOfService send data
                    if (!sendData.getBoolean("internalMessage")
                            && sendData.getString("typeOfService").equals(typeOfService)) {
                        isBusy.set(true);
                        writerToService.println(sendData.toString());
                        writerToService.flush();
                        String response = readerFromService.readLine();
                        receiveList.add(new JSONObject(response));
                        isBusy.set(false);
                    } else {
                        sendList.add(sendData);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO: Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                // TODO: Auto-generated catch block
                e.printStackTrace();
            }
        });

    }

}
