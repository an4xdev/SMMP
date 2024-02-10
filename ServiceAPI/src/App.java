import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.uuid.Generators;

public class App {
    public static void main(String[] args) throws Exception {
        // got from https://stackoverflow.com/a/60318055 and https://jar-download.com/artifacts/com.fasterxml.uuid/java-uuid-generator/4.3.0
        UUID uuid = Generators.timeBasedGenerator().generate();
        System.out.println(uuid.toString());
        Test test = new Test("M", "Ż");
        JSONObject obj = new JSONObject(test);
        String foo = obj.toString();
        JSONObject boo = new JSONObject(foo);
        System.out.println(boo);
    }
}
