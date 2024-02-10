import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

public class App {
    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        final JSONObject mutable = new JSONObject();
        Runnable r = new Runnable() {
            public void run() {
                mutable.put("foo", 10);
            }
        };
        Future<JSONObject> f = executorService.submit(r, mutable);
        JSONObject result;
        try {
            result = f.get();
            System.out.println("result: " + result);
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}