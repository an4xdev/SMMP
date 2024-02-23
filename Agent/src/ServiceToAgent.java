import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceToAgent implements Runnable {

    // private String typeOfService;

    private int serviceID;

    private LinkedList<JSONObject> receiveList;
    private LinkedList<JSONObject> sendToServiceList;
    private final ExecutorService executorService;

    private Socket socketFromService;
    private BufferedReader readerFromService;
    private PrintWriter writerToService;

    public ServiceToAgent(Socket socket, LinkedList<JSONObject> receiveList) throws IOException {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.receiveList = receiveList;
        sendToServiceList = new LinkedList<JSONObject>();
        this.socketFromService = socket;
        setInputOutput();
        this.run();
    }

    public void sendMessage(JSONObject message) {
        sendToServiceList.add(message);
    }

    private void setInputOutput() throws IOException {
        readerFromService = new BufferedReader(new InputStreamReader(socketFromService.getInputStream()));
        writerToService = new PrintWriter(socketFromService.getOutputStream());
    }

    public int getServiceID() {
        return serviceID;
    }

    @Override
    public void run() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // TODO: check if this is startServiceResponse and set fields
                    receiveList.add(new JSONObject(readerFromService.readLine()));
                } catch (JSONException | IOException e) {
                    System.out.println(
                            "ServiceToAgent in run() method in runnable that is reading data from Service, was JSON exception.");
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(
                            "ServiceToAgent in run() method in runnable that is reading data from Service, Thread.sleep() was interrupted.");
                    e.printStackTrace();
                }
            }
        });

        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (sendToServiceList.size() > 0) {
                    writerToService.println(sendToServiceList.poll().toString());
                    writerToService.flush();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println(
                            "ServiceToAgent in run() method in runnable that is writing data to Service, Thread.sleep() was interrupted.");
                    e.printStackTrace();
                }
            }
        });

    }

}
