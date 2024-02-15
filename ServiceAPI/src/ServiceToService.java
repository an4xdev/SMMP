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

    private int plug;
    private String typeOfService;

    private LinkedList<JSONObject> sendList;
    private LinkedList<JSONObject> receiveList;

    private final ExecutorService executorService;

    private Socket socketToService;
    private PrintWriter writerToService;
    private BufferedReader readerFromService;

    private AtomicBoolean isBusy;

    public ServiceToService(int plug, String typeOfService, LinkedList<JSONObject> sendList,
            LinkedList<JSONObject> receiveList, JSONObject dataToConnect) throws IOException {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.plug = plug;
        this.typeOfService = typeOfService;
        this.sendList = sendList;
        this.receiveList = receiveList;
        initializeSocket(null);
        this.run();
    }

    private void initializeSocket(JSONObject dataToConnect) throws IOException {
        socketToService = new Socket("localhost", 1234, InetAddress.getLoopbackAddress(), plug);
        writerToService = new PrintWriter(socketToService.getOutputStream());
        readerFromService = new BufferedReader(new InputStreamReader(socketToService.getInputStream()));
    }

    public int getPlug() {
        return plug;
    }

    public boolean getIsBusy() {
        return isBusy.get();
    }

    public void disconnect() {
        // TODO: add reference to ServiceApi to send closing message to Manager
        executorService.shutdown();
    }

    @Override
    public void run() {
        isBusy.set(false);
        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String response = readerFromService.readLine();
                    receiveList.add(new JSONObject(response));
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO: Auto-generated catch block
                        e.printStackTrace();
                    }
                    isBusy.set(false);
                }
            } catch (IOException e) {
                // TODO: Auto-generated catch block
                e.printStackTrace();
            }
        });
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                JSONObject sendData = sendList.poll();
                //TODO: if field in identifier is equals to typeOfService send
                writerToService.println(sendData.toString());
                writerToService.flush();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO: Auto-generated catch block
                    e.printStackTrace();
                }
                isBusy.set(true);
            }
        });

    }

}
