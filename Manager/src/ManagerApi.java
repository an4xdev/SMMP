import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

import com.fasterxml.uuid.Generators;

public class ManagerApi implements IManagerApi, Runnable {

    private LinkedList<JSONObject> requestList;
    private LinkedList<JSONObject> responseList;

    private int managerPort;

    private final ExecutorService executorService;

    public ManagerApi() {
        executorService = Executors.newVirtualThreadPerTaskExecutor();
        requestList = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
        responseList = (LinkedList<JSONObject>) Collections.synchronizedList(new LinkedList<JSONObject>());
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    @Override
    public void start(JSONObject args) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void sendData(JSONObject message) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendData'");
    }

    @Override
    public JSONObject receiveDataToProcess() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'receiveDataToProcess'");
    }

    @Override
    public String UUIDGenerator() {
        return Generators.timeBasedEpochGenerator().generate().toString();
    }

}
