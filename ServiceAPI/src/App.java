import java.util.UUID;

import com.fasterxml.uuid.Generators;

public class App {
    public static void main(String[] args) throws Exception {
        // got from https://stackoverflow.com/a/60318055 and https://jar-download.com/artifacts/com.fasterxml.uuid/java-uuid-generator/4.3.0
        UUID uuid = Generators.timeBasedGenerator().generate();
        System.out.println(uuid.toString());
    }
}
